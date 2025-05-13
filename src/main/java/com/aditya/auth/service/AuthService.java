package com.aditya.auth.service;

import com.aditya.auth.dto.*;
import com.aditya.auth.model.Role;
import com.aditya.auth.model.User;
import com.aditya.auth.repository.RoleRepository;
import com.aditya.auth.repository.UserRepository;
import com.aditya.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        Set<Role> roles = new HashSet<>();

        for (String roleName : request.getRoles()) {
            String formattedRole = "ROLE_" + roleName.toUpperCase();
            Role role = roleRepository.findByName(formattedRole)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + formattedRole));
            roles.add(role);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);

        userRepository.save(user);
        System.out.println("Registered user with roles: " + roles);
        return "User registered successfully!";
    }

    public JwtResponse login(LoginRequest request) {
        System.out.println("Login attempt for: " + request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("User roles: " + user.getRoles());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new JwtResponse(token, user.getUsername(), roles);
    }
}