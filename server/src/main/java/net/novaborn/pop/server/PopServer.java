package net.novaborn.pop.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import net.novaborn.config.ServerConfig;
import net.novaborn.smtp.server.SmtpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class PopServer {
    public final AtomicBoolean SEVER_STATUS = new AtomicBoolean(false);
    private static EventLoopGroup bossGroup;
    @Autowired
    ServerConfig serverConfig;
    private static EventLoopGroup workerGroup;
    private final static Logger log = LoggerFactory.getLogger(SmtpSession.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public boolean getSEVER_STATUS() {
        return SEVER_STATUS.get();
    }
    @Async
    public  void run() throws Exception {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        if(SEVER_STATUS.get()==false) {

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childHandler(new PopServerInitializer());
                ChannelFuture f = b.bind(serverConfig.getPopport()).sync();
                log.info(sdf.format(new java.util.Date()) + " -[POP服务器]:启动了 监听端口"+serverConfig.getPopport());
                SEVER_STATUS.set(true);
//                f.addListener((ChannelFutureListener) channelFuture -> {
////                log.info("---pop邮件服务器 启动了---");
//                });
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
                SEVER_STATUS.set(false);
                log.info(sdf.format(new java.util.Date()) + " -[SMTP服务器]:关闭了");
            }
        }else{
                log.info(sdf.format(new java.util.Date())+" -[SMTP服务器]:服务器已经启动了 请勿重复启动");
            }

    }


    @Async
    public  void startupServer() {
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void shutdownServer() {
        if(SEVER_STATUS.get()==true) {
            if ((bossGroup != null) && (!bossGroup.isShutdown())) {
                bossGroup.shutdownGracefully();
            }
            if ((workerGroup != null) && (!workerGroup.isShutdown())) {
                workerGroup.shutdownGracefully();
            }
        }else{
            log.info(sdf.format(new java.util.Date())+" -[SMTP服务器]:服务器还未启动 无法关闭");
        }
    }

//    public static void main(String[] args) {
//        startupServer();
//    }
}
