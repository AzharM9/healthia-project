package com.example.firebaseapp;

public class ModelForum {
    //user same name as we given while uploading post
    String fId;
    String fCategory;
    String fTitle;
    String fDescription;
    String fReplies;
    String fImage;
    String fTime;
    String uid;
    String uEmail;
    String uDp;
    String uName;

    public ModelForum() {
    }

    public ModelForum(String fId, String fCategory, String fTitle, String fDescription, String fReplies, String fImage, String fTime, String uid, String uEmail, String uDp, String uName) {
        this.fId = fId;
        this.fCategory = fCategory;
        this.fTitle = fTitle;
        this.fDescription = fDescription;
        this.fReplies = fReplies;
        this.fImage = fImage;
        this.fTime = fTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
    }

    public String getfCategory() {
        return fCategory;
    }

    public void setfCategory(String fCategory) {
        this.fCategory = fCategory;
    }

    public String getfId() {
        return fId;
    }

    public void setfId(String fId) {
        this.fId = fId;
    }

    public String getfTitle() {
        return fTitle;
    }

    public void setfTitle(String fTitle) {
        this.fTitle = fTitle;
    }

    public String getfDescription() {
        return fDescription;
    }

    public void setfDescription(String fDescription) {
        this.fDescription = fDescription;
    }

    public String getfReplies() {
        return fReplies;
    }

    public void setfReplies(String fReplies) {
        this.fReplies = fReplies;
    }

    public String getfImage() {
        return fImage;
    }

    public void setfImage(String fImage) {
        this.fImage = fImage;
    }

    public String getfTime() {
        return fTime;
    }

    public void setfTime(String fTime) {
        this.fTime = fTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}