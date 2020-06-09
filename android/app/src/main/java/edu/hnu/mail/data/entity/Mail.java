package edu.hnu.mail.data.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.sql.Blob;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Transient;

@Entity(indexes = {@Index(value = "uid,messageId,userAddress,mailBox", unique = true)})
public class Mail {
    private String uid;
    private String messageId;
    private String userAddress;
    private String avatar;
    private String from;
    private String fromEmail;
    private String to;
    private String toEmail;
    private Date sendTime;
    private String subject;

    @Transient
    private byte[] data;

    private int flag;
    private int deleted;
    private int draft;
    private int seen;
    private String mailBox;
    private int size;
    private String textContent;
    private String replyTo;
    private int realDeleted;
    private int isGroup;

    @Transient
    private List<Attachment> attachmentList;




    @Generated(hash = 1589877177)
    public Mail(String uid, String messageId, String userAddress, String avatar, String from,
            String fromEmail, String to, String toEmail, Date sendTime, String subject,
            int flag, int deleted, int draft, int seen, String mailBox, int size,
            String textContent, String replyTo, int realDeleted, int isGroup) {
        this.uid = uid;
        this.messageId = messageId;
        this.userAddress = userAddress;
        this.avatar = avatar;
        this.from = from;
        this.fromEmail = fromEmail;
        this.to = to;
        this.toEmail = toEmail;
        this.sendTime = sendTime;
        this.subject = subject;
        this.flag = flag;
        this.deleted = deleted;
        this.draft = draft;
        this.seen = seen;
        this.mailBox = mailBox;
        this.size = size;
        this.textContent = textContent;
        this.replyTo = replyTo;
        this.realDeleted = realDeleted;
        this.isGroup = isGroup;
    }

    @Generated(hash = 1943431032)
    public Mail() {
    }

    


    public int getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(int isGroup) {
        this.isGroup = isGroup;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getDraft() {
        return draft;
    }

    public void setDraft(int draft) {
        this.draft = draft;
    }

    public int getSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public String getMailBox() {
        return mailBox;
    }

    public void setMailBox(String mailBox) {
        this.mailBox = mailBox;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public List<Attachment> getAttachmentList() {
        return attachmentList;
    }

    public void setAttachmentList(List<Attachment> attachmentList) {
        this.attachmentList = attachmentList;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "uid='" + uid + '\'' +
                ", messageId='" + messageId + '\'' +
                ", userAddress='" + userAddress + '\'' +
                ", avatar='" + avatar + '\'' +
                ", from='" + from + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                ", to='" + to + '\'' +
                ", toEmail='" + toEmail + '\'' +
                ", sendTime=" + sendTime +
                ", subject='" + subject + '\'' +
                ", flag=" + flag +
                ", deleted=" + deleted +
                ", draft=" + draft +
                ", seen=" + seen +
                ", mailBox='" + mailBox + '\'' +
                ", size=" + size +
                ", textContent='" + textContent + '\'' +
                ", replyTo='" + replyTo + '\'' +
                ", realDeleted=" + realDeleted +
                ", isGroup=" + isGroup +
                ", attachmentList=" + attachmentList +
                '}';
    }

    public int getRealDeleted() {
        return this.realDeleted;
    }

    public void setRealDeleted(int realDeleted) {
        this.realDeleted = realDeleted;
    }
}
