package com.Iuer.Pacman.service;

import com.Iuer.Pacman.entity.User;
import com.Iuer.Pacman.index.TreeIndex;
import com.Iuer.Pacman.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserIndexService {

    private final UserRepository userRepository;
    private final TreeIndex<String, User> userTree = new TreeIndex<>();

    public UserIndexService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void preloadUsers() {
        userRepository.findAll().forEach(user -> {
            if (user.getUsername() != null) {
                userTree.insert(user.getUsername(), user);
            }
        });
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        User cached = userTree.find(username);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<User> fromDb = userRepository.findByUsername(username);
        fromDb.ifPresent(u -> userTree.insert(u.getUsername(), u));
        return fromDb;
    }

    public boolean existsByUsername(String username) {
        if (username == null) {
            return false;
        }
        if (userTree.find(username) != null) {
            return true;
        }
        return userRepository.existsByUsername(username);
    }

    public User save(User user) {
        User saved = userRepository.save(user);
        if (saved.getUsername() != null) {
            userTree.insert(saved.getUsername(), saved);
        }
        return saved;
    }
}
