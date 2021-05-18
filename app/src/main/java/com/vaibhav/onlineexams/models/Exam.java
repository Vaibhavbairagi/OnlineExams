package com.vaibhav.onlineexams.models;

import java.util.ArrayList;

public class Exam {
    String examId;
    String classroomId;
    String examTitle;
    String startTime;
    String endTime;
    String qpLink;

    public Exam() {
    }

    public Exam(String examId, String classroomId, String examTitle, String startTime, String endTime, String qpLink) {
        this.examId = examId;
        this.classroomId = classroomId;
        this.examTitle = examTitle;
        this.startTime = startTime;
        this.endTime = endTime;
        this.qpLink = qpLink;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getQpLink() {
        return qpLink;
    }

    public void setQpLink(String qpLink) {
        this.qpLink = qpLink;
    }

}
