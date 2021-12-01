package com.java110.vo.api.junkRequirement;

import java.io.Serializable;

public class ApiJunkRequirementDataVo implements Serializable {

    private String publishUserName;
    private String publishUserId;
    private String statusCd;
    private String junkRequirementId;
    private String classification;
    private String referencePrice;
    private String operate;
    private String typeCd;
    private String publishUserLink;
    private String context;
    private String state;
    private String communityId;
    private String bId;

    public String getPublishUserName() {
        return publishUserName;
    }

    public void setPublishUserName(String publishUserName) {
        this.publishUserName = publishUserName;
    }

    public String getPublishUserId() {
        return publishUserId;
    }

    public void setPublishUserId(String publishUserId) {
        this.publishUserId = publishUserId;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getJunkRequirementId() {
        return junkRequirementId;
    }

    public void setJunkRequirementId(String junkRequirementId) {
        this.junkRequirementId = junkRequirementId;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(String referencePrice) {
        this.referencePrice = referencePrice;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public String getPublishUserLink() {
        return publishUserLink;
    }

    public void setPublishUserLink(String publishUserLink) {
        this.publishUserLink = publishUserLink;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getBId() {
        return bId;
    }

    public void setBId(String bId) {
        this.bId = bId;
    }


}
