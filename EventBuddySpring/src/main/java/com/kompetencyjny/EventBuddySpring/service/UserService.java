package com.kompetencyjny.EventBuddySpring.service;

import com.kompetencyjny.EventBuddySpring.exception.NotFoundException;
import com.kompetencyjny.EventBuddySpring.model.User;
import com.kompetencyjny.EventBuddySpring.model.Role;
import com.kompetencyjny.EventBuddySpring.repo.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(String username, String password, String firstName, String lastName, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Użytkownik już istnieje!");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return userRepository.save(user);
    }
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public Optional<User> findById(Long id){
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) return userOptional;
        if (userOptional.get().getActive().equals(false)) return Optional.empty();
        return userOptional;
    }
    public Optional<User> findByUserName(String username){
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) return userOptional;
        if (userOptional.get().getActive().equals(false)) return Optional.empty();
        return userOptional;
    }

    public Optional<User> findDeletedByUserName(String username){
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) return userOptional;
        if (userOptional.get().getActive().equals(true)) return Optional.empty();
        return userOptional;
    }

    @Transactional
    public User setUserRole(String username, Role role){
        Optional<User> userOpt = this.findByUserName(username);
        if (userOpt.isEmpty()) throw new NotFoundException("User of username: "+username+" does not exist!");
        User user = userOpt.get();
        user.setRole(role);
        return userRepository.save(user);
    }
}
