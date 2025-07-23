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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.josegomez.spring_mongo_api.domain.dto.RoleRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.RoleResponseDTO;
import com.josegomez.spring_mongo_api.service.RoleService;


@WebMvcTest(RoleController.class)
@Disabled
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    // CREATE
    @Test
    void createRole_Success() throws Exception {
        RoleRequestDTO request = new RoleRequestDTO("admin", "Administrator");
        RoleResponseDTO response = new RoleResponseDTO(1L, "admin", "Administrator");

        when(roleService.save(any(RoleRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/roles/1"))
                .andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.key").value("admin"))
                .andExpect(jsonPath("$.name").value("Administrator"));
    }

    @Test
    void createRole_InvalidInput_BadRequest() throws Exception {
        String badJson = """
                {
                    "key": "admin",
                    "name": ""
                }
                """;

        mockMvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON).content(badJson))
                .andExpect(status().isBadRequest());
    }

    // UPDATE
    @Test
    void updateRole_Success() throws Exception {
        Long id = 1L;
        RoleRequestDTO request = new RoleRequestDTO("mod", "Moderator");
        RoleResponseDTO response = new RoleResponseDTO(id, "mod", "Moderator");

        when(roleService.update(eq(id), any(RoleRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/roles/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.key").value("mod"))
                .andExpect(jsonPath("$.name").value("Moderator"));
    }

    @Test
    void updateRole_NotFound() throws Exception {
        Long id = 999L;
        RoleRequestDTO request = new RoleRequestDTO("mod", "Moderator");

        when(roleService.update(eq(id), any(RoleRequestDTO.class)))
                .thenThrow(new RuntimeException("Role not found"));

        mockMvc.perform(put("/api/roles/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // GET ALL
    @Test
    void getAllRoles_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("key").ascending());
        List<RoleResponseDTO> roles = List.of(new RoleResponseDTO(1L, "admin", "Administrator"));
        Page<RoleResponseDTO> page = new PageImpl<>(roles, pageable, roles.size());

        when(roleService.findAll(any(Pageable.class), eq(false))).thenReturn(page);

        mockMvc.perform(get("/api/roles").param("page", "0").param("size", "10")
                .param("sortBy", "key").param("direction", "asc").param("all", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].key").value("admin"));
    }

    @Test
    void getAllRoles_Empty() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RoleResponseDTO> emptyPage = Page.empty(pageable);

        when(roleService.findAll(any(Pageable.class), eq(false))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/roles").param("page", "0").param("size", "10")
                .param("sortBy", "key").param("direction", "asc").param("all", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }

    // GET BY ID
    @Test
    void getRoleById_Success() throws Exception {
        Long id = 1L;
        RoleResponseDTO response = new RoleResponseDTO(id, "admin", "Administrator");

        when(roleService.findById(id)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/roles/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.key").value("admin"));
    }

    @Test
    void getRoleById_NotFound() throws Exception {
        Long id = 999L;

        when(roleService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roles/{id}", id)).andExpect(status().isNotFound());
    }

    // FILTER
    @Test
    void searchRoleByName_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        List<RoleResponseDTO> roles = List.of(new RoleResponseDTO(1L, "admin", "Administrator"));
        Page<RoleResponseDTO> page = new PageImpl<>(roles, pageable, roles.size());

        when(roleService.findByName(eq("admin"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/roles/filter").param("name", "admin").param("page", "0")
                .param("size", "10").param("sortBy", "name").param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Administrator"));
    }

    @Test
    void searchRoleByName_NotFound() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RoleResponseDTO> emptyPage = Page.empty(pageable);

        when(roleService.findByName(eq("nonexistent"), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/roles/filter").param("name", "nonexistent").param("page", "0")
                .param("size", "10").param("sortBy", "name").param("direction", "asc"))
                .andExpect(status().isNotFound());
    }

    // DELETE
    @Test
    void deleteRole_Success() throws Exception {
        Long id = 1L;

        doNothing().when(roleService).delete(id);

        mockMvc.perform(delete("/api/roles/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void deleteRole_NotFound() throws Exception {
        Long id = 999L;

        doThrow(new RuntimeException("Role not found")).when(roleService).delete(id);

        mockMvc.perform(delete("/api/roles/{id}", id)).andExpect(status().isInternalServerError());
    }


}
