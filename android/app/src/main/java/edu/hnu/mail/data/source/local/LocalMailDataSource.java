package edu.hnu.mail.data.source.local;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.hnu.mail.constant.DefaultServer;
import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.dao.AttachmentDao;
import edu.hnu.mail.data.dao.DaoManager;
import edu.hnu.mail.data.dao.DaoSession;
import edu.hnu.mail.data.dao.DataDao;
import edu.hnu.mail.data.dao.MailDao;
import edu.hnu.mail.data.entity.Attachment;
import edu.hnu.mail.data.entity.Data;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.entity.User;
import edu.hnu.mail.data.source.MailDataSource;
import edu.hnu.mail.data.source.remote.SMTPClient;
import edu.hnu.mail.util.FileUtil;
import edu.hnu.mail.util.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class LocalMailDataSource implements MailDataSource {

    private DaoSession daoSession;
    private MailDao mailDao;
    private DataDao dataDao;
    private AttachmentDao attachmentDao;
    private Context context;

    public LocalMailDataSource(Context context){
        daoSession = DaoManager.getInstance(context).getDaoSession();
        mailDao = daoSession.getMailDao();
        dataDao = daoSession.getDataDao();
        attachmentDao = daoSession.getAttachmentDao();
        this.context = context;
    }

    @Override
    public void getMailById(Mail mail,@NonNull MailDataSource.LoadCallBack loadCallBack) {
        try {
            mail = mailDao.queryBuilder()
                    .where(MailDao.Properties.Uid.eq(mail.getUid()),
                            MailDao.Properties.MailBox.eq(mail.getMailBox()),
                            MailDao.Properties.UserAddress.eq(mail.getUserAddress()))
                    .build().unique();
            Data data = dataDao.queryBuilder()
                    .where(DataDao.Properties.Uid.eq(mail.getUid()),
                            DataDao.Properties.MailBox.eq(mail.getMailBox()),
                            DataDao.Properties.UserAddress.eq(mail.getUserAddress()))
                    .build().unique();
            mail.setData(data.getData());
            if (mail != null && mail.getSeen() == 0) {
                String strSql = "update " + mailDao.getTablename() +
                        " set " + MailDao.Properties.Seen.columnName + " = 1"
                        + " where " + MailDao.Properties.Uid.columnName + "='" + mail.getUid() +
                        "' and " + MailDao.Properties.MailBox.columnName + "= '" + mail.getMailBox() +
                        "' and " + MailDao.Properties.UserAddress.columnName + " = '" +
                        mail.getUserAddress() + "'";
                daoSession.getDatabase().execSQL(strSql);
            }
            if (mail != null) {
                List<Attachment> attachments = attachmentDao.queryBuilder()
                        .where(AttachmentDao.Properties.MailUid.eq(mail.getUid()),
                                AttachmentDao.Properties.UserAddress.eq(mail.getUserAddress()))
                        .list();
                mail.setAttachmentList(attachments);
                loadCallBack.onLoadSuccess(mail);
            } else {
                loadCallBack.onLoadFailure();
            }
        }catch (Exception e){
            e.printStackTrace();
            loadCallBack.onLoadFailure();
        }
    }



    @Override
    public void deleteMailById(Mail mail, @NonNull UpdateCallBack updateCallBack) {
        String strSql = null;
        if(mail.getMailBox().equalsIgnoreCase("INBOX")) {
            if (mail.getDeleted() == 0){
                strSql = "update " + mailDao.getTablename() +
                        " set " + MailDao.Properties.Deleted.columnName + " = 1 ,"
                        + MailDao.Properties.Draft.columnName + " = 0"
                        + " where " + MailDao.Properties.Uid.columnName + "='" + mail.getUid() +
                        "' and " + MailDao.Properties.MailBox.columnName + "= '" + mail.getMailBox() +
                        "' and " + MailDao.Properties.UserAddress.columnName + " = '" +
                        mail.getUserAddress() + "'";
                daoSession.getDatabase().execSQL(strSql);
            }else{
                strSql = "update " + mailDao.getTablename() +
                        " set " + MailDao.Properties.RealDeleted.columnName + " = 1 "
                        + " where " + MailDao.Properties.Uid.columnName + "='" + mail.getUid() +
                        "' and " + MailDao.Properties.MailBox.columnName + "= '" + mail.getMailBox() +
                        "' and " + MailDao.Properties.UserAddress.columnName + " = '" +
                        mail.getUserAddress() + "'";
                daoSession.getDatabase().execSQL(strSql);
                dataDao.queryBuilder()
                        .where(DataDao.Properties.Uid.eq(mail.getUid()),
                                DataDao.Properties.MailBox.eq(mail.getMailBox()),
                                DataDao.Properties.UserAddress.eq(mail.getUserAddress()))
                        .buildDelete().executeDeleteWithoutDetachingEntities();
                attachmentDao.queryBuilder()
                        .where(AttachmentDao.Properties.MailUid.eq(mail.getUid()),
                                AttachmentDao.Properties.UserAddress.eq(mail.getUserAddress()))
                        .buildDelete().executeDeleteWithoutDetachingEntities();
            }
        }else{
            mailDao.queryBuilder()
                    .where(MailDao.Properties.Uid.eq(mail.getUid()),
                            MailDao.Properties.MailBox.eq(mail.getMailBox()),
                            MailDao.Properties.UserAddress.eq(mail.getUserAddress())
                    )
                    .buildDelete().executeDeleteWithoutDetachingEntities();
            dataDao.queryBuilder()
                    .where(DataDao.Properties.Uid.eq(mail.getUid()),
                            DataDao.Properties.MailBox.eq(mail.getMailBox()),
                            DataDao.Properties.UserAddress.eq(mail.getUserAddress()))
                    .buildDelete().executeDeleteWithoutDetachingEntities();
            attachmentDao.queryBuilder()
                    .where(AttachmentDao.Properties.MailUid.eq(mail.getUid()),
                            AttachmentDao.Properties.UserAddress.eq(mail.getUserAddress()))
                    .buildDelete().executeDeleteWithoutDetachingEntities();
        }
        if (updateCallBack!=null){
            updateCallBack.onUpdateSuccess();
        }
    }

    @Override
    public void addMail(Mail mail, UpdateCallBack updateCallBack) {
        mail.setUid(String.valueOf(System.currentTimeMillis()));
        Data data = new Data();
        data.setUid(mail.getUid());
        data.setMailBox(mail.getMailBox());
        data.setUserAddress(mail.getUserAddress());
        data.setData(mail.getData());
        mailDao.insert(mail);
        long j = dataDao.queryBuilder()
                .where(DataDao.Properties.Uid.eq(mail.getUid()),
                        DataDao.Properties.MailBox.eq(mail.getMailBox()),
                        DataDao.Properties.UserAddress.eq(mail.getUserAddress()))
                .buildCount().count();
        if (j > 0){
            dataDao.queryBuilder()
                    .where(DataDao.Properties.Uid.eq(mail.getUid()),
                            DataDao.Properties.MailBox.eq(mail.getMailBox()),
                            DataDao.Properties.UserAddress.eq(mail.getUserAddress()))
                    .buildDelete().executeDeleteWithoutDetachingEntities();
        }
        dataDao.insert(data);
        List<Attachment> attachments = mail.getAttachmentList();
        if(attachments!=null && attachments.size() > 0){
            for(Attachment attachment : attachments){
                attachment.setMailUid(mail.getUid());
                attachment.setUserAddress(mail.getUserAddress());
                attachment.setMailBox(mail.getMailBox());
            }
            attachmentDao.insertInTx(attachments);
        }
        if (updateCallBack!=null){
            updateCallBack.onUpdateSuccess();
        }
    }

    @Override
    public void sendMail(Mail mail, @NonNull UpdateCallBack updateCallBack){
        User nowUser = null;
        for (User user:UserInfo.users){
            if(user.getSmtpUserName().equalsIgnoreCase(mail.getFromEmail())){
                nowUser = user;
                break;
            }
        }
        String smtpHost = nowUser.getSmtpHost();
        String userName = nowUser.getSmtpUserName();
        String passWord = nowUser.getSmtpPassword();
        if(mail.getIsGroup() == 0) {
            int port = nowUser.getSmtpPort();
            SMTPClient smtpClient = SMTPClient.getInstance();
            try {
                System.out.println("发起连接：" + smtpHost);
                smtpClient.connect(smtpHost, port);
                if (smtpClient.isConnected()) {
                    System.out.println("连接成功");
                    if (smtpClient.login(mail.getUserAddress())) {
                        System.out.println("helo成功");
                        if (smtpClient.authLogin(userName, passWord)) {
                            System.out.println("登录成功");
                            String mailContent = null;
                            //MIME编码
                            if (mail.getAttachmentList() != null && mail.getAttachmentList().size() > 0) {
                                mailContent = parseMailWithAttachment(mail);
                            } else {
                                mailContent = parseMail(mail);
                            }
                            //发送邮件，mail from , rcpt to , data三个指令
                            if (smtpClient.sendSimpleMessage(mail.getFromEmail(),
                                    mail.getToEmail().split(";"), mailContent)) {
                                System.out.println("发送成功");
                                addMail(mail, null);
                                smtpClient.disconnect();
                                updateCallBack.onUpdateSuccess();
                                return;
                            } else {
                                System.out.println("发送失败");
                                updateCallBack.onUpdateFailure();
                            }
                        }
                    }
                }
                smtpClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("发送失败");
            }
        }else{
            System.out.println("群发邮件");
            FormBody.Builder formBuild = new FormBody.Builder();
            String mailContent = null;
            try {
                if (mail.getAttachmentList() != null && mail.getAttachmentList().size() > 0) {
                    mailContent = parseMailWithAttachment(mail);
                } else {
                    mailContent = parseMail(mail);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            formBuild.add("admin",nowUser.getUserName());
            formBuild.add("data",mailContent);
            HttpUtil.httpPost(DefaultServer.ADMIN_HOST + "/sendEmailToAll", formBuild.build(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    updateCallBack.onUpdateFailure();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.getInt("status") == 200){
                            System.out.println("oldMail:" + mail);
                            String uid = mail.getUid();
                            int draft = mail.getDraft();
                            mail.setDraft(0);
                            addMail(mail,null);
                            if (draft == 1){
                                mail.setDraft(1);
                                mail.setUid(uid);
                                mail.setMailBox("SENT");
                                deleteMailById(mail,null);
                            }
                            updateCallBack.onUpdateSuccess();
                        }else{
                            updateCallBack.onUpdateFailure();
                        }
                        System.out.println("应答：" + jsonObject);
                    }catch (Exception e){
                        e.printStackTrace();
                        updateCallBack.onUpdateFailure();
                    }
                }
            });
        }
    }

    private String parseMail(Mail mail) throws EncoderException, UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        Base64 base64 = new Base64();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String date = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        stringBuilder.append("Date: ");
        stringBuilder.append(new Date(System.currentTimeMillis()));
        stringBuilder.append("\r\n");
        stringBuilder.append("Subject: ");
        if (mail.getSubject().trim().length()>0) {
            stringBuilder.append("=?UTF-8?B?" +
                    new String(base64.encode(mail.getSubject().getBytes("UTF-8")),
                            "UTF-8") + "?=");
        }else{
            stringBuilder.append("");
        }
        stringBuilder.append("\r\n");
        stringBuilder.append("From: ");
        stringBuilder.append("=?UTF-8?B?"+
                new String(base64.encode(mail.getFrom().getBytes("UTF-8")),
                        "UTF-8")+"?=");
        stringBuilder.append(" <");
        stringBuilder.append(mail.getFromEmail());
        stringBuilder.append(">");
        stringBuilder.append("\r\n");
        stringBuilder.append("To: ");
        stringBuilder.append("=?UTF-8?B?"+
                new String(base64.encode(mail.getTo().getBytes("UTF-8")),
                        "UTF-8")+"?=");
        stringBuilder.append(" <");
        stringBuilder.append(mail.getToEmail());
        stringBuilder.append(">");
        stringBuilder.append("\r\nContent-Type:multipart/related;\r\n" +
                "\tboundary=\"----=_001_NextPart884423485678_=----\"\r\n");
        stringBuilder.append("\r\n");
        stringBuilder.append("This is a multi-part message in MIME format.\r\n\r\n");

        stringBuilder.append("------=_001_NextPart884423485678_=----\r\n" +
                "Content-Type: multipart/alternative;\r\n" +
                "\tboundary=\"----=_002_NextPart035243843125_=----\"\r\n\r\n");

        stringBuilder.append("------=_002_NextPart035243843125_=----\r\n" +
                "Content-Type: text/html;\r\n" +
                "\tcharset=\"utf-8\"\r\n" +
                "Content-Transfer-Encoding: base64\r\n\r\n");
        List<String> base64Images = new ArrayList<String>();
        Document parse = Jsoup.parseBodyFragment(new String(mail.getData()));
        Elements imgs = parse.getElementsByTag("img");
        int i = 0;

        for(Element img : imgs){
            System.out.println("原来的src："+img.attr("src"));
            int jj = img.attr("src").indexOf(',');
            base64Images.add(img.attr("src").substring(jj+1).replace(" ","+"));
            img.attr("src", "cid:Shihuan_Cid"+(i++));
            System.out.println("替换了一个src");
        }
        String base64String = android.util.Base64.encodeToString(
                parse.html().getBytes("UTF-8"), android.util.Base64.DEFAULT);
