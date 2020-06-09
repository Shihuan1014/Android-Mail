package edu.hnu.mail;

import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.util.LineInputStream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

import edu.hnu.mail.constant.UserInfo;
import edu.hnu.mail.data.dao.MailDao;
import edu.hnu.mail.data.entity.Attachment;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.data.source.remote.DotTerminatedMessageReader;
import edu.hnu.mail.data.source.remote.MultiPart;
import edu.hnu.mail.data.source.remote.POP3;
import edu.hnu.mail.data.source.remote.POP3Client;
import edu.hnu.mail.data.source.remote.POP3MessageInfo;
import edu.hnu.mail.util.QuotedPrintable;

import org.apache.commons.codec.binary.Base64;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    /**
     * 理解final
     */
    @Test
    public void test(){

        //定义一个变量
        List list = new ArrayList();
        //定义一个引用
        List list1 = list;

        for(int i = 0;i < 10;i ++){
            //定义一个被final定义的引用，意味着这个引用只能赋值一次
            final List list2 = list;
            List list3 = list;
            list.add(i);
            System.out.println("List的长度为：" + list.size());
            System.out.println("List1的长度为：" + list1.size() + " hashCode: "+list1.hashCode());
            System.out.println("List2的长度为：" + list2.size() + " hashCode: "+list2.hashCode());
            System.out.println("List3的长度为：" + list3.size() + " hashCode: "+list3.hashCode());
        }
    }

