package edu.hnu.mail.data.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Contact {

    @Id
    String mailAddress;
    String userName;
    String avatar;

    @Generated(hash = 772742463)
    public Contact(String mailAddress, String userName, String avatar) {
        this.mailAddress = mailAddress;
        this.userName = userName;
        this.avatar = avatar;
    }
    @Generated(hash = 672515148)
    public Contact() {
    }
    public String getMailAddress() {
        return this.mailAddress;
    }
    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }
    public String getUserName() {
        return this.userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getAvatar() {
        return this.avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
