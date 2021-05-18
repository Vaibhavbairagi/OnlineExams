package com.vaibhav.onlineexams.models;

public class Submission {
    String submissionId;
    String examId;
    String studentId;
    String submissionLink;
    String rollNo;

    public Submission() {
    }

    public Submission(String submissionId, String examId, String studentId, String submissionLink, String rollNo) {
        this.submissionId = submissionId;
        this.examId = examId;
        this.studentId = studentId;
        this.submissionLink = submissionLink;
        this.rollNo = rollNo;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSubmissionLink() {
        return submissionLink;
    }

    public void setSubmissionLink(String submissionLink) {
        this.submissionLink = submissionLink;
    }
}
