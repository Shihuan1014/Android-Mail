package edu.hnu.mail.data.source.remote;

import android.content.Context;

import java.io.IOException;

public class POP3LoginTest extends POP3 {

    public static POP3LoginTest pop3LoginTest;

    public static POP3LoginTest getInstance(){
        if (pop3LoginTest == null){
            pop3LoginTest = new POP3LoginTest();
        }
        return pop3LoginTest;
    }

    /**
     * 发送登录
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public boolean login(String username, String password) throws IOException
    {

        int code = sendCommand(POP3Command.USER, username);
        System.out.println("登录状态码：" + code);
        if (code != POP3Reply.OK)
            return false;

        code = sendCommand(POP3Command.PASS, password);
        System.out.println("登录状态码：" + code);
        if (code != POP3Reply.OK)
            return false;

        return true;
    }
}
