package edu.hnu.mail.data.source.remote;

import java.util.List;
import java.util.Map;

import edu.hnu.mail.data.entity.Attachment;

public class Mail {
    private String uid;
    private Map<String,String> header;
    private MultiPart multiPart;
    private String text;
    private List<Attachment> attachments;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public MultiPart getMultiPart() {
        return multiPart;
    }

    public void setMultiPart(MultiPart multiPart) {
        this.multiPart = multiPart;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "header=" + header +
                ", multiPart=" + multiPart +
                ", text='" + text + '\'' +
                '}';
    }
}
