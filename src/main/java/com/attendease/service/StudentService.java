package com.attendease.service;

import com.attendease.dto.StudentForm;
import com.attendease.entity.Student;
import java.util.List;

public interface StudentService {
    List<Student> search(String query);
    Student get(long id);
    Student save(Long id, StudentForm form);
    void deactivate(long id);
}
