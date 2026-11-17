package com.attendease.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String registrationNumber;
    private String fullName;
    private String email;
    private String department;
    private int semester;
    private Long userId;

    protected Student() {}

    public Student(String registrationNumber, String fullName, String email, String department, int semester, Long userId) {
        this.registrationNumber = registrationNumber;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.semester = semester;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
