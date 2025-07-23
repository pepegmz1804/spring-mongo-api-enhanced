package com.josegomez.spring_mongo_api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
import com.josegomez.spring_mongo_api.domain.dto.RoleRequestDTO;
import com.josegomez.spring_mongo_api.domain.model.Role;
import com.josegomez.spring_mongo_api.domain.model.User;
import com.josegomez.spring_mongo_api.repository.RoleRepository;
import com.josegomez.spring_mongo_api.repository.UserRepository;
import com.josegomez.spring_mongo_api.service.RoleService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleService roleService;

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
    void createRole_returnSuccess() throws Exception {
        RoleRequestDTO request = new RoleRequestDTO();
        request.setKey("admin_role");
        request.setName("Administrator");

        mockMvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.key").value("admin_role"))
                .andExpect(jsonPath("$.name").value("Administrator"));
    }

    @Test
    void createRole_invalidInput_returnsBadRequest() throws Exception {
        RoleRequestDTO request = new RoleRequestDTO();
        request.setKey("ADMIN123");
        request.setName("Invalid Role");

        mockMvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // UPDATE
    @Test
    void updateRole_returnSuccess() throws Exception {
        Role role = new Role();
        role.setKey("support");
        role.setName("Support Agent");
        Role saved = roleRepository.save(role);

        RoleRequestDTO updateRequest = new RoleRequestDTO();
        updateRequest.setKey("support_updated");
        updateRequest.setName("Support Updated");

        mockMvc.perform(
                put("/api/roles/{id}", saved.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.key").value("support_updated"))
                .andExpect(jsonPath("$.name").value("Support Updated"));
    }

    @Test
    void updateRole_whenRoleDoesNotExist_returnsNotFound() throws Exception {
        RoleRequestDTO updateRequest = new RoleRequestDTO();
        updateRequest.setKey("ghost");
        updateRequest.setName("Ghost Role");

        mockMvc.perform(put("/api/roles/{id}", "9999").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found with id: 9999"));
    }

    // GET ALL
    @Test
    void getAllRoles_returnsSuccess() throws Exception {
        roleRepository.save(Role.builder().key("admin").key("Administrator").build());
        roleRepository.save(Role.builder().key("user").key("User").build());

        mockMvc.perform(get("/api/roles")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllUsers_empty() throws Exception {
        mockMvc.perform(get("/api/roles").param("page", "0").param("size", "10")
                .param("sortBy", "id").param("direction", "asc")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }


    // GET BY ID
    @Test
    void getRoleById_returnRole() throws Exception {
        Role role = new Role();
        role.setKey("user_role");
        role.setName("User");
        Role saved = roleRepository.save(role);

        mockMvc.perform(get("/api/roles/{id}", saved.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.key").value("user_role"))
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    void getRoleById_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/roles/{id}", 9999L)).andExpect(status().isNotFound());
    }


    // FILTER BY NAME
    @Test
    void testSearchByName_returnSuccces() throws Exception {
        RoleRequestDTO role = RoleRequestDTO.builder().key("admin").name("Administrator").build();
        roleService.save(role);

        mockMvc.perform(get("/api/roles/filter").param("name", "Admin").param("page", "0")
                .param("size", "10").param("sortBy", "name").param("direction", "asc")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].key").value("admin"))
                .andExpect(jsonPath("$.content[0].name").value("Administrator"));
    }

    @Test
    void testSearchByName_returnNotFound() throws Exception {
        mockMvc.perform(get("/api/roles/filter").param("name", "nonexistentrole").param("page", "0")
                .param("size", "10").param("sortBy", "name").param("direction", "asc")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    // DELETE
    @Test
    void deleteRole_whenRoleNotUsed_returnSucces() throws Exception {
        Role role = new Role();
        role.setKey("to_delete");
        role.setName("To Delete");
        Role saved = roleRepository.save(role);

        mockMvc.perform(delete("/api/roles/{id}", saved.getId())).andExpect(status().isNoContent());

        assertThat(roleRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void deleteRole_whenRoleAssignedToUser_returnsBadRequest() throws Exception {
        Role role = new Role();
        role.setKey("assigned_role");
        role.setName("Assigned");
        Role savedRole = roleRepository.save(role);

        User user = new User();
        user.setFirstName("Test");
        user.setLastNamePaternal("User");
        user.setLastNameMaternal("Test");
        user.setRoles(List.of(savedRole.getId()));
        userRepository.save(user);

        mockMvc.perform(delete("/api/roles/{id}", savedRole.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot delete role")));
    }
}
