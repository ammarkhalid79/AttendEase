package com.attendease.controller;

import com.attendease.entity.*;
import com.attendease.exception.DomainException;
import com.attendease.service.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

import static com.attendease.controller.TeacherController.id;

@Controller
@RequestMapping("/teacher")
public class AttendanceController {
    private final AttendanceService attendance;
    private final CourseService courses;

    public AttendanceController(AttendanceService attendance, CourseService courses) {
        this.attendance = attendance;
        this.courses = courses;
    }

    @GetMapping("/attendance")
    String attendance(@RequestParam(required=false) Long courseId, @RequestParam(required=false) LocalDate date,
                      @RequestParam(defaultValue="1") int number, Principal principal, Model model) {
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        model.addAttribute("courses", courses.list(id(principal)));
        model.addAttribute("courseId", courseId);
        model.addAttribute("date", selectedDate);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("number", number);
        model.addAttribute("statuses", Status.values());
        if (courseId != null) model.addAttribute("marks", attendance.sheet(id(principal), courseId, selectedDate, number));
        return "attendance";
    }

    @PostMapping("/attendance")
    String save(@RequestParam long courseId, @RequestParam LocalDate date, @RequestParam int number,
                @RequestParam MultiValueMap<String, String> fields, Principal principal, RedirectAttributes redirect) {
        Map<Long, Status> marks = new HashMap<>();
        try {
            fields.forEach((key, values) -> {
                if (key.startsWith("mark_")) {
                    if (values.size() != 1) throw new IllegalArgumentException();
                    marks.put(Long.valueOf(key.substring(5)), Status.valueOf(values.getFirst()));
                }
            });
            attendance.save(id(principal), courseId, date, number, marks);
            redirect.addFlashAttribute("notice", "Attendance saved.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("problem", "Select a valid status for each listed student.");
        } catch (DomainException ex) {
            redirect.addFlashAttribute("problem", ex.getMessage());
        }
        redirect.addAttribute("courseId", courseId).addAttribute("date", date.toString()).addAttribute("number", number);
        return "redirect:/teacher/attendance";
    }

    @GetMapping("/reports")
    String reports(@RequestParam(required=false) Long courseId, @RequestParam(required=false) Long studentId,
                   @RequestParam(required=false) LocalDate from, @RequestParam(required=false) LocalDate to,
                   @RequestParam(required=false) Status status, Principal principal, Model model) {
        var owned = courses.list(id(principal));
        List<Student> options = courseId == null
                ? owned.stream().flatMap(c -> courses.roster(c.getId()).stream())
                        .collect(java.util.stream.Collectors.toMap(Student::getId, s -> s, (a, b) -> a))
                        .values().stream().sorted(Comparator.comparing(Student::getFullName)).toList()
                : courses.roster(courses.owned(id(principal), courseId).getId());
        model.addAttribute("courses", owned);
        model.addAttribute("students", options);
        model.addAttribute("courseId", courseId);
        model.addAttribute("studentId", studentId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("status", status);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("report", attendance.report(id(principal), courseId, studentId, from, to, status));
        return "reports";
    }

    @GetMapping("/reports.csv")
    ResponseEntity<String> csv(@RequestParam(required=false) Long courseId, @RequestParam(required=false) Long studentId,
                               @RequestParam(required=false) LocalDate from, @RequestParam(required=false) LocalDate to,
                               @RequestParam(required=false) Status status, Principal principal) {
        var report = attendance.report(id(principal), courseId, studentId, from, to, status);
        var csv = new StringBuilder("Course,Date,Session,Registration,Student,Status\r\n");
        for (var e : report.entries()) {
            csv.append(cell(e.course().getCourseCode())).append(',').append(e.session().getSessionDate()).append(',')
                    .append(e.session().getSessionNumber()).append(',').append(cell(e.student().getRegistrationNumber()))
                    .append(',').append(cell(e.student().getFullName())).append(',').append(e.status()).append("\r\n");
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance.csv").body(csv.toString());
    }

    private String cell(String value) {
        String trimmed = value.stripLeading();
        if ((!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0)
                || (!value.isEmpty() && "\t\r\n".indexOf(value.charAt(0)) >= 0)) value = "'" + value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
