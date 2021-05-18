package com.vaibhav.onlineexams.models;

public class Teacher {
    String uid;
    String name;
    String email;
    String instituteName;

    public Teacher() {
    }

    public Teacher(String uid, String name, String email, String instituteName) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.instituteName = instituteName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }
}
