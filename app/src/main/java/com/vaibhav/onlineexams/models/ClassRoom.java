package com.vaibhav.onlineexams.models;

import java.util.ArrayList;

public class ClassRoom {
    String teacherId;
    String branch;
    String batch;
    String title;
    ArrayList<String> studentIds;
    String classroomId;

    public ClassRoom() {
    }

    public ClassRoom(String teacherId, String branch, String batch, String title, ArrayList<String> studentIds, String classroomId) {
        this.teacherId = teacherId;
        this.branch = branch;
        this.batch = batch;
        this.title = title;
        this.studentIds = studentIds;
        this.classroomId = classroomId;
    }

    public String getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(ArrayList<String> studentIds) {
        this.studentIds = studentIds;
    }
}
