package com.attendease;

import com.attendease.dto.StudentForm;
import com.attendease.entity.*;
import com.attendease.exception.DomainException;
import com.attendease.repository.*;
import com.attendease.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock StudentRepository students;
    @Mock AccountRepository accounts;
    @Mock PasswordEncoder passwords;
    @InjectMocks StudentServiceImpl service;
    private final StudentForm form = new StudentForm("SE-21", "Amina Khan", "Amina@example.org",
            "Software Engineering", 4, "unit-test-password");

    @Test
    void createsAStudentWithAHashedAccountPassword() {
        when(passwords.encode(form.password())).thenReturn("stored-hash");
        when(accounts.save(any())).thenAnswer(call -> call.getArgument(0));
        when(students.save(any())).thenAnswer(call -> call.getArgument(0));

        Student saved = service.save(null, form);
        assertEquals("SE-21", saved.getRegistrationNumber());
        assertEquals("amina@example.org", saved.getEmail());
        var account = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(account.capture());
        assertEquals("stored-hash", account.getValue().getPasswordHash());
        assertEquals(Role.STUDENT, account.getValue().getRole());
    }

    @Test
    void duplicateRegistrationIsRejectedBeforeCreatingAnAccount() {
        Student duplicate = mock(Student.class);
        when(duplicate.getId()).thenReturn(41L);
        when(students.findByRegistrationNumberIgnoreCase("SE-21")).thenReturn(Optional.of(duplicate));

        assertThrows(DomainException.class, () -> service.save(null, form));
        verifyNoInteractions(accounts, passwords);
    }
}
