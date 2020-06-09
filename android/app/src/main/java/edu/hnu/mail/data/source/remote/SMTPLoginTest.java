package edu.hnu.mail.data.source.remote;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

public class SMTPLoginTest extends SMTP {

    public static SMTPLoginTest smtpClient;
    /**
     * Default SMTPClient constructor.  Creates a new SMTPClient instance.
     */
    public SMTPLoginTest() {  }

    public static SMTPLoginTest getInstance(){
        if(smtpClient==null){
            smtpClient = new SMTPLoginTest();
        }
        return smtpClient;
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
        return SMTPReply.isPositiveCompletion(
                auth(user, pass));
    }
}
