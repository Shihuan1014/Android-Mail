package edu.hnu.mail.data.source.remote;

import android.net.InetAddresses;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

/**
 * 代理Socket，干一些底层的活
 * 抽象类。这个类只做一些事情，基本上所有方法都需要子类重写自己的业务
 * author: 周世焕
 * date: 2020/4/15
 */
public abstract class SocketClient {

    //维护Socket长连接, 用protected，和子类共享
    protected Socket socket;
    //维护输入流
    protected InputStream inputStream;
    //维护输出流
    protected OutputStream outputStream;
    //维护ip地址
    protected String ip;
    //连接端口
    protected int port;
    //设置read阻塞时间，即调用InputStream.read()会等服务器多久，超过时间则跳出阻塞
    protected int timeout;
    // 连接超时时间，0表示永久有效
    protected int connectTimeout = 1000;
    // 很有用，因为我们是按行取应答的，所以换行符很重要
    public static final String NETASCII_EOL = "\r\n";

    public SocketClient(){
        socket = null;
        inputStream = null;
        outputStream = null;
        ip = null;
        port = 0;
        timeout = 0;
    }

    /**
     * 连接成功后，要初始化I/O流
     * @throws IOException
     */
    protected void onConnect() throws IOException {
        socket.setSoTimeout(timeout);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    /**
     * 发起建立连接
     * @param host
     * @param port
     * @throws IOException
     */
    public void connect(InetAddress host, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), connectTimeout);
        //如果不报错，说明连接上了，进行IO初始化
        onConnect();
    }

    /**
     * 发起建立连接
     * @param hostName 域名
     * @param port 端口
     * @throws IOException
     */
    public void connect(String hostName, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(hostName, port), connectTimeout);
        //如果不报错，说明连接上了，进行IO初始化
        onConnect();
    }

    public InetAddress getLocalAddress(){
        return socket.getLocalAddress();
    }

    /**
     * 主动断开连接，善后操作
     * @throws IOException
     */
    public void disconnect() throws IOException
    {
        if (socket != null) socket.close();
        if (inputStream != null) inputStream.close();
        if (outputStream != null) outputStream.close();
        if (socket != null) socket = null;
        inputStream = null;
        outputStream = null;
    }

    /**
     * 连接情况检测
     * @return
     */
    public boolean isConnected()
    {
        if (socket == null){
            return false;
        }
        return socket.isConnected();
    }

    public void setPort(int port){
        this.port = port;
    }
    /**
     * 设置read()阻塞等待时间
     * @param timeout
     * @throws SocketException
     */
    public void setSoTimeout(int timeout) throws SocketException
    {
        socket.setSoTimeout(timeout);
    }


    /**
     * 设置发送缓冲区大小
     * @param size
     * @throws SocketException
     */
    public void setSendBufferSize(int size) throws SocketException {
        socket.setSendBufferSize(size);
    }


    /**
     * 设置接收缓冲区大小
     * @param size
     * @throws SocketException
     */
    public void setReceiveBufferSize(int size) throws SocketException  {
        socket.setReceiveBufferSize(size);
    }


    /**
     * 获得read()阻塞时间
     * @return
     * @throws SocketException
     */
    public int getSoTimeout() throws SocketException
    {
        return socket.getSoTimeout();
    }


    /**
     * 设置连接超时时间
     * @param connectTimeout
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 获得连接超时时间
     * @return
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

}
