package edu.hnu.mail.data.source.remote;

/**
 * 定义一个实体，能够表示命令或者应答的整体
 */
public class CommandEvent {
    // 应答码
    private int replyCode;
    // 是否为指令，如果不是，那就是应答
    private boolean isCommand;
    // 消息、指令
    private String message, command;

    /**
     * 给入站应答写的构造函数
     * @param replyCode
     * @param message
     */
    public CommandEvent(int replyCode,String message){
        this.replyCode = replyCode;
        this.message = message;
        //显然，不是出站命令
        this.isCommand = false;
    }

    /**
     * 给出站指令写的构造函数
     * @param command
     * @param message
     */
    public CommandEvent(String command,String message){
        this.command = command;
        this.message = message;
        this.isCommand = true;
    }

    public int getReplyCode() {
        return replyCode;
    }

    public void setReplyCode(int replyCode) {
        this.replyCode = replyCode;
    }

    public boolean isCommand() {
        return isCommand;
    }

    public void setCommand(boolean command) {
        isCommand = command;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }


    @Override
    public String toString() {
        return "CommandEvent{" +
                "replyCode=" + replyCode +
                ", isCommand=" + isCommand +
                ", message='" + message + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
