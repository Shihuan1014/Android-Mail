package edu.hnu.mail.data.source.local;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.hnu.mail.constant.MailType;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.dao.DaoManager;
import edu.hnu.mail.data.dao.DaoSession;
import edu.hnu.mail.data.dao.MailDao;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.MailListDataSource;
import edu.hnu.mail.data.source.remote.MultiPart;
import edu.hnu.mail.data.source.remote.POP3;
import edu.hnu.mail.data.source.remote.POP3Client;
import edu.hnu.mail.data.source.remote.POP3MessageInfo;

public class LocalMailListDataSource implements MailListDataSource {

    private DaoSession daoSession;
    private MailDao mailDao;
    private Context context;

    public LocalMailListDataSource() {

    }

    // 请务必使用此构造函数
    public LocalMailListDataSource(Context context){
        daoSession = DaoManager.getInstance(context).getDaoSession();
        mailDao = daoSession.getMailDao();
        this.context = context;
    }
    @Override
    public void getMailList(@NonNull LoadCallBack loadCallBack,int type) {
        List<Mail> list = null;
        try {
            if(type == MailType.ALL_INBOX){
                list = mailDao.queryBuilder()
                        .where(MailDao.Properties.MailBox.eq("INBOX"),
                                MailDao.Properties.Deleted.eq(0),
                                MailDao.Properties.RealDeleted.eq(0))
                        .orderDesc(MailDao.Properties.SendTime)
                        .list();
            }else if (type == MailType.ALL_FLAG){
                list = mailDao.queryBuilder()
                        .where(MailDao.Properties.Flag.eq(1),
                                MailDao.Properties.RealDeleted.eq(0))
                        .orderDesc(MailDao.Properties.SendTime)
                        .list();
            }
            else{
                User user = UserInfo.users.get(UserInfo.currentIndex);
                String userName = user.getPopUserName();
                System.out.println(type);
                switch (type){
                    case MailType.INBOX:
                        list = mailDao.queryBuilder()
                                .where(MailDao.Properties.UserAddress.eq(userName),
                                        MailDao.Properties.MailBox.eq("INBOX"),
                                        MailDao.Properties.Deleted.eq(0),
                                        MailDao.Properties.RealDeleted.eq(0)
                                )
                                .orderDesc(MailDao.Properties.SendTime)
                                .list();
                        break;
                    case MailType.DRAFT:
                        list = mailDao.queryBuilder()
                                .where(MailDao.Properties.UserAddress.eq(userName),
                                        MailDao.Properties.MailBox.eq("SENT"),
                                        MailDao.Properties.Draft.eq(1),
                                        MailDao.Properties.RealDeleted.eq(0))
                                .orderDesc(MailDao.Properties.SendTime)
                                .list();
                        System.out.println(list.size());
                        break;
                    case MailType.SENT:
                        list = mailDao.queryBuilder()
                                .where(MailDao.Properties.UserAddress.eq(userName),
                                        MailDao.Properties.MailBox.eq("SENT"),
                                        MailDao.Properties.Deleted.eq(0),
                                        MailDao.Properties.Draft.eq(0),
                                        MailDao.Properties.RealDeleted.eq(0)
                                )
                                .orderDesc(MailDao.Properties.SendTime)
                                .list();
                        break;
                    case MailType.DELETED:
                        list = mailDao.queryBuilder()
                                .where(MailDao.Properties.UserAddress.eq(userName),
                                        MailDao.Properties.Deleted.eq(1),
                                        MailDao.Properties.RealDeleted.eq(0))
                                .orderDesc(MailDao.Properties.SendTime)
                                .list();
                        break;
                    case MailType.FLAG:
                        list = mailDao.queryBuilder()
                                .where(MailDao.Properties.UserAddress.eq(userName),
                                        MailDao.Properties.Flag.eq(1),
                                        MailDao.Properties.RealDeleted.eq(0))
                                .orderDesc(MailDao.Properties.SendTime)
                                .list();
                        break;
                    default:
                        break;
                }
            }
            loadCallBack.onLoadSuccess(list);
        }catch (Exception e){
            e.printStackTrace();
            loadCallBack.onLoadFailure();
        }

    }

