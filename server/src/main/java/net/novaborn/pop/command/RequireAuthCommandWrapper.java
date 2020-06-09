package net.novaborn.pop.command;

import net.novaborn.comm.SpringContextHolder;
import net.novaborn.config.ServerConfig;
import net.novaborn.exceptions.DropConnectionException;
import net.novaborn.pop.server.Command;
import net.novaborn.pop.server.PopSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class RequireAuthCommandWrapper implements Command {

    private Command wrapped;
    ServerConfig serverConfig= SpringContextHolder.getBean("serverConfig");;

    /**
     * @param wrapped the wrapped command (not null)
     */
    public RequireAuthCommandWrapper(Command wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(String commandString, PopSession popSession)
            throws IOException, DropConnectionException {
        if (!serverConfig.isPopServerRequireAuth() || popSession.isLogin()) { //配置中不需要登陆或者会话中已经登陆了才执行命令
            wrapped.execute(commandString, popSession);
        } else {    // 否则报错
            popSession.Write("-ERR Authentication required\r\n");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return wrapped.getName();
    }
}
