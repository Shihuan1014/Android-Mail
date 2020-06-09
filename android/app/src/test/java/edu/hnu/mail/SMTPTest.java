package edu.hnu.mail;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import edu.hnu.mail.data.source.remote.POP3Client;
import edu.hnu.mail.data.source.remote.SMTPClient;

public class SMTPTest {

    @Test
    public void smtpLogin() throws IOException {
        SMTPClient smtpClient = new SMTPClient();
        smtpClient.connect("shihuan.site",25);
        if(smtpClient.isConnected()){
            System.out.println("connected");
            if(smtpClient.login("163.com")){
                if(smtpClient.authLogin("user@shihuan.site","1234")){
                    if(smtpClient.sendSimpleMessage("user@shihuan.site",
                            "user2@shihuan.site","Subject: Hello\r\nFrom: Shihuan\r\nTo: nmsl\r\n\r\nHello From Android")){
                        System.out.println("发送成功");
                        smtpClient.disconnect();
                    }else {
                        System.out.println("发送失败");
                    }
                }
            }
        }
    }

    @Test
    public void base64(){
        String s = System.getProperty("line.separator");
        String ss = "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";
        System.out.println(ss.length());
    }

    @Test
    public void test(){
        POP3Client pop3Client = POP3Client.getInstance(null);
        String s = "https://www.eclipse.org/=\n" +
                "community/eclipse_newsletter/2020/april/images/thabang.png";
        s = pop3Client.qpDecoding(s,"UTF-8");
        System.out.println(s);
    }

    @Test
    public void base64Test() throws UnsupportedEncodingException {
        String s = "PGh0bWw+CiA8aGVhZD48L2hlYWQ+CiA8Ym9keT4gICAKICA8bWV0YSBuYW1lPSJ2aWV3cG9ydCIgY29udGVudD0id2lkdGg9ZGV2aWNlLXdpZHRoLCBpbml0aWFsLXNjYWxlPTEuMCwgbWluaW11bS1zY2FsZT0wLjUsIG1heGltdW0tc2NhbGU9Mi4wLCB1c2VyLXNjYWxhYmxlPXllcyI+ICAKICA8ZGl2IHN0eWxlPSJmb250LXNpemU6MjBweCI+CiAgIDxtZXRhIGh0dHAtZXF1aXY9IkNPTlRFTlQtVFlQRSIgY29udGVudD0iVEVYVC9IVE1MOyBDSEFSU0VUPVVURi04Ij4KICAgPGRpdiBzdHlsZT0iVEVYVC1BTElHTjogQ0VOVEVSOyI+CiAgICDlj5Hlj5Hlj5HlsLEKICAgIDxmb250IGNvbG9yPSIjRkYwMDAwIj48Yj5GRkZGRkY8L2I+PC9mb250PgogICA8L2Rpdj4KICAgPGRpdj4KICAgIDxicj4KICAgPC9kaXY+CiAgIDxkaXY+CiAgICA8ZGl2PgogICAgIDxpbWcgc3JjPSJjaWQ6U2hpaHVhbl9DaWQwIiB3aWR0aD0iIiBoZWlnaHQ9IiIgZGF0YS1hZGRpdGlvbj0iIiBhbHQ9IklNQUdFIj4KICAgICA8YnI+CiAgICAgPGJyPgogICAgPC9kaXY+CiAgIDwvZGl2PgogICA8ZGl2PgogICAgPCEtLUVNUFRZU0lHTi0tPgogICA8L2Rpdj4KICA8L2Rpdj4gCiA8L2JvZHk+CjwvaHRtbD4=";
        Base64 base64 = new Base64();
        String ss = new String(base64.decode(s.getBytes("UTF-8")));
        System.out.println(ss);
    }
}
