package com.vaibhav.onlineexams.models;

public class Student {
    String uid;
    String name;
    String email;
    String instituteName;
    int graduationYear;
    String branch;
    String rollNo;

    public Student() {
    }

    public Student(String uid, String name, String email, String instituteName, int graduationYear, String branch, String rollNo) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.instituteName = instituteName;
        this.graduationYear = graduationYear;
        this.branch = branch;
        this.rollNo = rollNo;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(int graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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
