package com.attendease.repository;

import com.attendease.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.*;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsernameIgnoreCase(String username);
    Optional<Account> findByEmailIgnoreCase(String email);
}
