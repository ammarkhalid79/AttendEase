package com.attendease.controller;

import com.attendease.entity.Role;
import com.attendease.repository.*;
import com.attendease.service.AttendanceService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class PortalController {
    private final AccountRepository accounts;
    private final StudentRepository students;
    private final AttendanceService attendance;

    public PortalController(AccountRepository accounts, StudentRepository students, AttendanceService attendance) {
        this.accounts = accounts;
        this.students = students;
        this.attendance = attendance;
    }

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping("/")
    String home(Principal principal) {
        var account = accounts.findById(Long.valueOf(principal.getName())).filter(a -> a.getActive())
                .orElseThrow(() -> new AccessDeniedException("This account is inactive."));
        return account.getRole() == Role.TEACHER ? "redirect:/teacher" : "redirect:/student";
    }

    @GetMapping("/student")
    String student(Principal principal, Model model) {
        var account = accounts.findById(Long.valueOf(principal.getName())).filter(a -> a.getActive())
                .orElseThrow(() -> new AccessDeniedException("This account is inactive."));
        var student = students.findByUserId(account.getId())
                .orElseThrow(() -> new AccessDeniedException("No student profile is linked to this account."));
        model.addAttribute("student", student);
        model.addAttribute("courses", attendance.studentCourses(student.getId()));
        model.addAttribute("entries", attendance.studentHistory(student.getId()));
        return "student";
    }
}
