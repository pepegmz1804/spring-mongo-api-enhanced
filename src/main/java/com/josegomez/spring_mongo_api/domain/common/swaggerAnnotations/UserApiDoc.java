package com.josegomez.spring_mongo_api.domain.common.swaggerAnnotations;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import com.josegomez.spring_mongo_api.domain.dto.UserRequestDTO;
import com.josegomez.spring_mongo_api.domain.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

// The `USerApiDoc` interface is a contract for documenting
// the API endpoints related to users. It includes method signatures with annotations from the
// Swagger library to describe the operations, parameters, responses, and
// examples for each API endpoint related to users.
public interface UserApiDoc {

  // CREATE
  @Operation(summary = "Create new user",
      description = "Creates a new user. Requires a valid DTO with name, email, password, and roles. ID is auto-generated.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User created successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)})
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "DTO containing user information. Required fields: firstName, lastNamePaternal, lastNameMaternal, email, password, roleKeys.",
      required = true,
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = UserRequestDTO.class),
          examples = @ExampleObject(value = """
              {
                "firstName": "Juan",
                "lastNamePaternal": "Gomez",
                "lastNameMaternal": "Perez",
                "email": "juan.gomez@example.com",
                "password": "StrongP@ssw0rd",
                "roleKeys": ["admin", "user"]
              }
              """)))
  public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO user);

  // UPDATE
  @Operation(summary = "Update existing user",
      description = "Updates a user by ID. Requires a valid DTO with new values. Returns 404 if not found.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
      @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "DTO containing updated user information. Required fields include: firstName, lastNamePaternal, lastNameMaternal, aund roleKeys."
          + "Using roleKeys instead of id to force the user to make a conscious choice",
      required = true,
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = UserRequestDTO.class),
          examples = @ExampleObject(value = """
              {
                "firstName": "Juan",
                "lastNamePaternal": "Gomez",
                "lastNameMaternal": "Perez",
                "roleKeys": ["admin", "user"]
              }
              """)))
  ResponseEntity<UserResponseDTO> update(
      @Parameter(description = "ID of the user to update", example = "1", required = true) Long id,

      @Valid @RequestBody UserRequestDTO userRequestDTO);

  // GET ALL
  @Operation(summary = "Get paginated users",
      description = "Returns a paginated list of users. Use `all=true` to get all users without pagination.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Users retrieved successfully",
      content = @Content(mediaType = "application/json",
          schema = @Schema(implementation = UserResponseDTO.class)))})
  ResponseEntity<Page<UserResponseDTO>> getAll(
      @Parameter(description = "Page number (0-based)", example = "0") int page,

      @Parameter(description = "Items per page", example = "10") int size,

      @Parameter(description = "Field to sort by", example = "id") String sortBy,

      @Parameter(description = "Sort direction", example = "asc") String direction,

      @Parameter(description = "If true, fetches all users ignoring pagination",
          example = "false") boolean all);

  // GET BY ID
  @Operation(summary = "Get user by ID",
      description = "Fetches a user by their unique numeric ID. Includes roles.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
  ResponseEntity<UserResponseDTO> getById(
      @Parameter(description = "User ID", example = "1", required = true) Long id);

  // SEARCH BY NAME
  @Operation(summary = "Search users by name",
      description = "Searches for users by name substring (case-insensitive). Supports pagination and sorting.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Matching users found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponseDTO.class))),
      @ApiResponse(responseCode = "404", description = "No users matched the search",
          content = @Content)})

  // SEARCH BY NAME
  ResponseEntity<Page<UserResponseDTO>> searchByName(
      @Parameter(description = "Name substring to search", example = "juan",
          required = true) String name,

      @Parameter(description = "Page number", example = "0") int page,

      @Parameter(description = "Items per page", example = "10") int size,

      @Parameter(description = "Sort field", example = "name") String sortBy,

      @Parameter(description = "Sort direction", example = "asc") String direction);

  // DELETE
  @Operation(summary = "Delete user",
      description = "Deletes a user by ID. Returns 204 if successful or 404 if user does not exist.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "User deleted successfully",
          content = @Content),
      @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
  ResponseEntity<Void> delete(
      @Parameter(description = "ID of the user to delete", example = "5", required = true) Long id);
}
