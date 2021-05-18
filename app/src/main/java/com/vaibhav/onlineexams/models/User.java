package com.vaibhav.onlineexams.models;

public class User {
    String uid;
    int userType;

    public User() {
    }

    public User(String uid, int userType) {
        this.uid = uid;
        this.userType = userType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }
}
