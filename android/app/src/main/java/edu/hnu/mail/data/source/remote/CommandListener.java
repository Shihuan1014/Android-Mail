package edu.hnu.mail.data.source.remote;

/**
 * 定义一个接口，对出入站消息做处理
 */
public interface CommandListener {

    // 处理命令发送
    public void commandSent(CommandEvent commandEvent);
    // 处理服务器应答
    public void commandReceived(CommandEvent commandEvent);

}
