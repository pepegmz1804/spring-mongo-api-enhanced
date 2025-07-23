package com.josegomez.spring_mongo_api.controller;


import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.josegomez.spring_mongo_api.domain.dto.UserRequestDTO;
import com.josegomez.spring_mongo_api.domain.model.Role;
import com.josegomez.spring_mongo_api.domain.model.User;
import com.josegomez.spring_mongo_api.repository.RoleRepository;
import com.josegomez.spring_mongo_api.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void clearDatabase() {
        mongoTemplate.getDb().drop();
    }

    // CREATE
    @Test
    void createUser_returnSuccess() throws Exception {
        roleRepository.save(new Role(null, "user", "User"));

        UserRequestDTO request = new UserRequestDTO("Juan", "Perez", "Gomez", List.of("user"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.roles[0].key").value("user"));
    }

    @Test
    void createUser_invalidInput_returnsBadRequest() throws Exception {
        UserRequestDTO request = new UserRequestDTO("", "", "", List.of());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // UPDATE
    @Test
    void updateUser_returnSuccess() throws Exception {
        Role adminRole = roleRepository.save(new Role(null, "admin", "Admin"));
        User user = userRepository
                .save(new User(null, "Ana", "Lopez", "Martinez", List.of(adminRole.getId()), "us4", "pass4", true));

        UserRequestDTO request =
                new UserRequestDTO("Ana Updated", "Lopez", "Martinez", List.of("admin"));

        mockMvc.perform(put("/api/users/{id}", user.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana Updated"));
    }

    @Test
    void updateUser_notFound_returnsNotFound() throws Exception {
        UserRequestDTO request = new UserRequestDTO("Ana", "Lopez", "Martinez", List.of("admin"));

        mockMvc.perform(put("/api/users/{id}", 9999L).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 9999"));
    }


    // GET ALL
    @Test
    void getAllUsers_returnSuccess() throws Exception {
        Role role = roleRepository.save(new Role(null, "user", "Usuario estándar"));
        userRepository.save(new User(null, "Lucia", "Garcia", "Reyes", List.of(role.getId()), "us4", "pass4", true));

        mockMvc.perform(get("/api/users").param("page", "0").param("size", "10")
                .param("sortBy", "id").param("direction", "asc")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getAllUsers_empty() throws Exception {
        mockMvc.perform(get("/api/users").param("page", "0").param("size", "10")
                .param("sortBy", "id").param("direction", "asc")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // GET BY ID
    @Test
    void getUserById_returnUser() throws Exception {
        Role role = roleRepository.save(new Role(null, "user", "User"));
        User user = userRepository
                .save(new User(null, "Carlos", "Ramirez", "Sanchez", List.of(role.getId()), "us1", "pass1", true));

        mockMvc.perform(get("/api/users/{id}", user.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Carlos"));
    }

    @Test
    void getUserById_notFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 9999L)).andExpect(status().isNotFound());
    }

    // FILTER BY NAME
    @Test
    void filterUsersByName_returnSuccess() throws Exception {
        Role role = roleRepository.save(new Role(null, "user", "User"));
        userRepository.
            save(new User(null, "Mario", "López", "Torres", List.of(role.getId()), "us2", "pass2", true));

        mockMvc.perform(get("/api/users/filter").param("name", "Mario").param("page", "0")
                .param("size", "10")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Mario"));
    }

    @Test
    void filterUsersByName_returnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/filter").param("name", "nonexistentrole").param("page", "0")
                .param("size", "10").param("sortBy", "name").param("direction", "asc")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }

    // DELETE
    @Test
    void deleteUser_returnSuccess() throws Exception {
        Role role = roleRepository.save(new Role(null, "user", "User"));
        User user = userRepository
                .save(new User(null, "Laura", "Mendoza", "Vargas", List.of(role.getId()), "us3", "pass3", true));

        mockMvc.perform(delete("/api/users/{id}", user.getId())).andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 9999L)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 9999"));
    }
}
