package com.josegomez.spring_mongo_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.josegomez.spring_mongo_api.domain.dto.RoleResponseDTO;
import com.josegomez.spring_mongo_api.domain.dto.UserRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.UserResponseDTO;
import com.josegomez.spring_mongo_api.service.RoleService;
import com.josegomez.spring_mongo_api.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RoleService roleService;


    // CREATE
    @Test
    void createUser_success() throws Exception {
        List<RoleResponseDTO> roles = List.of(new RoleResponseDTO(1L, "user", "Usuario estándar"));

        UserRequestDTO request = new UserRequestDTO("Juan", "Perez", "Gomez", List.of("user"));

        UserResponseDTO response = new UserResponseDTO(1L, "Juan", "Perez", "Gomez", roles);

        when(userService.save(any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.roles[0].key").value("user"))
                .andExpect(jsonPath("$.roles[0].name").value("Usuario estándar"));
    }

    @Test
    void createUser_invalidInput() throws Exception {
        UserRequestDTO request = new UserRequestDTO("", "", "", List.of());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // UPDATE
    @Test
    void updateUser_success() throws Exception {
        UserRequestDTO request =
                new UserRequestDTO("Juan Updated", "Perez", "Gomez", List.of("admin"));
        List<RoleResponseDTO> roles = List.of(new RoleResponseDTO(2L, "admin", "Administrador"));
        UserResponseDTO response = new UserResponseDTO(1L, "Juan Updated", "Perez", "Gomez", roles);

        when(userService.update(eq(1L), any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan Updated"))
                .andExpect(jsonPath("$.roles[0].key").value("admin"));
    }

    @Test
    void updateUser_notFound() throws Exception {
        UserRequestDTO request = new UserRequestDTO("Juan", "Perez", "Gomez", List.of("user"));

        when(userService.update(eq(999L), any(UserRequestDTO.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/999").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // GET ALL
    @Test
    void getAllUsers_success() throws Exception {
        List<RoleResponseDTO> roles = List.of(new RoleResponseDTO(1L, "user", "Usuario estándar"));
        UserResponseDTO user = new UserResponseDTO(1L, "Juan", "Perez", "Gomez", roles);

        Page<UserResponseDTO> page = new PageImpl<>(List.of(user));
        when(userService.findAll(any(Pageable.class), eq(false))).thenReturn(page);

        mockMvc.perform(get("/api/users").param("page", "0").param("size", "10")
                .param("sortBy", "id").param("direction", "asc").param("all", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getAllUsers_empty() throws Exception {
        Page<UserResponseDTO> emptyPage = Page.empty();
        when(userService.findAll(any(Pageable.class), eq(false))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/users")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // GET BY ID
    @Test
    void getUserById_found() throws Exception {
        List<RoleResponseDTO> roles = List.of(new RoleResponseDTO(1L, "user", "Usuario estándar"));
        UserResponseDTO response = new UserResponseDTO(1L, "Juan", "Perez", "Gomez", roles);

        when(userService.findByIdWithRoles(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/users/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserById_notFound() throws Exception {
        when(userService.findByIdWithRoles(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999")).andExpect(status().isNotFound());
    }

    // FILTER
    @Test
    void searchUsersByName_found() throws Exception {
        List<RoleResponseDTO> roles = List.of(new RoleResponseDTO(1L, "user", "Usuario estándar"));
        UserResponseDTO user = new UserResponseDTO(1L, "Juan", "Perez", "Gomez", roles);

        Page<UserResponseDTO> page = new PageImpl<>(List.of(user));
        when(userService.findByName(eq("Juan"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/filter").param("name", "Juan").param("page", "0")
                .param("size", "10").param("sortBy", "id").param("direction", "asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void searchUsersByName_empty() throws Exception {
        when(userService.findByName(eq("Unknown"), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users/filter").param("name", "Unknown"))
                .andExpect(status().isNotFound());
    }

    // DELETE
    @Test
    void deleteUser_success() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1")).andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userService).delete(999L);

        mockMvc.perform(delete("/api/users/999")).andExpect(status().isInternalServerError());
    }
}
