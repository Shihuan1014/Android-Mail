package net.novaborn.entity;

import java.util.Date;

/**
 * @description:
 * @author: 周世焕
 * @time: 2020-05-21 10:58
 */
public class Log {
    private String type;
    private Date created;
    private String log;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String toString() {
        return "Log{" +
                "type='" + type + '\'' +
                ", created=" + created +
                ", log='" + log + '\'' +
                '}';
    }
}
