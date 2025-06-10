package com.josegomez.spring_mongo_api.config;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.josegomez.spring_mongo_api.domain.dto.RoleRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.UserRequestDTO;
import com.josegomez.spring_mongo_api.service.RoleService;
import com.josegomez.spring_mongo_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The DataInitializer class is a Spring component that implements CommandLineRunner. His
 * method @run populate the database if data is not already exist
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;
    private final UserService userService;

    /**
     * The function initializes roles and users data if they are not already present in the system.
     */
    @Override
    public void run(String... args) throws Exception {
        if (roleService.count() == 0) {
            List<RoleRequestDTO> roles = List.of(buildRole("admin", "Admin"),
                    buildRole("moderator", "Moderator"), buildRole("user", "User"),
                    buildRole("monitor", "Monitor"), buildRole("editor", "Editor"));
            roleService.saveAll(roles);
            log.info("Roles data initialized");
        }

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
}
