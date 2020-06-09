package edu.hnu.mail.data.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

import edu.hnu.mail.data.source.remote.POP3Client;

@Entity
public class User {
    @Id(autoincrement = true)
    private long userId;
    private String userName;
    private String avatar;
    private String popHost;
    private int popPort;
    private String popUserName;
    private String popPassword;
    private String smtpHost;
    private int smtpPort;
    private String smtpUserName;
    private String smtpPassword;
    private String nickName;
    private int userType;

    @Transient
    private POP3Client pop3Client;

    @Transient
    private int mailInboxCount;

    @Generated(hash = 1282054)
    public User(long userId, String userName, String avatar, String popHost,
            int popPort, String popUserName, String popPassword, String smtpHost,
            int smtpPort, String smtpUserName, String smtpPassword, String nickName,
            int userType) {
        this.userId = userId;
        this.userName = userName;
        this.avatar = avatar;
        this.popHost = popHost;
        this.popPort = popPort;
        this.popUserName = popUserName;
        this.popPassword = popPassword;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUserName = smtpUserName;
        this.smtpPassword = smtpPassword;
        this.nickName = nickName;
        this.userType = userType;
    }

    @Generated(hash = 586692638)
    public User() {
    }


    public POP3Client getPop3Client() {
        return pop3Client;
    }

    public void setPop3Client(POP3Client pop3Client) {
        this.pop3Client = pop3Client;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPopHost() {
        return popHost;
    }

    public void setPopHost(String popHost) {
        this.popHost = popHost;
    }

    public int getPopPort() {
        return popPort;
    }

    public void setPopPort(int popPort) {
        this.popPort = popPort;
    }

    public String getPopUserName() {
        return popUserName;
    }

    public void setPopUserName(String popUserName) {
        this.popUserName = popUserName;
    }

    public String getPopPassword() {
        return popPassword;
    }

    public void setPopPassword(String popPassword) {
        this.popPassword = popPassword;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUserName() {
        return smtpUserName;
    }

    public void setSmtpUserName(String smtpUserName) {
        this.smtpUserName = smtpUserName;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getMailInboxCount() {
        return mailInboxCount;
    }

    public void setMailInboxCount(int mailInboxCount) {
        this.mailInboxCount = mailInboxCount;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", popHost='" + popHost + '\'' +
                ", popPort=" + popPort +
                ", popUserName='" + popUserName + '\'' +
                ", popPassword='" + popPassword + '\'' +
                ", smtpHost='" + smtpHost + '\'' +
                ", smtpPort=" + smtpPort +
                ", smtpUserName='" + smtpUserName + '\'' +
                ", smtpPassword='" + smtpPassword + '\'' +
                ", nickName='" + nickName + '\'' +
                ", userType=" + userType +
                ", mailInboxCount=" + mailInboxCount +
                '}';
    }
}
