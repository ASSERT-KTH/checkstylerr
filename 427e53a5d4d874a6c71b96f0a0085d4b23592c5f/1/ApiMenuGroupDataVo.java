package com.java110.vo.api.menuGroup;

import java.io.Serializable;
import java.util.Date;

public class ApiMenuGroupDataVo implements Serializable {

    private String gId;
private String name;
private String icon;
private String label;
private String seq;
private String description;
public String getGId() {
        return gId;
    }
public void setGId(String gId) {
        this.gId = gId;
    }
public String getName() {
        return name;
    }
public void setName(String name) {
        this.name = name;
    }
public String getIcon() {
        return icon;
    }
public void setIcon(String icon) {
        this.icon = icon;
    }
public String getLabel() {
        return label;
    }
public void setLabel(String label) {
        this.label = label;
    }
public String getSeq() {
        return seq;
    }
public void setSeq(String seq) {
        this.seq = seq;
    }
public String getDescription() {
        return description;
    }
public void setDescription(String description) {
        this.description = description;
    }

    public String getgId() {
        return gId;
    }

    public void setgId(String gId) {
        this.gId = gId;
    }


}
