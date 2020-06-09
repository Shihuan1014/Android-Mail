package edu.hnu.mail.data.entity;

import android.net.Uri;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Base64;

@Entity
public class Attachment {

    private String mailUid;
    private String userAddress;
    private String mailBox;
    private String path;

    @Transient
    private Uri uri;

    @Transient
    private String base64;

    private int size;
    private String fileType;
    private String name;


    @Generated(hash = 918741973)
    public Attachment(String mailUid, String userAddress, String mailBox,
            String path, int size, String fileType, String name) {
        this.mailUid = mailUid;
        this.userAddress = userAddress;
        this.mailBox = mailBox;
        this.path = path;
        this.size = size;
        this.fileType = fileType;
        this.name = name;
    }

    @Generated(hash = 1924760169)
    public Attachment() {
    }


    public String getMailUid() {
        return mailUid;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public void setMailUid(String mailUid) {
        this.mailUid = mailUid;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "Attachment{" +
                "mailUid='" + mailUid + '\'' +
                ", userAddress='" + userAddress + '\'' +
                ", mailBox='" + mailBox + '\'' +
                ", path='" + path + '\'' +
                ", uri=" + uri +
                ", size=" + size +
                ", fileType='" + fileType + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
