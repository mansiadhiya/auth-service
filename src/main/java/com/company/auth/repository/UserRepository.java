package com.company.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.company.auth.entity.User;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
}