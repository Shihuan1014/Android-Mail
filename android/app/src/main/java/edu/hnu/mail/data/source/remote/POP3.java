package edu.hnu.mail.data.source.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

/**
 * POP3协议开发，实现指令发送和数据接收
 * 继承自SocketClient并封装I/O，我们需要按行(块)读取，走缓冲区
 * author: 周世焕
 * date: 2020/4/15
 */
public class POP3 extends SocketClient {

    // POP3 协议端口
    public static final int DEFAULT_PORT = 110;
    // OK状态码
    static final String OK = "+OK";
    // ERR状态码
    static final String ERROR = "-ERR";

    // POP协议状态之  未连接
    public static final int DISCONNECTED_STATE = -1;
    // POP协议状态之  连接了，可以登录
    public static final int AUTHORIZATION_STATE = 0;
    // POP协议状态之  登录了，可以做事
    public static final int TRANSACTION_STATE = 1;
    // POP协议状态之  做完了，提交事务(注意了，没提交前做的任何事情都不算数的)
    public static final int UPDATE_STATE = 2;
    // 编码
//    private static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final String DEFAULT_ENCODING = "UTF-8";
    // 当前POP进行到哪一步(状态)
    private int popState;
    /*** 写相关 ***/
    // 封装OutputStream
    protected  BufferedWriter writer;
    // 复用的StringBuffer ，优化内存
    protected  StringBuffer commandBuffer;
    /*** 读相关 ***/
    // 封装InputStream
    protected  BufferedReader reader;
    // 应答码
    int replyCode;

    // 最后一行应答
    protected String lastReplyLine;
    // 全部应答
    protected Vector<String> replyLines;
    // 事件管理
    private CommandListenerManager commandListenerManager;

    public POP3(){
        setPort(DEFAULT_PORT);
        commandBuffer = new StringBuffer();
        popState = DISCONNECTED_STATE;
        reader = null;
        writer = null;
        replyLines = new Vector<String>();
        commandListenerManager = new CommandListenerManager();
    }

    /**
     * 单行读取，对单行指令应答足够用了
     * @throws IOException
     */
    private void getReply() throws IOException
    {
        String line;

        // 清空读缓存，因为我们是按行读取的（BufferReader）
        replyLines.setSize(0);
        //进入read阻塞，按行读取，一次读一行
        line = reader.readLine();

        if (line == null)
            throw new IOException("长时间读取不到应答");
        if (line.startsWith(OK))
            replyCode = POP3Reply.OK;
        else if (line.startsWith(ERROR))
            replyCode = POP3Reply.ERROR;
        else{
            System.out.println("应答：" + line);
            while ((line = reader.readLine())!=null){
                System.out.println("应答：" + line);
                if (line.startsWith(OK)){
                    replyCode = POP3Reply.OK;
                    break;
                }
                else if (line.startsWith(ERROR)){
                    replyCode = POP3Reply.ERROR;
                    break;
                }
            }
//            throw new
//                    IOException(
//                    "服务器的应答有问题，不是以+OK或者-ERR开头");
        }

        if (commandListenerManager.getListenerCount() > 0)
            commandListenerManager.fireReplyReceived(replyCode,getReplyString());
        // 加入行缓存
        replyLines.addElement(line);
        // 最后一行往往用来判断是否是 应答码
        lastReplyLine = line;
    }

    /**
     * 多行读取，POP3是以英文句号来声明多行应答的结尾，故可用循环
     * @throws IOException
     */
    public void getAdditionalReply() throws IOException
    {
        String line;

        line = reader.readLine();
        while (line != null)
        {
            replyLines.addElement(line);
            if (line.equals("."))
                break;
            line = reader.readLine();
        }
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
        setState(AUTHORIZATION_STATE);
    }

    /***
     * Disconnects the client from the server, and sets the state to
     * <code> DISCONNECTED_STATE </code>.  The reply text information
     * from the last issued command is voided to allow garbage collection
     * of the memory used to store that information.
     * <p>
     * @exception IOException  If there is an error in disconnecting.
     ***/
    @Override
    public void disconnect() throws IOException
    {
        super.disconnect();
        reader = null;
        writer = null;
        lastReplyLine = null;
        replyLines.setSize(0);
        //设置为退出状态
        setState(DISCONNECTED_STATE);
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
        //加换行符，这很重要，POP3协议按行读取消息，每行以换行符结束
        commandBuffer.append(SocketClient.NETASCII_EOL);
        //往服务器写指令
        writer.write(message = commandBuffer.toString());
        writer.flush();
        //指令发送动作，广播给对此感兴趣的Listener
        if (commandListenerManager.getListenerCount() > 0)
            commandListenerManager.fireCommandSent(command, message);
        System.out.println("sendCommand:" + command);
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
        return sendCommand(POP3Command.commands[command], args);
    }

    /**
     * 无参指令发送，command为整数代号，详情见POP3Command
     * @param command
     * @return
     * @throws IOException
     */
    public int sendCommand(int command) throws IOException
    {
        return sendCommand(edu.hnu.mail.data.source.remote.POP3Command.commands[command], null);
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

    public int getState() {
        return popState;
    }

    public void setState(int state){
        this.popState = state;
    }

    public int getReplyCode() {
        return replyCode;
    }
}
