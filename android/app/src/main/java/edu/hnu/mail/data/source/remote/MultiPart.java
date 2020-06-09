package edu.hnu.mail.data.source.remote;

import java.util.Vector;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

public class MultiPart {

    protected String contentType = "multipart/mixed";
    private MultiPart parent;
    private String content;
    private String textContent;
    private String name;
    private String encoding;
    private String contentId;
    private String charset;
    private Vector<MultiPart> parts = new Vector<MultiPart> ();
    private int isAttachment;
    private int hasAttachment;

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public int getIsAttachment() {
        return isAttachment;
    }

    public void setIsAttachment(int isAttachment) {
        this.isAttachment = isAttachment;
    }

    public int getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(int hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public MultiPart getParent() {
        return parent;
    }

    public void setParent(MultiPart parent) {
        this.parent = parent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public Vector<MultiPart> getParts() {
        return parts;
    }

    public void setParts(Vector<MultiPart> parts) {
        this.parts = parts;
    }

    public void addPart(MultiPart multiPart){
        multiPart.setParent(this);
        parts.add(multiPart);
    }

    public synchronized int getCount() throws MessagingException {
        if (parts == null)
            return 0;

        return parts.size();
    }

    public synchronized MultiPart getBodyPart(int index)
            throws MessagingException {
        if (parts == null)
            throw new IndexOutOfBoundsException("No such BodyPart");

        return (MultiPart)parts.elementAt(index);
    }

    @Override
    public String toString() {
        StringBuilder part = new StringBuilder();

        for (MultiPart multiPart : parts){
            part.append(multiPart);
        }

        return "MultiPart{" +
                "contentType='" + contentType + '\'' +
                ", content='" + content + '\'' +
                ", name='" + name + '\'' +
                ", encoding='" + encoding + '\'' +
                ", contentId='" + contentId + '\'' +
                ", parts=" + part +
                ", isAttachment=" + isAttachment +
                ", hasAttachment=" + hasAttachment +
                '}';
    }
}
