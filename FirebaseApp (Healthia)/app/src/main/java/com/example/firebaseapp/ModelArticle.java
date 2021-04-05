package com.example.firebaseapp;

public class ModelArticle {
    //user same name as we given while uploading post
    String aId;
    String aCategory;
    String aTitle;
    String aDescription;
    String aImage;
    String aTime;
    String uid;
    String uEmail;
    String uDp;
    String uName;

    public ModelArticle() {
    }

    public ModelArticle(String aId, String aCategory, String aTitle, String aDescription, String aImage, String aTime, String uid, String uEmail, String uDp, String uName) {
        this.aId = aId;
        this.aCategory = aCategory;
        this.aTitle = aTitle;
        this.aDescription = aDescription;
        this.aImage = aImage;
        this.aTime = aTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
    }

    public String getaCategory() {
        return aCategory;
    }

    public void setaCategory(String aCategory) {
        this.aCategory = aCategory;
    }

    public String getaId() {
        return aId;
    }

    public void setaId(String aId) {
        this.aId = aId;
    }

    public String getaTitle() {
        return aTitle;
    }

    public void setaTitle(String aTitle) {
        this.aTitle = aTitle;
    }

    public String getaDescription() {
        return aDescription;
    }

    public void setaDescription(String aDescription) {
        this.aDescription = aDescription;
    }

    public String getaImage() {
        return aImage;
    }

    public void setaImage(String aImage) {
        this.aImage = aImage;
    }

    public String getaTime() {
        return aTime;
    }

    public void setaTime(String aTime) {
        this.aTime = aTime;
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