package com.attendease.dto;

import jakarta.validation.constraints.*;

public record CourseForm(
        @NotBlank @Size(max=30) String courseCode,
        @NotBlank @Size(max=120) String courseName,
        @Size(max=1000) String description) {}
