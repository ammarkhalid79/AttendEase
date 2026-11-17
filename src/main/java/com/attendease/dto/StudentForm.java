package com.attendease.dto;

import jakarta.validation.constraints.*;

public record StudentForm(
        @NotBlank @Size(max=40) String registrationNumber,
        @NotBlank @Size(max=120) String fullName,
        @NotBlank @Email @Size(max=160) String email,
        @NotBlank @Size(max=100) String department,
        @NotNull @Min(1) @Max(12) Integer semester,
        @Size(max=72) String password) {}