//        String base64String = new String(base64.encode(parse.html().getBytes("UTF-8")),"UTF-8");
//        System.out.println("Here："+base64String);
        stringBuilder.append(base64String);
        stringBuilder.append("\r\n\r\n"+"------=_002_NextPart035243843125_=------\r\n");

        for(int j = i-1 ; j >= 0;j --) {
            stringBuilder.append("\r\n" +
                    "------=_001_NextPart884423485678_=----\r\n");
            stringBuilder.append("Content-Type: image/jpeg;\r\n" +
                    "\tname=\""+j+".jpg\"\r\n");
            stringBuilder.append("Content-Transfer-Encoding: base64\r\n");
            stringBuilder.append("Content-ID: <Shihuan_Cid"+j+">\r\n\r\n");
            byte[] bytes = base64Images.get(j).getBytes("UTF-8");
            int l = bytes.length;
            int perFrame = 3000;
            int jj = 0;
            StringBuilder stringBuilder1 = new StringBuilder();
            while (jj + perFrame < l){
                stringBuilder1.append(new String(Arrays.copyOfRange(bytes,jj,jj + perFrame)));
                stringBuilder1.append("\r\n");
                jj = jj + perFrame;
            }
            stringBuilder1.append(new String(Arrays.copyOfRange(bytes,jj,l)));
            stringBuilder1.append("\r\n");
            System.out.println("提取出来的src:"+stringBuilder1.toString());
            stringBuilder.append(stringBuilder1.toString()+"\r\n\r\n");
        }
        stringBuilder.append("------=_001_NextPart884423485678_=------\r\n");
