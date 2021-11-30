package com.hlj.redis.topiclistener.data;

import java.io.Serializable;

/**
 * Created by j.sh on 28/11/2017.
 */
public class ConvertBean implements Serializable {

    private static final long serialVersionUID = -2010261192610665719L;

    private String toUid;
    private String content;

    public ConvertBean() {
    }

    public ConvertBean(String toUid, String content) {
        this.toUid = toUid;
        this.content = content;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ProjectData{" +
                "toUid='" + toUid + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
