package net.novaborn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value="classpath:serverConfig.properties")//属性源,maven配置位于src\main\resources目录下
@ConfigurationProperties(prefix = "server")
public class ServerConfig {
//    public static final String hostname = "www.wangzhiwang.online";
////    public static final String dataMode = "false";
////    public static final int MaxMessageSize = 1024*26;
////    public static final boolean isAuthenticated = true;
////    public static final boolean isPopServerRequireAuth = true; // pop3服务器是否需要先登陆才能发送邮件，默认是需要的，即不登陆就不能在这里拉屎
////    public static final int MaxRecipients = 6;
    private String hostname;
    private int MaxMessageSize;
    private boolean isAuthenticated;
    private boolean isPopServerRequireAuth ; // pop3服务器是否需要先登陆才能发送邮件，默认是需要的，即不登陆就不能在这里拉屎
    private int MaxRecipients;
    private int popport;
    private int smtpport;
    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getMaxMessageSize() {
        return MaxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        MaxMessageSize = maxMessageSize;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public boolean isPopServerRequireAuth() {
        return isPopServerRequireAuth;
    }

    public void setPopServerRequireAuth(boolean popServerRequireAuth) {
        isPopServerRequireAuth = popServerRequireAuth;
    }

    public int getMaxRecipients() {
        return MaxRecipients;
    }

    public void setMaxRecipients(int maxRecipients) {
        MaxRecipients = maxRecipients;
    }

    public int getPopport() {
        return popport;
    }

    public void setPopport(int popport) {
        this.popport = popport;
    }

    public int getSmtpport() {
        return smtpport;
    }

    public void setSmtpport(int smtpport) {
        this.smtpport = smtpport;
    }
}