//        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    private String parseMailWithAttachment(Mail mail) throws EncoderException,
            UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        Base64 base64 = new Base64();
        stringBuilder.append("Date: ");
        stringBuilder.append(new Date(System.currentTimeMillis()));
        stringBuilder.append("\r\n");
        stringBuilder.append("Subject: ");
        if (mail.getSubject().trim().length()>0) {
            stringBuilder.append("=?UTF-8?B?" +
                    new String(base64.encode(mail.getSubject().getBytes("UTF-8")),
                            "UTF-8") + "?=");
        }else{
            stringBuilder.append("");
        }
        stringBuilder.append("\r\n");
        stringBuilder.append("From: ");
        stringBuilder.append("=?UTF-8?B?"+
                new String(base64.encode(mail.getFrom().getBytes("UTF-8")),
                        "UTF-8")+"?=");
        stringBuilder.append(" <");
        stringBuilder.append(mail.getFromEmail());
        stringBuilder.append(">");
        stringBuilder.append("\r\n");
        stringBuilder.append("To: ");
        stringBuilder.append("=?UTF-8?B?"+
                new String(base64.encode(mail.getTo().getBytes("UTF-8")),
                        "UTF-8")+"?=");
        stringBuilder.append(" <");
        stringBuilder.append(mail.getToEmail());
        stringBuilder.append(">");
        stringBuilder.append("\r\nContent-Type:multipart/mixed;\r\n" +
                "\tboundary=\"----=_001_NextPart724032025785_=----\"\r\n");
        stringBuilder.append("\r\n");
        stringBuilder.append("This is a multi-part message in MIME format.\r\n\r\n");

        stringBuilder.append("------=_001_NextPart724032025785_=----\r\n" +
                "Content-Type: multipart/related;\r\n" +
                "\tboundary=\"----=_002_NextPart807417080148_=----\"\r\n\r\n");

        stringBuilder.append("------=_002_NextPart807417080148_=----\r\n" +
                "Content-Type: multipart/alternative;\r\n" +
                "\tboundary=\"----=_003_NextPart032466352151_=----\"\r\n\r\n");

        stringBuilder.append("------=_003_NextPart032466352151_=----\r\n" +
                "Content-Type: text/html;\r\n" +
                "\tcharset=\"utf-8\"\r\n" +
                "Content-Transfer-Encoding: base64\r\n\r\n");
        List<String> base64Images = new ArrayList<String>();
        Document parse = Jsoup.parseBodyFragment(new String(mail.getData()));
        Elements imgs = parse.getElementsByTag("img");
        int i = 0;

        for(Element img : imgs){
            System.out.println("原来的src："+img.attr("src"));
            int jj = img.attr("src").indexOf(',');
            base64Images.add(img.attr("src").substring(jj+1).replace(" ","+"));
            img.attr("src", "cid:Shihuan_Cid"+(i++));
            System.out.println("替换了一个src");
        }

        String base64String = android.util.Base64.encodeToString(parse.html().getBytes("UTF-8")
                , android.util.Base64.DEFAULT);

        stringBuilder.append(base64String);
        stringBuilder.append("\r\n\r\n"+"------=_003_NextPart032466352151_=------\r\n");

        for(int j = i-1 ; j >= 0;j --) {
            stringBuilder.append("\r\n" +
                    "------=_002_NextPart807417080148_=----\r\n");
            stringBuilder.append("Content-Type: image/jpeg;\r\n" +
                    "\tname=\""+j+".jpg\"\r\n");
            stringBuilder.append("Content-Transfer-Encoding: base64\r\n");
            stringBuilder.append("Content-ID: <Shihuan_Cid"+j+">\r\n\r\n");
            byte[] bytes = base64Images.get(j).getBytes("UTF-8");
            int l = bytes.length;
            int perFrame = 3000;
            int jj = 0;
            StringBuilder stringBuilder1 = new StringBuilder();
            while (jj + perFrame < l){
                stringBuilder1.append(new String(Arrays.copyOfRange(bytes,jj,jj + perFrame)));
                stringBuilder1.append("\r\n");
                jj = jj + perFrame;
            }
            stringBuilder1.append(new String(Arrays.copyOfRange(bytes,jj,l)));
            stringBuilder1.append("\r\n");
            System.out.println("提取出来的src:"+stringBuilder1.toString());
            stringBuilder.append(stringBuilder1.toString()+"\r\n\r\n");
        }
        stringBuilder.append("------=_002_NextPart807417080148_=------\r\n\r\n");

        for(Attachment attachment : mail.getAttachmentList()) {
            stringBuilder.append("------=_001_NextPart724032025785_=----\r\n" +
                    "Content-Type: application/octet-stream;\r\n" +
                    "\tname=\""+attachment.getName()+"\"\r\n" +
                    "Content-Transfer-Encoding: base64\r\n");
            stringBuilder.append("Content-Disposition: attachment;\r\n" +
                    "\tfilename=\""+attachment.getName()+"\"\r\n\r\n");

            byte[] bytes = attachment.getBase64().getBytes("UTF-8");
            int l = bytes.length;
            int perFrame = 3000;
            int jj = 0;
            StringBuilder stringBuilder1 = new StringBuilder();
            while (jj + perFrame < l){
                stringBuilder1.append(new String(Arrays.copyOfRange(bytes,jj,jj + perFrame)));
                stringBuilder1.append("\r\n");
                jj = jj + perFrame;
            }
            stringBuilder1.append(new String(Arrays.copyOfRange(bytes,jj,l)));
            stringBuilder1.append("\r\n");
            stringBuilder.append(stringBuilder1.toString());
            stringBuilder.append("\r\n\r\n");
        }
        stringBuilder.append("------=_001_NextPart724032025785_=------\r\n\r\n");
