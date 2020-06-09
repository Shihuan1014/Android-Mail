package edu.hnu.mail.data.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Arrays;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {@Index(value = "uid,userAddress,mailBox", unique = true)})
public class Data {
    String uid;
    String userAddress;
    String mailBox;
    byte[] data;


    @Generated(hash = 814814561)
    public Data(String uid, String userAddress, String mailBox, byte[] data) {
        this.uid = uid;
        this.userAddress = userAddress;
        this.mailBox = mailBox;
        this.data = data;
    }

    @Generated(hash = 2135787902)
    public Data() {
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getMailBox() {
        return mailBox;
    }

    public void setMailBox(String mailBox) {
        this.mailBox = mailBox;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Data{" +
                "uid='" + uid + '\'' +
                ", userAddress='" + userAddress + '\'' +
                ", mailBox='" + mailBox + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
