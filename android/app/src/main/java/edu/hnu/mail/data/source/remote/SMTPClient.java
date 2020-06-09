package edu.hnu.mail.data.source.remote;

import org.apache.commons.codec.binary.Base64;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetAddress;

public class SMTPClient extends SMTP{


    public static SMTPClient smtpClient;
    /**
     * Default SMTPClient constructor.  Creates a new SMTPClient instance.
     */
    public SMTPClient() {  }

    public static SMTPClient getInstance(){
        if(smtpClient==null){
            smtpClient = new SMTPClient();
        }
        return smtpClient;
    }


    /**
     * 设置编码
     * @param encoding
     */
    public SMTPClient(String encoding) {
        super(encoding);
    }


    /**
     * 看服务器返回码，200-300间的返回码为不成功
     * @return
     * @throws IOException
     */
    public boolean completePendingCommand() throws IOException
    {
        return SMTPReply.isPositiveCompletion(getReplyAndCode());
    }


    /**
     * helo指令
     * @param hostname
     * @return
     * @throws IOException
     */
    public boolean login(String hostname) throws IOException
    {
        return SMTPReply.isPositiveCompletion(helo(hostname));
    }


    /**
     * helo指令，自动从Socket获取ip，填入helo的参数
     * @return
     * @throws IOException
     */
    public boolean login() throws IOException
    {
        String name;
        InetAddress host;

        host = getLocalAddress();
        name = host.getHostName();

        if (name == null)
            return false;

        return SMTPReply.isPositiveCompletion(helo(name));
    }


    /**
     * auth login 指令
     * @return
     * @throws IOException
     */
    public boolean authLogin(String userName,String password) throws IOException
    {
        Base64 base64 = new Base64();
        String user = new String(base64.encode(userName.getBytes("UTF-8")),"UTF-8");
        String pass = new String(base64.encode(password.getBytes("UTF-8")),"UTF-8");
        System.out.println(user + " " + pass);
        return SMTPReply.isPositiveCompletion(
                auth(user, pass));
    }


    /**
     * MAIL FROM指令，这是邮件事务的开端
     * @param path  多个地址，这个RelayPath会帮忙封装成 <xxx@xx.com,xxx@xx.com> 的形式
     * @return
     * @throws IOException
     */
    public boolean setSender(RelayPath path) throws IOException
    {
        return SMTPReply.isPositiveCompletion(mail(path.toString()));
    }


    /**
     * MAIL FROM:<xxx@xx.com>
     * @param address
     * @return
     * @throws IOException
     */
    public boolean setSender(String address) throws IOException
    {
        return SMTPReply.isPositiveCompletion(mail("<" + address + ">"));
    }


    /**
     * RCPT TO:<>
     * @param path 多个地址，这个RelayPath会帮忙封装成 <xxx@xx.com,xxx@xx.com> 的形式
     * @return
     * @throws IOException
     */
    public boolean addRecipient(RelayPath path) throws IOException
    {
        return SMTPReply.isPositiveCompletion(rcpt(path.toString()));
    }


    /**
     * RCPT TO:<>
     * @param address
     * @return
     * @throws IOException
     */
    public boolean addRecipient(String address) throws IOException
    {
        return SMTPReply.isPositiveCompletion(rcpt("<" + address + ">"));
    }


    /**
     * 发送DATA指令，获取输出流句柄
     * @return
     * @throws IOException
     */
    public Writer sendMessageData() throws IOException
    {
        if (!SMTPReply.isPositiveIntermediate(data()))
            return null;

        return new DotTerminatedMessageWriter(writer);
    }


    /**
     * 发送DATA指令
     * @param message
     * @return
     * @throws IOException
     */
    public boolean sendShortMessageData(String message) throws IOException
    {
        Writer writer;
        writer = sendMessageData();
        if (writer == null)
            return false;

        writer.write(message);
        writer.close();

        return completePendingCommand();
    }


    /**
     * 发送邮件给单个recipient，相当于
     * @param sender 收件人地址
     * @param recipient
     * @param message
     * @return
     * @throws IOException
     */
    public boolean sendSimpleMessage(String sender, String recipient,
                                     String message)
            throws IOException
    {
        if (!setSender(sender))
            return false;

        if (!addRecipient(recipient))
            return false;

        return sendShortMessageData(message);
    }


    /**
     * 发送简单邮件，给多个recipients
     * @param sender
     * @param recipients
     * @param message
     * @return
     * @throws IOException
     */
    public boolean sendSimpleMessage(String sender, String[] recipients,
                                     String message)
            throws IOException
    {
        boolean oneSuccess = false;
        int count;

        if (!setSender(sender))
            return false;

        for (count = 0; count < recipients.length; count++)
        {
            if (addRecipient(recipients[count]))
                oneSuccess = true;
        }

        if (!oneSuccess)
            return false;

        return sendShortMessageData(message);
    }


    /**
     * 发送quit指令，退出
     * @return
     * @throws IOException
     */
    public boolean logout() throws IOException
    {
        return SMTPReply.isPositiveCompletion(quit());
    }


    /**
     * 发送noop指令，维持连接
     * @return
     * @throws IOException
     */
    public boolean sendNoOp() throws IOException
    {
        return SMTPReply.isPositiveCompletion(noop());
    }
}
