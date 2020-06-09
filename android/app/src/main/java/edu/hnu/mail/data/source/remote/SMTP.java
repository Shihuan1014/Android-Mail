package edu.hnu.mail.data.source.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Vector;

public class SMTP extends SocketClient{

    // SMTP 协议端口
    public static final int DEFAULT_PORT = 25;
    // 编码
//    private static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final String DEFAULT_ENCODING = "UTF-8";
    /** The encoding to use (user-settable) */
    private String encoding = DEFAULT_ENCODING;
    private boolean newReplyString = false;

    /*** 写相关 ***/
    // 封装OutputStream
    protected BufferedWriter writer;
    // 复用的StringBuffer ，优化内存
    protected  StringBuffer commandBuffer;
    /*** 读相关 ***/
    // 封装InputStream
    protected BufferedReader reader;
    // 应答码
    int replyCode;

    // 最后一行应答
    protected String lastReplyLine;
    // 全部应答
    protected Vector<String> replyLines;

    public SMTP(){
        setPort(DEFAULT_PORT);
        commandBuffer = new StringBuffer();
        reader = null;
        writer = null;
        replyLines = new Vector<String>();
    }

    public SMTP(String encoding) {
        this();
        this.encoding = encoding;
    }
    /**
     * 获得服务器返回
     * @throws IOException
     */
    protected void getReply() throws IOException
    {
        int length;

        newReplyString = true;
        replyLines.clear();

        String line = reader.readLine();

        if (line == null)
            throw new IOException(
                    "Connection closed without indication.");

        length = line.length();
        System.out.println("SMTP.java: 获得reply " + line);
        if (length < 3)
            throw new IOException(
                    "Truncated server reply: " + line);

        try
        {
            String code = line.substring(0, 3);
            replyCode = Integer.parseInt(code);
        }
        catch (NumberFormatException e)
        {
            throw new IOException(
                    "Could not parse response code.\nServer Reply: " + line);
        }

        replyLines.add(line);
        if (length > 3 && line.charAt(3) == '-')
        {
            do
            {
                line = reader.readLine();

                if (line == null)
                    throw new RuntimeException(
                            "Connection closed without indication.");

                replyLines.add(line);

            }
            while (!(line.length() >= 4 && line.charAt(3) != '-' &&
                    Character.isDigit(line.charAt(0))));
        }
        if (replyCode == SMTPReply.SERVICE_NOT_AVAILABLE)
            throw new RuntimeException(
                    "SMTP response 421 received.  Server closed connection.");
    }

    public int getReplyAndCode() throws IOException
    {
        getReply();
        return replyCode;
    }

    /**
     * Socket连接成功时执行
     * @throws IOException
     */
    @Override
    protected void onConnect() throws IOException
    {
        //父类SocketClient完成了对基础I/O的初始化
        super.onConnect();
        reader =
                new BufferedReader(new InputStreamReader(inputStream,
                        DEFAULT_ENCODING));
        writer =
                new BufferedWriter(new OutputStreamWriter(outputStream,
                        DEFAULT_ENCODING));
        //获取服务器应答
        getReply();
        // 设置当前状态为登录状态，意味着下一波输入就会被当成是用户密码
    }

    /**
     * 断开连接
     * @throws IOException
     */
    @Override
    public void disconnect() throws IOException
    {
        super.disconnect();
        reader = null;
        writer = null;
        lastReplyLine = null;
        replyLines.setSize(0);
        //设置为退出状态
    }

    /**
     * 这个command是字符串，就像用telnet一样
     * @param command
     * @param args
     * @return
     * @throws IOException
     */
    public int sendCommand(String command, String args) throws IOException
    {
        String message;
        // 重置复用的StringBuffer
        commandBuffer.setLength(0);
        //指令在前
        commandBuffer.append(command);
        if (args != null)
        {
            //加个空格，参数在后，比如 USER xxxxx
            commandBuffer.append(' ');
            commandBuffer.append(args);
        }
        //加换行符，这很重要
        commandBuffer.append(SocketClient.NETASCII_EOL);
        //往服务器写指令
        writer.write(message = commandBuffer.toString());
        writer.flush();
        //发完指令后就阻塞等应答,等到回复又广播给对此应答感兴趣的Listener
        getReply();
        //服务器应答的状态码
        return replyCode;
    }

