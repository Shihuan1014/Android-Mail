package edu.hnu.mail.data.source.local;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sun.mail.pop3.POP3Folder;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.dao.AttachmentDao;
import edu.hnu.mail.data.dao.DaoManager;
import edu.hnu.mail.data.dao.DaoSession;
import edu.hnu.mail.data.dao.DataDao;
import edu.hnu.mail.data.dao.MailDao;
import edu.hnu.mail.data.dao.UserDao;
import edu.hnu.mail.data.entity.Attachment;
import edu.hnu.mail.data.entity.Data;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.MailBoxDataSource;
import edu.hnu.mail.data.source.remote.MultiPart;
import edu.hnu.mail.data.source.remote.POP3;
import edu.hnu.mail.data.source.remote.POP3Client;
import edu.hnu.mail.data.source.remote.POP3MessageInfo;

/**
 * 获取远程的信件，使用JavaMail
 */
public class RemoteMailBoxDataSource implements MailBoxDataSource {

    private DaoSession daoSession;
    private MailDao mailDao;
    private DataDao dataDao;
    private UserDao userDao;
    private AttachmentDao attachmentDao;
    private Context context;
    public static String TAG = "RemoteMailBoxDataSource  ";

    public RemoteMailBoxDataSource(){

    }

    public RemoteMailBoxDataSource(Context context){
        this();
        daoSession = DaoManager.getInstance(context).getDaoSession();
        mailDao = daoSession.getMailDao();
        attachmentDao = daoSession.getAttachmentDao();
        userDao = daoSession.getUserDao();
        dataDao = daoSession.getDataDao();
        this.context = context;
    }

    /**
     * 主界面获得全部收件、全部星标邮件数量
     * @param loadCallBack
     */
    @Override
    public void getMailBox(@NonNull LoadCallBack loadCallBack) {
        int[] list = new int[2];
        list[0] = UserInfo.getTotalMailCount();
        list[1] = 0;
        Long i = mailDao.queryBuilder()
                .where(MailDao.Properties.Flag.eq(1),
                        MailDao.Properties.RealDeleted.eq(0))
                .count();
        list[1] = i.intValue();
        //获取成功回调
        loadCallBack.onLoadSuccess(list);
    }

