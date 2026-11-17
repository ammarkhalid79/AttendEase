package com.attendease.controller;

import com.attendease.dto.*;
import com.attendease.entity.Student;
import com.attendease.repository.AccountRepository;
import com.attendease.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
public class TeacherController {
    private final StudentService students;
    private final CourseService courses;
    private final AttendanceService attendance;
    private final AccountRepository accounts;

    public TeacherController(StudentService students, CourseService courses, AttendanceService attendance,
                             AccountRepository accounts) {
        this.students = students;
        this.courses = courses;
        this.attendance = attendance;
        this.accounts = accounts;
    }

    static long id(Principal principal) { return Long.parseLong(principal.getName()); }

    @GetMapping
    String dashboard(Principal principal, Model model) {
        model.addAttribute("dashboard", attendance.dashboard(id(principal)));
        return "dashboard";
    }

    @GetMapping("/students")
    String students(@RequestParam(defaultValue="") String q, Model model) {
        var results = students.search(q);
        model.addAttribute("students", results);
        model.addAttribute("activeIds", accounts.findAllById(results.stream().map(Student::getUserId).toList())
                .stream().filter(a -> a.getActive()).map(a -> a.getId()).collect(Collectors.toSet()));
        model.addAttribute("q", q);
        return "students";
    }

    @GetMapping("/students/{id}")
    String studentDetails(@PathVariable long id, Model model) {
        var student = students.get(id);
        model.addAttribute("student", student);
        model.addAttribute("active", accounts.findById(student.getUserId()).orElseThrow().getActive());
        return "student-details";
    }

    @GetMapping({"/students/new", "/students/{id}/edit"})
    String studentForm(@PathVariable(required=false) Long id, Model model) {
        var student = id == null ? null : students.get(id);
        model.addAttribute("editing", id);
        model.addAttribute("studentForm", student == null ? new StudentForm("", "", "", "", 1, "")
                : new StudentForm(student.getRegistrationNumber(), student.getFullName(), student.getEmail(),
                        student.getDepartment(), student.getSemester(), ""));
        return "student-form";
    }

    @PostMapping("/students/save")
    String saveStudent(@RequestParam(required=false) Long id, @Valid @ModelAttribute StudentForm studentForm,
                       BindingResult errors, Model model, RedirectAttributes flash) {
        model.addAttribute("editing", id);
        if (errors.hasErrors()) return "student-form";
        try {
            var saved = students.save(id, studentForm);
            flash.addFlashAttribute("notice", "Student saved.");
            return "redirect:/teacher/students/" + saved.getId();
        } catch (com.attendease.exception.DomainException ex) {
            errors.reject("student", ex.getMessage());
            return "student-form";
        }
    }

    @PostMapping("/students/{id}/deactivate")
    String deactivate(@PathVariable long id, RedirectAttributes flash) {
        students.deactivate(id);
        flash.addFlashAttribute("notice", "Student account deactivated.");
        return "redirect:/teacher/students/" + id;
    }

    @GetMapping("/courses")
    String courses(Principal principal, Model model) {
        model.addAttribute("courses", courses.list(id(principal)));
        return "courses";
    }

    @GetMapping("/courses/{id}")
    String course(@PathVariable long id, Principal principal, Model model) {
        var course = courses.owned(id(principal), id);
        var roster = courses.roster(id);
        Set<Long> enrolledIds = roster.stream().map(Student::getId).collect(Collectors.toSet());
        model.addAttribute("course", course);
        model.addAttribute("roster", roster);
        model.addAttribute("available", courses.active(students.search("")).stream()
                .filter(s -> !enrolledIds.contains(s.getId())).toList());
        return "course-details";
    }

    @GetMapping({"/courses/new", "/courses/{id}/edit"})
    String courseForm(@PathVariable(required=false) Long id, Principal principal, Model model) {
        var course = id == null ? null : courses.owned(id(principal), id);
        model.addAttribute("editing", id);
        model.addAttribute("courseForm", course == null ? new CourseForm("", "", "")
                : new CourseForm(course.getCourseCode(), course.getCourseName(), course.getDescription()));
        return "course-form";
    }

    @PostMapping("/courses/save")
    String saveCourse(@RequestParam(required=false) Long id, @Valid @ModelAttribute CourseForm courseForm,
                      BindingResult errors, Principal principal, Model model, RedirectAttributes flash) {
        model.addAttribute("editing", id);
        if (errors.hasErrors()) return "course-form";
        try {
            var saved = courses.save(id(principal), id, courseForm);
            flash.addFlashAttribute("notice", "Course saved.");
            return "redirect:/teacher/courses/" + saved.getId();
        } catch (com.attendease.exception.DomainException ex) {
            errors.reject("course", ex.getMessage());
            return "course-form";
        }
    }

    @PostMapping("/courses/{id}/enroll")
    String enroll(@PathVariable long id, @RequestParam long studentId, Principal principal, RedirectAttributes flash) {
        courses.enroll(id(principal), id, studentId);
        flash.addFlashAttribute("notice", "Student enrolled.");
        return "redirect:/teacher/courses/" + id;
    }
}
