package com.aditya.auth.config;

import com.aditya.auth.model.Role;
import com.aditya.auth.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void init() {

        List<String> roles = List.of("ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER");

        roles.forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(null, roleName));
            }
        });
    }
}
