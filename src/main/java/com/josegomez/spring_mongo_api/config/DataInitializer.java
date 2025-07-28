package com.josegomez.spring_mongo_api.config;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.josegomez.spring_mongo_api.domain.dto.RoleRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.UserRequestDTO;
import com.josegomez.spring_mongo_api.domain.model.InitializationTask;
import com.josegomez.spring_mongo_api.domain.model.Role;
import com.josegomez.spring_mongo_api.domain.model.User;
import com.josegomez.spring_mongo_api.repository.InitializationTaskRepository;
import com.josegomez.spring_mongo_api.repository.RoleRepository;
import com.josegomez.spring_mongo_api.repository.UserRepository;
import com.josegomez.spring_mongo_api.service.RoleService;
import com.josegomez.spring_mongo_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The DataInitializer class is a Spring component that implements
 * CommandLineRunner. His
 * method @run populate the database if data is not already exist
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final InitializationTaskRepository initTaskRepository;

    /**
     * The function initializes roles and users data if they are not already present
     * in the system.
     */
    @Override
    public void run(String... args) throws Exception {

        loadRoles();
        loadUsers();
        fixRolePrefixes();
        fixPreviouslySavedUsers();
        loadAdminUser();
    }

    private void loadRoles() {
        if (roleService.count() == 0) {
            List<RoleRequestDTO> roles = List.of(buildRole("admin", "Admin"),
                    buildRole("moderator", "Moderator"), buildRole("user", "User"),
                    buildRole("monitor", "Monitor"), buildRole("editor", "Editor"));
            roleService.saveAll(roles);
            log.info("Roles data initialized");
        }
    }

    private void loadUsers() {
        if (roleService.count() > 0 && userService.count() == 0) {

            String keyRoleAdmin = "admin";
            String keyRoleModerator = "moderator";
            String keyRoleUser = "user";

            List<UserRequestDTO> users = List.of(
                    buildUser("Gabriel", "García", "Márquez", List.of(keyRoleAdmin, keyRoleUser)),
                    buildUser("Isabel", "Allende", "Llona", List.of(keyRoleUser)),
                    buildUser("Mario", "Vargas", "Llosa", List.of(keyRoleAdmin)),
                    buildUser("Jorge", "Luis", "Borges", List.of(keyRoleUser, keyRoleModerator)),
                    buildUser("Julio", "Cortázar", "Descotte", List.of(keyRoleUser)),
                    buildUser("Laura", "Esquivel", "Valdés",
                            List.of(keyRoleModerator, keyRoleUser)),
                    buildUser("Carlos", "Fuentes", "Macías", List.of(keyRoleUser)),
                    buildUser("Rosario", "Castellanos", "Figueroa",
                            List.of(keyRoleAdmin, keyRoleModerator, keyRoleUser)),
                    buildUser("Juan", "Rulfo", "Vizcaíno", List.of(keyRoleUser)),
                    buildUser("Claribel", "Alegría", "Vides", List.of(keyRoleModerator)),
                    buildUser("Juan", "Manuel", "Marquez", List.of(keyRoleUser)),
                    buildUser("Clara", "Alfonsina", "Velazquez", List.of(keyRoleUser)),
                    buildUser("Mario", "Moreno", "Reyes", List.of(keyRoleModerator)),
                    buildUser("Gabriela", "Mistral", "Godoy", List.of(keyRoleUser)),
                    buildUser("Isabelo", "Lopez", "Hernandez", List.of(keyRoleModerator)));

            userService.saveAll(users);
            log.info("Users data initialized");
        }

    }

    private RoleRequestDTO buildRole(String key, String name) {
        return RoleRequestDTO.builder().key(key).name(name).build();
    }

    private UserRequestDTO buildUser(String first, String lastP, String lastM, List<String> roles) {
        return UserRequestDTO.builder().firstName(first).lastNamePaternal(lastP)
                .lastNameMaternal(lastM).roleKeys(roles).build();
    }

    private void loadAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            List<Role> adminRoles = roleRepository.findByKeyIn(List.of("ROLE_ADMIN"));
            List<Long> roleIds = adminRoles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toList());

            User admin = new User();
            admin.setUsername("admin");
            admin.setFirstName("admin");
            admin.setLastNamePaternal("admin");
            admin.setLastNameMaternal("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(roleIds);
            userRepository.save(admin);
            log.info("✔ Usuario admin creado.");
        }
    }

    private void fixPreviouslySavedUsers() {
        log.debug("Fix previously saved users start");
        String taskKey = "FIX_PREVOUSLY_SAVED_USERS";

        boolean alreadyExecuted = initTaskRepository.existsById(taskKey);
        if (alreadyExecuted) {
            log.debug("Fix previously saved users already executed");
            return;
        }

        userRepository.findAll().forEach(user -> {
            if (user.getUsername() == null) {
                //user.setUsername(user.getFirstName() + user.getLastNamePaternal());
                //user.setPassword(passwordEncoder.encode(user.getFirstName() + user.getLastNamePaternal()));
                user.setEnabled(false);
                userRepository.save(user);
            }
        });

        InitializationTask task = new InitializationTask();
        task.setKey(taskKey);
        task.setDescription("Auto set enabled value to false for prevoisuly incomplete users");
        task.setExecutedAt(Instant.now());
        task.setExecuted(true);
        initTaskRepository.save(task);
        log.debug("Fix previously saved users success");

    }

    private void fixRolePrefixes() {
        log.debug("Fix role prefixes start");
        String taskKey = "FIX_ROLE_PREFIXES";

        boolean alreadyExecuted = initTaskRepository.existsById(taskKey);
        if (alreadyExecuted) {
            log.debug("Fix role prefixes already executed");
            return;
        }

        roleRepository.findAll().forEach(role -> {
            if (!role.getKey().startsWith("ROLE_")) {
                role.setKey("ROLE_" + role.getKey().toUpperCase());
                roleRepository.save(role);
            }
        });

        InitializationTask task = new InitializationTask();
        task.setKey(taskKey);
        task.setDescription("Prefixed all role keys with ROLE_");
        task.setExecutedAt(Instant.now());
        task.setExecuted(true);
        initTaskRepository.save(task);
        log.debug("Fix role prefixes success");

    }

}