//        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }
    /**
     * 星标邮件
     */
    @Override
    public void setMailFlag(Mail mail,@NonNull UpdateCallBack updateCallBack){
        String userName = mail.getUserAddress();
        String strSql = "update " + mailDao.getTablename() +
                " set " + MailDao.Properties.Flag.columnName + " = " + mail.getFlag()
                + " where " + MailDao.Properties.Uid.columnName + "='" + mail.getUid()+
                "' and " + MailDao.Properties.MailBox.columnName + "= '" + mail.getMailBox() +
                "' and " + MailDao.Properties.UserAddress.columnName + " = '" +
                userName+ "'";
        daoSession.getDatabase().execSQL(strSql);
        updateCallBack.onUpdateSuccess();
    }

    /**
     * 设已读标志
     */
    @Override
    public void setMailSeen(Mail mail,@NonNull UpdateCallBack updateCallBack){
        String userName = mail.getUserAddress();
        String strSql = "update " + mailDao.getTablename() +
                " set " + MailDao.Properties.Seen.columnName + " = " + mail.getSeen()
                + " where " + MailDao.Properties.Uid.columnName + "='" + mail.getUid()+
                "' and " + MailDao.Properties.MailBox.columnName + "= '" + mail.getMailBox() +
                "' and " + MailDao.Properties.UserAddress.columnName + " = '" +
                userName+ "'";
        daoSession.getDatabase().execSQL(strSql);
        updateCallBack.onUpdateSuccess();
    }
}