    /**
     * 某个用户界面，获得其所有种类的邮箱邮件数
     * 分别是：收件、星标、群发、草稿、发送、删除
     * @param userName
     * @param loadCallBack
     */
    @Override
    public void getUserMailBox(String userName,@NonNull LoadCallBack loadCallBack){
        int[] list = new int[6];
        long i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.MailBox.eq("INBOX"),
                        MailDao.Properties.Deleted.eq(0),
                        MailDao.Properties.RealDeleted.eq(0))
                .count();
        list[0] = (int) i;
        i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.Flag.eq(1),
                        MailDao.Properties.RealDeleted.eq(0))
                .count();
        list[1] = (int) i;
        list[2] = 0;
        i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.Draft.eq(1),
                        MailDao.Properties.RealDeleted.eq(0))
                .count();
        list[3] = (int) i;
        //注意，发件是不会有DELETED状态的，因为会被直接删
        i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.MailBox.eq("SENT"),
                        MailDao.Properties.Draft.eq(0))
                .count();
        list[4] = (int) i;
        i = mailDao.queryBuilder()
                .where(MailDao.Properties.UserAddress.eq(userName),
                        MailDao.Properties.Deleted.eq(1),
                        MailDao.Properties.RealDeleted.eq(0))
                .count();
        list[5] = (int) i;
        //获取成功回调
        loadCallBack.onLoadSuccess(list);
    }
    @Override
    public void getNewMailBox(@NonNull LoadCallBack loadCallBack){
        //准备会话信息
        final List<User> users = UserInfo.users;
//        User user = UserInfo.users.get(UserInfo.currentIndex);
        int length = users.size();
        int[] count = new int[length];
        int index = 0;
        //因为我们是共用同一个Socket,如果连接还存在，说明正在获取邮件
        if(DefaultServer.isFetching){
            loadCallBack.showLoading();
        }else{
            DefaultServer.isFetching = true;
            for(User user : users) {
                try {
                    List<Mail> mailList = new ArrayList<Mail>();
                    List<Data> dataList = new ArrayList<Data>();
                    String userName = user.getPopUserName();
                    String password = user.getPopPassword();
                    String popHost = user.getPopHost();
                    int popPort = user.getPopPort();
                    POP3Client p = user.getPop3Client();
                    if (p == null) {
                        p = new POP3Client(context);
                    }
                    if(!p.isConnected()){
                        p.connect(popHost, popPort);
                        //3秒钟read超时自动断开连接
                        p.setSoTimeout(3000);
                        if (p.isConnected()) {
                            if(p.login(userName, password)) {
                                user.setPop3Client(p);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            POP3Client p = user.getPop3Client();
                                            while (p.isConnected()) {
                                                Thread.sleep(5000);
                                                p.noop();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }else{
                                //登录错误
                                loadCallBack.onLoadFailure();
                                continue;
                            }
                        } else {
                            loadCallBack.onLoadFailure();
                            continue;
                        }
                    }
                    POP3MessageInfo[] list = p.listUniqueIdentifiers();
                    System.out.println(Arrays.toString(list));
                    for (POP3MessageInfo pop3MessageInfo : list) {
                        String uid = pop3MessageInfo.identifier;
                        Long i = mailDao.queryBuilder()
                                .where(MailDao.Properties.UserAddress.eq(userName),
                                        MailDao.Properties.MailBox.eq("INBOX"),
                                        MailDao.Properties.Uid.eq(uid))
                                .count();
                        if (i == 0) {
                            System.out.println("正在解析邮件:" + uid);
                            try {
                                edu.hnu.mail.data.source.remote.Mail mail = p.retrMail(p, pop3MessageInfo.number);
                                mail.setUid(uid);
                                count[index]++;
                                Mail mail1 = parseMail(p,userName,mail);
                                Data data = new Data();
                                data.setUserAddress(mail1.getUserAddress());
                                data.setUid(mail1.getUid());
                                data.setMailBox(mail1.getMailBox());
                                data.setData(mail1.getData());
                                mailList.add(mail1);
                                dataList.add(data);
                            } catch (Exception e) {
                                count[index]--;
                                e.printStackTrace();
                            }
                        }
                    }
                    mailDao.insertInTx(mailList);
                    dataDao.insertInTx(dataList);
                    index++;
                }catch (Exception e){
                    e.printStackTrace();
                    loadCallBack.onLoadFailure();
                }
            }
            DefaultServer.isFetching = false;
            System.out.println("这里");
            loadCallBack.onLoadSuccess(count);
        }
    }


    public Mail parseMail(POP3Client p,String userName,edu.hnu.mail.data.source.remote.Mail mailFormat) throws Exception{
        Map<String,String> headers = mailFormat.getHeader();
        MultiPart multiPart = mailFormat.getMultiPart();
        // 邮件实体对应数据库
        edu.hnu.mail.data.entity.Mail mail = new edu.hnu.mail.data.entity.Mail();
        mail.setUserAddress(userName);
        mail.setUid(mailFormat.getUid());
        System.out.println(mailFormat.getUid());
        mail.setMailBox("INBOX");
        String date = headers.get("Date");
        if(date == null){
            String received = headers.get("Received");
            if (received!=null){
                int i = received.indexOf(";");
                date = received.substring(i+1);
            }
        }
        try {
            mail.setSendTime(new Date(headers.get("Date")));
        }catch (Exception e){
            e.printStackTrace();
        }
        mail.setMessageId(headers.get("Message-ID"));
        String from = headers.get("From");
        if(from!=null){
            System.out.println("邮箱From: " + from);
            int i = from.indexOf("<");
            if(i>0){
                Matcher matcher = Pattern.compile("\"(.*)\"\\s+<(.*)>").matcher(from);
                if(matcher.find()){
                    mail.setFrom(p.decodeMailBase64(matcher.group(1)));
                    mail.setFromEmail(matcher.group(2));
                }else{
                    matcher = Pattern.compile("(.*)\\s+<(.*)>").matcher(from);
                    if(matcher.find()) {
                        mail.setFrom(p.decodeMailBase64(matcher.group(1)));
                        mail.setFromEmail(matcher.group(2));
                    }else{
                        from = from.replace("<","");
                        from = from.replace(">","");
                        from = p.decodeMailBase64(from);
                        mail.setFromEmail(from);
                        mail.setFrom(from);
                    }
                }
            }else{
                //只有邮箱
                from = p.decodeMailBase64(from);
                mail.setFromEmail(from);
                mail.setFrom(from);
            }
        }
        String subject = headers.get("Subject");
        if(subject!=null){
            mail.setSubject(p.decodeMailBase64(subject));
        }
        String to = headers.get("To");
        if(to!=null){
            System.out.println("To: " + to);
            int i = to.indexOf("<");
            if(i>0){
                Matcher matcher = Pattern.compile("\"(.*)\"\\s+<(.*)>").matcher(to);
                if(matcher.find()){
                    mail.setTo(p.decodeMailBase64(matcher.group(1)));
                    mail.setToEmail(matcher.group(2));
                }else{
                    to = to.replace("<","");
                    to = to.replace(">","");
                    to = p.decodeMailBase64(to);
                    mail.setToEmail(to);
                    mail.setTo(to);
                }
            }else{
                to = p.decodeMailBase64(to);
                mail.setToEmail(to);
                mail.setTo(to);
            }
        }
        try {
            if (multiPart!=null) {
                mail.setData(multiPart.getContent().getBytes());
                String textContent = multiPart.getTextContent();
                if(textContent!=null){
                    textContent = Jsoup.parse(textContent).text();
                    mail.setTextContent(textContent);
                }else if((textContent = multiPart.getContent())!=null){
                    mail.setTextContent(Jsoup.parse(textContent).text());
                }
            }else{
                String textContent = mailFormat.getText();
                if(textContent!=null){
                    textContent = Jsoup.parse(textContent).text();
                    mail.setTextContent(textContent);
                }
                mail.setData(mailFormat.getText().getBytes());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mail.setReplyTo(headers.get("Reply-To"));
        System.out.println("插入mail" + mail);
        /**
         * 现在进入测试环境，不要写数据库
         */

        //把附件url写入数据库
        List<Attachment> list = mailFormat.getAttachments();
        if(list!=null){
            for (Attachment a:list){
                a.setUserAddress(mail.getUserAddress());
                a.setMailUid(mail.getUid());
                if(a.getName().endsWith(".png") || a.getName().endsWith(".jpg")
                        || a.getName().endsWith(".jpeg") || a.getName().endsWith(".gif")){
                    a.setFileType("image/jpg");
                }
                System.out.println("存储文件" + a.getPath());
            }
            attachmentDao.insertInTx(list);
        }
        return mail;
    }
}