//    @Test
//    public void pop(){
//        POP3Client p = POP3Client.getInstance();
//        try{
//            String userAddress = "user@shihuan.site";
////            p.connect("pop.qq.com",110);
////            p.login("1075515629@qq.com","vmszctfrcpgsjjgh");
//            p.connect("shihuan.site",110);
//            p.login(userAddress,"1234");
//            POP3MessageInfo[] list = p.listMessages();
//            for (POP3MessageInfo pop3MessageInfo : list){
//                System.out.println("正在收取第 "+pop3MessageInfo.number+"封邮件");
//                edu.hnu.mail.data.source.remote.Mail mail = p.retrMail(p,pop3MessageInfo.number,userAddress);
//                System.out.println(mail);
//            }
//            p.disconnect();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
    @Test
    public void popTest() throws MessagingException, IOException {

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "pop3");		// 协议
        props.setProperty("mail.pop3.port", "110");				// 端口
        props.setProperty("mail.pop3.host", "pop.qq.com");

        Session session = Session.getInstance(props);
        Store store = session.getStore("pop3");
        store.connect("1075515629@qq.com","vmszctfrcpgsjjgh");
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        POP3Folder inbox = (POP3Folder) folder;
        Message[] messages = folder.getMessages();
        parseMessage(messages);
        for (Message message : messages){
            MimeMessage mimeMessage = (MimeMessage) message;
            String uid = inbox.getUID(mimeMessage);
            System.out.println(uid);
            if(uid == null){
                String messageId = mimeMessage.getMessageID();
                System.out.println(messageId);
            }

        }
    }

    /**
     * 解析邮件
     * @param messages 要解析的邮件列表
     */
    public static void parseMessage(Message ...messages) throws MessagingException, IOException {
        if (messages == null || messages.length < 1)
            throw new MessagingException("未找到要解析的邮件!");

        // 解析所有邮件
        for (int i = 0, count = messages.length; i < count; i++) {
            MimeMessage msg = (MimeMessage) messages[i];
            Mail mail = new Mail();
            System.out.println("------------------解析第" + msg.getMessageNumber() + "封邮件-------------------- ");
            System.out.println("主题: " + getSubject(msg));
            mail.setSubject(getSubject(msg));
            String[] s = getFrom(msg);
            System.out.println("发件人: " + s[0]);
            mail.setFrom(s[0]);
            mail.setFromEmail(s[1]);
            System.out.println("收件人：" + getReceiveAddress(msg, null));
            System.out.println("发送时间：" + getSentDate(msg, null));
            System.out.println("是否已读：" + isSeen(msg));
            System.out.println("邮件优先级：" + getPriority(msg));
            System.out.println("是否需要回执：" + isReplySign(msg));
            System.out.println("邮件大小：" + msg.getSize() * 1024 + "kb");
            boolean isContainerAttachment = isContainAttachment(msg);
            System.out.println("是否包含附件：" + isContainerAttachment);
            if (isContainerAttachment) {
                System.out.println("发现附件");
//                saveAttachment(msg, "c:\\mailtmp\\"+msg.getSubject() + "_"); //保存附件
            }
            StringBuffer content = new StringBuffer(30);
            getMailTextContent(msg, content);
            System.out.println("邮件正文：" + (content.length() > 100 ? content.substring(0,100) + "..." : content));
            System.out.println("------------------第" + msg.getMessageNumber() + "封邮件解析结束-------------------- ");
            System.out.println();
        }
    }

    /**
     * 获得邮件主题
     * @param msg 邮件内容
     * @return 解码后的邮件主题
     */
    public static String getSubject(MimeMessage msg) throws UnsupportedEncodingException, MessagingException {
        return MimeUtility.decodeText(msg.getSubject());
    }

    /**
     * 获得邮件发件人
     * @param msg 邮件内容
     * @return 姓名 <Email地址>
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static String[] getFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        String[] from = new String[2];
        Address[] froms = msg.getFrom();
        if (froms.length < 1)
            throw new MessagingException("没有发件人!");

        InternetAddress address = (InternetAddress) froms[0];
        String person = address.getPersonal();
        if (person != null) {
            person = MimeUtility.decodeText(person) + " ";
        } else {
            person = "";
        }
        from[0] = person;
        from[1] = address.getAddress();
        return from;
    }

    /**
     * 根据收件人类型，获取邮件收件人、抄送和密送地址。如果收件人类型为空，则获得所有的收件人
     * <p>Message.RecipientType.TO  收件人</p>
     * <p>Message.RecipientType.CC  抄送</p>
     * <p>Message.RecipientType.BCC 密送</p>
     * @param msg 邮件内容
     * @param type 收件人类型
     * @return 收件人1 <邮件地址1>, 收件人2 <邮件地址2>, ...
     * @throws MessagingException
     */
    public static String getReceiveAddress(MimeMessage msg, Message.RecipientType type) throws MessagingException {
        StringBuffer receiveAddress = new StringBuffer();
        Address[] addresss = null;
        if (type == null) {
            addresss = msg.getAllRecipients();
        } else {
            addresss = msg.getRecipients(type);
        }

        if (addresss == null || addresss.length < 1)
            throw new MessagingException("没有收件人!");
        for (Address address : addresss) {
            InternetAddress internetAddress = (InternetAddress)address;
            receiveAddress.append(internetAddress.toUnicodeString()).append(",");
        }

        receiveAddress.deleteCharAt(receiveAddress.length()-1);	//删除最后一个逗号

        return receiveAddress.toString();
    }

    /**
     * 获得邮件发送时间
     * @param msg 邮件内容
     * @return yyyy年mm月dd日 星期X HH:mm
     * @throws MessagingException
     */
    public static String getSentDate(MimeMessage msg, String pattern) throws MessagingException {
        Date receivedDate = msg.getSentDate();
        if (receivedDate == null)
            return "";

        if (pattern == null || "".equals(pattern))
            pattern = "yyyy年MM月dd日 E HH:mm ";

        return new SimpleDateFormat(pattern).format(receivedDate);
    }

    /**
     * 判断邮件中是否包含附件
     * @param part 邮件内容
     * @return 邮件中存在附件返回true，不存在返回false
     * @throws MessagingException
     * @throws IOException
     */
    public static boolean isContainAttachment(Part part) throws MessagingException, IOException {
        boolean flag = false;
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    flag = true;
                } else if (bodyPart.isMimeType("multipart/*")) {
                    flag = isContainAttachment(bodyPart);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("application") != -1) {
                        flag = true;
                    }

                    if (contentType.indexOf("name") != -1) {
                        flag = true;
                    }
                }

                if (flag) break;
            }
        } else if (part.isMimeType("message/rfc822")) {
            flag = isContainAttachment((Part)part.getContent());
        }
        return flag;
    }

    /**
     * 判断邮件是否已读
     * @param msg 邮件内容
     * @return 如果邮件已读返回true,否则返回false
     * @throws MessagingException
     */
    public static boolean isSeen(MimeMessage msg) throws MessagingException {
        return msg.getFlags().contains(Flags.Flag.SEEN);
    }

    /**
     * 判断邮件是否需要阅读回执
     * @param msg 邮件内容
     * @return 需要回执返回true,否则返回false
     * @throws MessagingException
     */
    public static boolean isReplySign(MimeMessage msg) throws MessagingException {
        boolean replySign = false;
        String[] headers = msg.getHeader("Disposition-Notification-To");
        if (headers != null)
            replySign = true;
        return replySign;
    }

    /**
     * 获得邮件的优先级
     * @param msg 邮件内容
     * @return 1(High):紧急  3:普通(Normal)  5:低(Low)
     * @throws MessagingException
     */
    public static String getPriority(MimeMessage msg) throws MessagingException {
        String priority = "普通";
        String[] headers = msg.getHeader("X-Priority");
        if (headers != null) {
            String headerPriority = headers[0];
            if (headerPriority.indexOf("1") != -1 || headerPriority.indexOf("High") != -1)
                priority = "紧急";
            else if (headerPriority.indexOf("5") != -1 || headerPriority.indexOf("Low") != -1)
                priority = "低";
            else
                priority = "普通";
        }
        return priority;
    }

    /**
     * 获得邮件文本内容
     * @param part 邮件体
     * @param content 存储邮件文本内容的字符串
     * @throws MessagingException
     * @throws IOException
     */
    public static void getMailTextContent(Part part, StringBuffer content) throws MessagingException, IOException {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part)part.getContent(),content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart,content);
            }
        }
    }

    /**
     * 保存附件
     * @param part 邮件中多个组合体中的其中一个组合体
     * @param destDir  附件保存目录
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveAttachment(Part part, String destDir) throws UnsupportedEncodingException, MessagingException,
            FileNotFoundException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();	//复杂体邮件
            //复杂体邮件包含多个邮件体
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                //获得复杂体邮件中其中一个邮件体
                BodyPart bodyPart = multipart.getBodyPart(i);
                //某一个邮件体也有可能是由多个邮件体组成的复杂体
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    InputStream is = bodyPart.getInputStream();
                    saveFile(is, destDir, decodeText(bodyPart.getFileName()));
                } else if (bodyPart.isMimeType("multipart/*")) {
                    saveAttachment(bodyPart,destDir);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("name") != -1 || contentType.indexOf("application") != -1) {
                        saveFile(bodyPart.getInputStream(), destDir, decodeText(bodyPart.getFileName()));
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachment((Part) part.getContent(),destDir);
        }
    }

    /**
     * 读取输入流中的数据保存至指定目录
     * @param is 输入流
     * @param fileName 文件名
     * @param destDir 文件存储目录
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void saveFile(InputStream is, String destDir, String fileName)
            throws FileNotFoundException, IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(new File(destDir + fileName)));
        int len = -1;
        while ((len = bis.read()) != -1) {
            bos.write(len);
            bos.flush();
        }
        bos.close();
        bis.close();
    }

    /**
     * 文本解码
     * @param encodeText 解码MimeUtility.encodeText(String text)方法编码后的文本
     * @return 解码后的文本
     * @throws UnsupportedEncodingException
     */
    public static String decodeText(String encodeText) throws UnsupportedEncodingException {
        if (encodeText == null || "".equals(encodeText)) {
            return "";
        } else {
            return MimeUtility.decodeText(encodeText);
        }
    }

    @Test
    public void ttt() throws UnsupportedEncodingException {
            String s = ".\r\n";
            String ss = ".\r\n";
        System.out.println(s.equalsIgnoreCase(ss));
    }



    public String decodeMailBase64(String mailText) throws UnsupportedEncodingException {
        Matcher matcher = Pattern.compile("=\\?(.*)\\?(\\S)\\?(.*)\\?=").matcher(mailText);
        if(matcher.find()){
            String charset = matcher.group(1);
            String encoding = matcher.group(2);
            String content = matcher.group(3);
            Base64 base64 = new Base64();
            if(encoding.equalsIgnoreCase("B")){
                return  new String(base64.decode(content.getBytes(charset)),charset);
            }else if(encoding.equalsIgnoreCase("Q")){
                return  qpDecoding(content);
            }
        }
        return mailText;
    }

    /**
     * 工具，解码quoted-printable
     * @param str
     * @return
     */
    public final String qpDecoding(String str) {
        if (str == null)
        {
            return "";
        }
        try
        {
            str = str.replaceAll("=\n", "");
            byte[] bytes = str.getBytes("US-ASCII");
            for (int i = 0; i < bytes.length; i++)
            {
                byte b = bytes[i];
                if (b != 95)
                {
                    bytes[i] = b;
                }
                else
                {
                    bytes[i] = 32;
                }
            }
            if (bytes == null)
            {
                return "";
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (int i = 0; i < bytes.length; i++)
            {
                int b = bytes[i];
                if (b == '=')
                {
                    try
                    {
                        int u = Character.digit((char) bytes[++i], 16);
                        int l = Character.digit((char) bytes[++i], 16);
                        if (u == -1 || l == -1)
                        {
                            continue;
                        }
                        buffer.write((char) ((u << 4) + l));
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    buffer.write(b);
                }
            }
            return new String(buffer.toByteArray(), "GBK");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    @Test
    public void pop3ClientParseMailTest() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        String ss = "X-QQ-FEAT: /dyzw3/77JCalRfF4sWXqIxOY+Ie41B6BEWN1HjwE4jXZW5jUWCWDoKyNxwcl\n" +
                "\tsMLO+0lWJoKbWAIggvr/xiE+bkoklAijpjKQmkxVmzmKdks5lTE4HNqRkAEgYpCKuZSQap/\n" +
                "\tsCKfnhyBvuJJNVgCmpieVIz8QE7ryjEKODY473Xjev2Qx9e9lstjeu2zhCwISlzt9jLlNRp\n" +
                "\tgwTS44jkHeqFZQJpd4EEQKMpbSrIK3EGr4rapUcWm5Q3RMdAstMLSQzb9qqvq8OaYfJya8b\n" +
                "\tgYOM8q+fbNmivr4xlLtLZC9Xk=\n" +
                "X-QQ-SSF: 0001000000000010\n" +
                "X-QQ-WAPMAIL: 1\n" +
                "X-QQ-BUSINESS-ORIGIN: 2\n" +
                "X-Originating-IP: 116.11.31.239\n" +
                "X-QQ-STYLE: \n" +
                "X-QQ-mid: riamail60t1587376533t30950\n" +
                "From: \"=?utf-8?B?WmhvdQ==?=\" <280294671@qq.com>\n" +
                "To: \"=?utf-8?B?U2hpaHVhbg==?=\" <1075515629@qq.com>\n" +
                "Subject: hello\n" +
                "Mime-Version: 1.0\n" +
                "Content-Type: multipart/alternative;\n" +
                "\tboundary=\"----=_NextPart_5E9D7195_0EBB98A0_604C042A\"\n" +
                "Content-Transfer-Encoding: 8Bit\n" +
                "Date: Mon, 20 Apr 2020 17:55:33 +0800\n" +
                "X-Priority: 3\n" +
                "Message-ID: <tencent_281A6149642256A285B5FA7BCC4ABB5EA30A@qq.com>\n" +
                "X-QQ-MIME: TCMime 1.0 by Tencent\n" +
                "X-Mailer: QQMail 2.x\n" +
                "X-QQ-Mailer: QQMail 2.x\n" +
                "\n" +
                "This is a multi-part message in MIME format.\n" +
                "\n" +
                "------=_NextPart_5E9D7195_0EBB98A0_604C042A\n" +
                "Content-Type: text/plain;\n" +
                "\tcharset=\"utf-8\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "5Y+R5Y+R5Y+R5bCx5Y6755yL5byA54K55ZOm6aOe6KaFDQrpuKHohb/ogonlk6boj7Loj7Jp\n" +
                "ZmlpaWblsLHmm7TliqANCg0KDQrnvLTotLnmnLropoU=\n" +
                "\n" +
                "------=_NextPart_5E9D7195_0EBB98A0_604C042A\n" +
                "Content-Type: text/html;\n" +
                "\tcharset=\"utf-8\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "PG1ldGEgaHR0cC1lcXVpdj0iQ29udGVudC1UeXBlIiBjb250ZW50PSJ0ZXh0L2h0bWw7IGNo\n" +
                "YXJzZXQ9VVRGLTgiPjxibG9ja3F1b3RlIHN0eWxlPSJtYXJnaW46IDBweCAwcHggMTBweDsg\n" +
                "cGFkZGluZzogMTRweCAxNnB4OyBjb2xvcjogcmdiKDE0OSwgMTQ5LCAxNDkpOyBiYWNrZ3Jv\n" +
                "dW5kLWNvbG9yOiByZ2IoMjQ1LCAyNDYsIDI1MCk7Ij7lj5Hlj5Hlj5HlsLHljrvnnIvlvIDn\n" +
                "grk8L2Jsb2NrcXVvdGU+PGJsb2NrcXVvdGUgc3R5bGU9Im1hcmdpbjogMHB4IDBweCAxMHB4\n" +
                "OyBwYWRkaW5nOiAxNHB4IDE2cHg7IGNvbG9yOiByZ2IoMTQ5LCAxNDksIDE0OSk7IGJhY2tn\n" +
                "cm91bmQtY29sb3I6IHJnYigyNDUsIDI0NiwgMjUwKTsiPjxmb250IHNpemU9IjUiPuWTpumj\n" +
                "nuimhTwvZm9udD48L2Jsb2NrcXVvdGU+PGJsb2NrcXVvdGUgc3R5bGU9Im1hcmdpbjogMHB4\n" +
                "IDBweCAxMHB4OyBwYWRkaW5nOiAxNHB4IDE2cHg7IGNvbG9yOiByZ2IoMTQ5LCAxNDksIDE0\n" +
                "OSk7IGJhY2tncm91bmQtY29sb3I6IHJnYigyNDUsIDI0NiwgMjUwKTsiPjx1bD48bGk+PGZv\n" +
                "bnQgc2l6ZT0iNSI+6bih6IW/6IKJPC9mb250Pjxmb250IGNvbG9yPSIjZjY0ZTRmIj48Zm9u\n" +
                "dCBzaXplPSI1Ij7lk6boj7Loj7JpZjwvZm9udD48Zm9udCBzaXplPSIzIj5paWlm5bCx5pu0\n" +
                "5YqgPC9mb250PjwvZm9udD48YnI+PC9saT48L3VsPjxmb250IGNvbG9yPSIjZjY0ZTRmIj48\n" +
                "L2ZvbnQ+PGJyPjxmb250IGNvbG9yPSIjZjY0ZTRmIj48L2ZvbnQ+PC9ibG9ja3F1b3RlPjxi\n" +
                "bG9ja3F1b3RlIHN0eWxlPSJtYXJnaW46IDBweCAwcHggMTBweDsgcGFkZGluZzogMTRweCAx\n" +
                "NnB4OyBjb2xvcjogcmdiKDE0OSwgMTQ5LCAxNDkpOyBiYWNrZ3JvdW5kLWNvbG9yOiByZ2Io\n" +
                "MjQ1LCAyNDYsIDI1MCk7Ij7nvLTotLnmnLropoU8L2Jsb2NrcXVvdGU+PGRpdj48IS0tZW1w\n" +
                "dHlzaWduLS0+PC9kaXY+\n" +
                "\n" +
                "------=_NextPart_5E9D7195_0EBB98A0_604C042A--";
                stringBuilder.append(ss);
        POP3Client pop3Client = POP3Client.getInstance(null);
        pop3Client.parseMail(pop3Client,new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stringBuilder.toString().getBytes()))));
    }
}