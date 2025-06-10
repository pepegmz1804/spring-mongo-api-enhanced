package com.josegomez.spring_mongo_api.domain.common.swaggerAnnotations;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import com.josegomez.spring_mongo_api.domain.dto.RoleRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.RoleResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

// The `RoleApiDoc` interface is a contract for documenting
// the API endpoints related to roles. It includes method signatures with annotations from the
// Swagger library to describe the operations, parameters, responses, and
// examples for each API endpoint related to roles.
public interface RoleApiDoc {

        // CREATE
        @Operation(summary = "Create a new role",
                        description = "Creates a new role with a unique key and name. Both fields are required. "
                                        + "The role ID is automatically generated and returned in the response.")
        @ApiResponses(value = {@ApiResponse(responseCode = "201",
                        description = "Role created successfully",
                        content = @Content(mediaType = "application/json",
                                        examples = @ExampleObject(name = "Created Role Example",
                                                        value = """
                                                                            {
                                                                                "id": 15,
                                                                                "key": "admin_finance",
                                                                                "name": "Admin Finance"
                                                                            }
                                                                        """),
                                        schema = @Schema(implementation = RoleResponseDTO.class))),
                        @ApiResponse(responseCode = "400",
                                        description = "Invalid input data (e.g., missing fields or invalid format)",
                                        content = @Content),
                        @ApiResponse(responseCode = "409",
                                        description = "Role key or name already exists",
                                        content = @Content)})
        ResponseEntity<RoleResponseDTO> create(
                        @Parameter(description = "DTO containing role key and name",
                                        required = true) RoleRequestDTO role);

        // UPDATE
        @Operation(summary = "Update existing role",
                        description = "Updates an existing role by its ID. Requires a valid RoleRequestDTO in the request body. "
                                        + "Returns the updated role information.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Role updated successfully",
                        content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = RoleResponseDTO.class))),
                        @ApiResponse(responseCode = "400",
                                        description = "Invalid input data (e.g., missing fields, wrong ID format)",
                                        content = @Content),
                        @ApiResponse(responseCode = "404", description = "Role not found",
                                        content = @Content),
                        @ApiResponse(responseCode = "409",
                                        description = "Role key or name already exists (conflict)",
                                        content = @Content)})
        ResponseEntity<RoleResponseDTO> update(
                        @Parameter(description = "ID of the role to update", required = true,
                                        example = "1") Long id,

                        @Parameter(description = "DTO containing updated key and name",
                                        required = true) RoleRequestDTO roleRequestDTO);

        // GET ALL
        @Operation(summary = "Get paginated list of roles",
                        description = "Retrieves a paginated list of roles. Use `all=true` to fetch the full list without pagination.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Roles retrieved successfully",
                        content = @Content(mediaType = "application/json",
                                        examples = @ExampleObject(name = "Example Response",
                                                        summary = "Sample paginated roles",
                                                        value = """
                                                                        {
                                                                            "content": [
                                                                                {"id": 1, "key": "admin", "name": "Admin"},
                                                                                {"id": 2, "key": "moderator", "name": "Moderator"},
                                                                                {"id": 3, "key": "admin_edit_test", "name": "Admin edit test"},
                                                                                {"id": 4, "key": "super_admin", "name": "Admin super"},
                                                                                {"id": 5, "key": "mega_admin", "name": "Admin mega"},
                                                                                {"id": 11, "key": "mega_admin_dos", "name": "Admin mega dos"}
                                                                            ],
                                                                            "page": {
                                                                                "size": 10,
                                                                                "number": 0,
                                                                                "totalElements": 6,
                                                                                "totalPages": 1
                                                                            }
                                                                        }
                                                                        """)))})
        ResponseEntity<Page<RoleResponseDTO>> getAll(
                        @Parameter(description = "Page number (0-based index)",
                                        example = "0") int page,

                        @Parameter(description = "Number of roles per page",
                                        example = "10") int size,

                        @Parameter(description = "Field by which to sort",
                                        example = "id") String sortBy,

                        @Parameter(description = "Sorting direction: 'asc' or 'desc'",
                                        example = "asc") String direction,

                        @Parameter(description = "Set to true to ignore pagination and fetch all roles",
                                        example = "false") boolean all);

        // GET BY ID
        @Operation(summary = "Get role by ID",
                        description = "Retrieves a single role by its unique numeric ID. Returns 404 if not found.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Role found successfully",
                        content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = RoleResponseDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Role not found",
                                        content = @Content)})
        ResponseEntity<RoleResponseDTO> getById(@Parameter(description = "ID of the role",
                        required = true, example = "1") Long id);

        // SEACH BY NAME
        @Operation(summary = "Search roles by name",
                        description = "Finds roles that contain the given name substring (case-insensitive). "
                                        + "Supports pagination and sorting. Returns 404 if no results are found.")
        @ApiResponses(value = {@ApiResponse(responseCode = "200",
                        description = "Matching roles found",
                        content = @Content(mediaType = "application/json",
                                        examples = @ExampleObject(name = "Filtered Roles Example",
                                                        summary = "Filtered roles with name containing 'admin'",
                                                        value = """
                                                                        {
                                                                            "content": [
                                                                                { "id": 1, "key": "admin", "name": "Admin" },
                                                                                { "id": 3, "key": "admin_edit_test", "name": "Admin edit test" },
                                                                                { "id": 4, "key": "super_admin", "name": "Admin super" },
                                                                                { "id": 5, "key": "mega_admin", "name": "Admin mega" },
                                                                                { "id": 11, "key": "mega_admin_dos", "name": "Admin mega dos" }
                                                                            ],
                                                                            "page": {
                                                                                "size": 10,
                                                                                "number": 0,
                                                                                "totalElements": 5,
                                                                                "totalPages": 1
                                                                            }
                                                                        }
                                                                        """))),
                        @ApiResponse(responseCode = "404",
                                        description = "No roles matched the criteria",
                                        content = @Content)})
        ResponseEntity<Page<RoleResponseDTO>> searchByName(
                        @Parameter(description = "Substring to search in role names",
                                        required = true, example = "admin") String name,

                        @Parameter(description = "Page number (0-based index)",
                                        example = "0") int page,

                        @Parameter(description = "Number of roles per page",
                                        example = "10") int size,

                        @Parameter(description = "Field to sort by",
                                        example = "name") String sortBy,

                        @Parameter(description = "Sorting direction: 'asc' or 'desc'",
                                        example = "desc") String direction);

        // DELETE
        @Operation(summary = "Delete role by ID",
                        description = "Deletes a role identified by its unique numeric ID. Returns 204 No Content if deleted successfully, or 404 if the role does not exist.")
        @ApiResponses(value = {@ApiResponse(responseCode = "204",
                        description = "Role deleted successfully", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Role not found",
                                        content = @Content)})
        ResponseEntity<Void> delete(@Parameter(description = "ID of the role to delete",
                        required = true, example = "42") Long id);
}