    /**
     * 无参指令发送
     * @param command
     * @return
     * @throws IOException
     */
    public int sendCommand(String command) throws IOException
    {
        return sendCommand(command, null);
    }

    /**
     * 这个方法的command是一个代号，用起来可能轻松一些
     * @param command
     * @return
     * @throws IOException
     */
    public int sendCommand(int command, String args) throws IOException
    {
        return sendCommand(SMTPCommand.commands[command], args);
    }

    /**
     * 无参指令发送，command为整数代号
     * @param command
     * @return
     * @throws IOException
     */
    public int sendCommand(int command) throws IOException
    {
        return sendCommand(SMTPCommand.commands[command], null);
    }

    private int sendCommand(String command, String args, boolean includeSpace)
            throws IOException
    {
        String message;

        commandBuffer.setLength(0);
        commandBuffer.append(command);

        if (args != null)
        {
            if (includeSpace)
                commandBuffer.append(' ');
            commandBuffer.append(args);
        }

        commandBuffer.append(SocketClient.NETASCII_EOL);

        writer.write(message = commandBuffer.toString());
        writer.flush();
        getReply();
        return replyCode;
    }

    private int sendCommand(int command, String args, boolean includeSpace)
            throws IOException
    {
        return sendCommand(SMTPCommand.commands[command], args, includeSpace);
    }

    /**
     * Vector转数组
     * @return
     */
    public String[] getReplyStrings()
    {
        String[] lines;
        lines = new String[replyLines.size()];
        replyLines.copyInto(lines);
        return lines;
    }

    /**
     * 将获取到的应答所有的行拼成字符串
     * @return
     */
    public String getReplyString()
    {
        Enumeration<String> en;
        StringBuffer buffer = new StringBuffer(256);
        en = replyLines.elements();
        while (en.hasMoreElements())
        {
            buffer.append(en.nextElement());
            buffer.append(SocketClient.NETASCII_EOL);
        }
        return buffer.toString();
    }


    public int getReplyCode() {
        return replyCode;
    }

    /**
     * helo
     * @param hostname
     * @return
     * @throws IOException
     */
    public int helo(String hostname) throws IOException
    {
        return sendCommand(SMTPCommand.HELO, hostname);
    }

    protected int auth(String name,String pass) throws IOException
    {
        int i = sendCommand(SMTPCommand.AUTH_LOGIN);
        System.out.println(i);
        if(SMTPReply.isPositiveIntermediate(i)){
            i = sendCommand(name);
            System.out.println(i);
            if (SMTPReply.isPositiveIntermediate(i)){
                i = sendCommand(pass);
                System.out.println(i);
                if (SMTPReply.isPositiveCompletion(i)){
                    System.out.println("登录成功");
                    return SMTPReply.CODE_250;
                }
            }
        }
        return SMTPReply.CODE_535;
    }

    /**
     *
     * @param reversePath
     * @return
     * @throws IOException
     */
    public int mail(String reversePath) throws IOException
    {
        return sendCommand(SMTPCommand.MAIL, reversePath, false);
    }

    public int rcpt(String forwardPath) throws IOException
    {
        return sendCommand(SMTPCommand.RCPT, forwardPath, false);
    }

    public int data() throws IOException
    {
        return sendCommand(SMTPCommand.DATA);
    }

    public int send(String reversePath) throws IOException
    {
        return sendCommand(SMTPCommand.SEND, reversePath);
    }

    public int noop() throws IOException
    {
        return sendCommand(SMTPCommand.NOOP);
    }

    /**
     * QUIT指令
     * @return
     * @throws IOException
     */
    public int quit() throws IOException
    {
        return sendCommand(SMTPCommand.QUIT);
    }
}
