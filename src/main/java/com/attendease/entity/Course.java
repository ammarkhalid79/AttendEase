package com.attendease.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Long teacherId;

    protected Course() {}

    public Course(String courseCode, String courseName, String description, Long teacherId) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.teacherId = teacherId;
    }

    public Long getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}
