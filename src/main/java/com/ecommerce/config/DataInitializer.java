package com.ecommerce.config;

import com.ecommerce.entity.Role;
import com.ecommerce.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        createRoleIfNotExists("BUYER");
        createRoleIfNotExists("SELLER");
        createRoleIfNotExists("ADMIN");
        log.info("Default roles seeded successfully");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }
}
