package net.novaborn.controller;

import net.novaborn.config.ServerConfig;
import net.novaborn.dao.LogMapper;
import net.novaborn.pop.server.PopServer;
import net.novaborn.smtp.server.SmtpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
public class ServerController {
    @Autowired
    SmtpServer smtpServer;
    @Autowired
    PopServer popServer;
    @Autowired
    ServerConfig serverConfig;
    @Autowired
    LogMapper logMapper;

    @RequestMapping("SmtpServerRun")
    public void SmtpServerRun(HttpServletRequest request) throws Exception {
        if(request.getSession().getAttribute("admin")!=null){
            smtpServer.startupServer();
            logMapper.insertLog("Server Status","Start SMTP Server");
        }
    }

    @RequestMapping("SmtpServerStop")
    public void SmtpServerStop(HttpServletRequest request) throws Exception {
        if(request.getSession().getAttribute("admin")!=null){
            smtpServer.shutdownServer();
            logMapper.insertLog("Server Status","ShutDown SMTP Server");
        }
    }

    @RequestMapping("getSmtpStatus")
    public boolean getSmtpStatus(HttpServletRequest request) throws Exception {
        if(request.getSession().getAttribute("admin")!=null) {
            return smtpServer.getSEVER_STATUS();
        }else {
            return false;
        }
    }
    @RequestMapping("getConfig")
    public ServerConfig getServerConfig(){
        return this.serverConfig;
    }


    @RequestMapping("PopServerRun")
    public void PopServerRun(HttpServletRequest request) throws Exception {
        if(request.getSession().getAttribute("admin")!=null) {
            popServer.startupServer();
            logMapper.insertLog("Server Status","Start POP Server");
        }
    }

    @RequestMapping("PopServerStop")
    public void PopServerStop(HttpServletRequest request) throws Exception {
        if(request.getSession().getAttribute("admin")!=null) {
            popServer.shutdownServer();
            logMapper.insertLog("Server Status","ShutDown POP Server");
        }
    }

    @RequestMapping("getPopStatus")
    public boolean getPopStatus(HttpServletRequest request) throws Exception {
        if(request.getSession().getAttribute("admin")!=null) {
            return popServer.getSEVER_STATUS();
        }else {
            return false;
        }
    }
//
//    @GetMapping("setSmtpPort")
//    public void setSmtpPort(HttpServletRequest request,int port){
//            serverConfig.setSmtpport(port);
//    }
//
//    @GetMapping("setPopPort")
//    public void setPopPort(int port){
//        serverConfig.setPopport(port);
//    }
//
//    @GetMapping("setHostName")
//    public void setHostName(String hostname){
//        serverConfig.setHostname(hostname);
//    }

    @RequestMapping(value = "server",method = RequestMethod.GET)
    public Map<String,Object> getServerInfo(HttpServletRequest request){
        Map<String,Object> map = new HashMap<String, Object>();
        if(request.getSession().getAttribute("admin")!=null){
            map.put("status",200);
            map.put("popPort",serverConfig.getPopport());
            map.put("smtpPort",serverConfig.getSmtpport());
            map.put("maxReceiver",serverConfig.getMaxRecipients());
            map.put("maxMail",serverConfig.getMaxMessageSize());
            map.put("domain",serverConfig.getDomain());
            System.out.println(serverConfig.getMaxMessageSize());
        }else{
            map.put("status",500);
        }
        return map;
    }

    @RequestMapping(value = "server",method = RequestMethod.POST)
    public Map<String,Object> updateServer(int popPort,int smtpPort,int maxReceiver,int maxMail,String domain){
        Map<String,Object> map = new HashMap<String, Object>();
        try {
            boolean needPopReload = false;
            boolean needSmtpReload = false;
            if (popPort!=serverConfig.getPopport()){
                needPopReload = true;
            }
            if (smtpPort!=serverConfig.getSmtpport()){
                needSmtpReload = true;
            }
            serverConfig.setPopport(popPort);
            serverConfig.setSmtpport(smtpPort);
            serverConfig.setMaxRecipients(maxReceiver);
            serverConfig.setMaxMessageSize(maxMail*1024);
            serverConfig.setDomain(domain);
//            URL url = this.getClass().getClassLoader().getResource("serverConfig.properties");
//            Properties pro = new Properties();
//            FileInputStream in = new FileInputStream(url.getPath());
//            pro.load(in);
//            in.close();
//            pro.setProperty("server.domain",domain);
//            pro.setProperty("server.popport",String.valueOf(popPort));
//            pro.setProperty(" server.smtpport",String.valueOf(smtpPort));
//            pro.setProperty("server.MaxRecipients",String.valueOf(maxReceiver));
//            pro.setProperty("server.MaxMessageSize",String.valueOf(maxMail*1024));
//            FileOutputStream out = new FileOutputStream(url.getPath());
//            pro.store(out,new Date(System.currentTimeMillis())+" update");
//            out.close();
//            if (needPopReload){
//                popServer.shutdownServer();
//                popServer.startupServer();
//            }
//            if (needSmtpReload){
//                smtpServer.shutdownServer();
//                smtpServer.startupServer();
//            }
            map.put("status",200);
        }catch (Exception e){
            e.printStackTrace();
            map.put("status",500);
        }
        return map;
    }
}