    /**
     * 连接pop服务器获取新邮件
     * @param loadCallBack
     * @param isAllInboxMail
     */
    @Override
    public void getNewMailList(@NonNull LoadCallBack loadCallBack,boolean isAllInboxMail){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Mail> mailList = new ArrayList<Mail>();
                    final List<User> users = UserInfo.users;
                    try {
                        if (isAllInboxMail) {
                            for (User user : users) {
                                fetchUserMail(user, mailList);
                            }
                        } else {
                            User user = users.get(UserInfo.currentIndex);
                            fetchUserMail(user, mailList);
                        }
                        loadCallBack.onLoadSuccess(mailList);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("走这里");
                        loadCallBack.onLoadFailure();
                    }
                    System.out.println("getNewMailList的结果数量是：" + mailList.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }}).start();
    }

    @Override
    public void getNewMailListLocal(@NonNull LoadCallBack loadCallBack,int type,Date date){
        List<Mail> list = null;
        if(type == MailType.ALL_INBOX){
            list = mailDao.queryBuilder()
                    .where(MailDao.Properties.MailBox.eq("INBOX"),
                            MailDao.Properties.Deleted.eq(0),
                            MailDao.Properties.SendTime.gt(date),
                            MailDao.Properties.RealDeleted.eq(0))
                    .orderDesc(MailDao.Properties.SendTime)
                    .list();
        }else if (type == MailType.ALL_FLAG){
            list = mailDao.queryBuilder()
                    .where(MailDao.Properties.Flag.eq(1),
                            MailDao.Properties.SendTime.gt(date),
                            MailDao.Properties.RealDeleted.eq(0))
                    .orderDesc(MailDao.Properties.SendTime)
                    .list();
        }
        else{
            User user = UserInfo.users.get(UserInfo.currentIndex);
            String userName = user.getPopUserName();
            System.out.println(type);
            switch (type){
                case MailType.INBOX:
                    list = mailDao.queryBuilder()
                            .where(MailDao.Properties.UserAddress.eq(userName),
                                    MailDao.Properties.MailBox.eq("INBOX"),
                                    MailDao.Properties.Deleted.eq(0),
                                    MailDao.Properties.SendTime.gt(date),
                                    MailDao.Properties.RealDeleted.eq(0)
                            )
                            .orderDesc(MailDao.Properties.SendTime)
                            .list();
                    break;
                case MailType.DRAFT:
                    list = mailDao.queryBuilder()
                            .where(MailDao.Properties.UserAddress.eq(userName),
                                    MailDao.Properties.MailBox.eq("SENT"),
                                    MailDao.Properties.Draft.eq(1),
                                    MailDao.Properties.SendTime.gt(date),
                                    MailDao.Properties.RealDeleted.eq(0))
                            .orderDesc(MailDao.Properties.SendTime)
                            .list();
                    System.out.println(list.size());
                    break;
                case MailType.SENT:
                    list = mailDao.queryBuilder()
                            .where(MailDao.Properties.UserAddress.eq(userName),
                                    MailDao.Properties.MailBox.eq("SENT"),
                                    MailDao.Properties.Deleted.eq(0),
                                    MailDao.Properties.Draft.eq(0),
                                    MailDao.Properties.SendTime.gt(date),
                                    MailDao.Properties.RealDeleted.eq(0)
                            )
                            .orderDesc(MailDao.Properties.SendTime)
                            .list();
                    break;
                case MailType.DELETED:
                    list = mailDao.queryBuilder()
                            .where(MailDao.Properties.UserAddress.eq(userName),
                                    MailDao.Properties.Deleted.eq(1),
                                    MailDao.Properties.SendTime.gt(date),
                                    MailDao.Properties.RealDeleted.eq(0))
                            .orderDesc(MailDao.Properties.SendTime)
                            .list();
                    break;
                case MailType.FLAG:
                    list = mailDao.queryBuilder()
                            .where(MailDao.Properties.UserAddress.eq(userName),
                                    MailDao.Properties.Flag.eq(1),
                                    MailDao.Properties.SendTime.gt(date),
                                    MailDao.Properties.RealDeleted.eq(0))
                            .orderDesc(MailDao.Properties.SendTime)
                            .list();
                    break;
                default:
                    break;
            }
        }
        loadCallBack.onLoadSuccess(list);
    }

    private void fetchUserMail(User user,List<Mail> mailList) throws Exception {
        String userName = user.getPopUserName();
        String password = user.getPopPassword();
        String popHost = user.getPopHost();
        int popPort = user.getPopPort();
        POP3Client p = user.getPop3Client();
        if (p == null){
            p = new POP3Client(context);
        }
        if (!p.isConnected()){
            p.connect(popHost, popPort);
            //3秒钟read超时自动断开连接
            p.setSoTimeout(3000);
            p.login(userName, password);
        }
        POP3MessageInfo[] list = p.listUniqueIdentifiers();
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
                    mailList.add(saveMail(p,mail));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        p.disconnect();
    }

    public Mail saveMail(POP3Client p,edu.hnu.mail.data.source.remote.Mail mailFormat) throws Exception{
        User user = UserInfo.users.get(UserInfo.currentIndex);
        String userName = user.getPopUserName();
        Map<String,String> headers = mailFormat.getHeader();
        MultiPart multiPart = mailFormat.getMultiPart();
        // 邮件实体对应数据库
        edu.hnu.mail.data.entity.Mail mail = new edu.hnu.mail.data.entity.Mail();
        mail.setUserAddress(userName);
        mail.setUid(mailFormat.getUid());
        System.out.println(mailFormat.getUid());
        mail.setMailBox("INBOX");
        mail.setSendTime(new Date(headers.get("Date")));
        mail.setMessageId(headers.get("Message-ID"));
        String from = headers.get("From");
        if(from!=null){
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
                    }
                }
            }else{
                //只有邮箱
                if(from.startsWith("<")){
                    from = from.replace("<","");
                    from = from.replace(">","");
                    mail.setFromEmail(from);
                }else{
                    mail.setFromEmail(from);
                }
            }
        }
        String subject = headers.get("Subject");
        if(subject!=null){
            mail.setSubject(p.decodeMailBase64(subject));
        }
        String to = headers.get("To");
        if(to!=null){
            int i = to.indexOf("<");
            if(i>0){
                Matcher matcher = Pattern.compile("\"(.*)\"\\s+<(.*)>").matcher(to);
                if(matcher.find()){
                    mail.setTo(p.decodeMailBase64(matcher.group(1)));
                    mail.setToEmail(matcher.group(2));
                }
            }else{
                //只有邮箱
                if(to.startsWith("<")){
                    to = to.replace("<","");
                    to = to.replace(">","");
                    mail.setToEmail(to);
                }else{
                    mail.setToEmail(to);
                }
                mail.setTo(to);
            }
        }

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
        mail.setReplyTo(headers.get("Reply-To"));
        System.out.println("插入mail" + mail);
        /**
         * 现在进入测试环境，不要写数据库
         */
        mailDao.insert(mail);
        return mail;
//        //把附件url写入数据库
//        List<Attachment> list = mailFormat.getAttachments();
//        if(list!=null){
//            for (Attachment a:list){
//                System.out.println("文件" + a.getPath());
//                attachmentDao.insert(a);
//            }
//        }
    }
}
