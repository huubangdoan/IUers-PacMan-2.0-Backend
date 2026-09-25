package com.Iuer.Pacman.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Iuer.Pacman.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username); // có thể xài Search
    boolean existsByUsername(String username);   
}