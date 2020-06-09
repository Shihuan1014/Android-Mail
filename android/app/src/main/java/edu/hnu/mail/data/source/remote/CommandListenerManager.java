package edu.hnu.mail.data.source.remote;
import java.util.EventListener;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 全局管理所有的Listener，有事情就通过这个类来广播给所有对该类型事件感兴趣的Listener
 */
public class CommandListenerManager {

    // 静态引用，反正之后不会改了
    private final CopyOnWriteArrayList<CommandListener> listeners;

    public CommandListenerManager(){
        listeners =  new CopyOnWriteArrayList<CommandListener>();

    }

    // 广播发送出去的命令
    public void fireCommandSent(String command, String message)
    {
        CommandEvent event;

        event = new CommandEvent(command, message);

        for (CommandListener listener : listeners)
        {
            ((CommandListener)listener).commandSent(event);
        }
    }

    // 广播接受到的应答
    public void fireReplyReceived(int replyCode, String message)
    {
        CommandEvent event;
        event = new CommandEvent(replyCode, message);
        for (CommandListener listener : listeners)
        {
            ((CommandListener)listener).commandReceived(event);
        }
    }

    /***
     * Adds a ProtocolCommandListener.
     * <p>
     * @param listener  The ProtocolCommandListener to add.
     ***/
    public void addCommandListener(CommandListener listener)
    {
        listeners.add(listener);
    }

    /**
     * 删除掉
     * @param listener
     */
    public void removeCommandListener(CommandListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * 获得总共注册的事件监听器总数
     * @return
     */
    public int getListenerCount()
    {
        return listeners.size();
    }

}
