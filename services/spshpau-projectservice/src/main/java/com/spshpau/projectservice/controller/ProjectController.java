package com.spshpau.projectservice.controller;

import com.spshpau.projectservice.dto.ProjectCreateDto;
import com.spshpau.projectservice.dto.ProjectResponseDto;
import com.spshpau.projectservice.dto.ProjectUpdateDto;
import com.spshpau.projectservice.dto.UserSummaryDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

public interface ProjectController {
    /**
     * Creates a new project.
     *
     * @param projectDto The project creation data.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the created ProjectResponseDto and HTTP status.
     * Example Response (201 Created):
     * <pre>{@code
     * {
     * "id": "c1d2e3f4-g5h6-7890-1234-567890abcdef",
     * "title": "New Awesome Project",
     * "description": "This is a description of the new project.",
     * "owner": {
     * "id": "user-uuid-123",
     * "username": "project_owner",
     * "firstName": "John",
     * "lastName": "Doe",
     * "location": "New York"
     * },
     * "collaborators": []
     * }
     * }</pre>
     */
    ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectCreateDto projectDto, Jwt jwt);

    /**
     * Retrieves a project by its ID.
     *
     * @param projectId The ID of the project to retrieve.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the ProjectResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "c1d2e3f4-g5h6-7890-1234-567890abcdef",
     * "title": "New Awesome Project",
     * "description": "This is a description of the new project.",
     * "owner": {
     * "id": "user-uuid-123",
     * "username": "project_owner",
     * "firstName": "John",
     * "lastName": "Doe",
     * "location": "New York"
     * },
     * "collaborators": [
     * {
     * "id": "user-uuid-456",
     * "username": "collaborator1",
     * "firstName": "Jane",
     * "lastName": "Smith",
     * "location": "London"
     * }
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable UUID projectId, Jwt jwt);

    /**
     * Retrieves a paginated list of projects owned by the current authenticated user.
     *
     * @param jwt The JWT token for authentication and authorization.
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a Page of ProjectResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {
     * "id": "c1d2e3f4-g5h6-7890-1234-567890abcdef",
     * "title": "My First Project",
     * "description": "Description of first project.",
     * "owner": {"id": "user-uuid-123", "username": "current_user", ...},
     * "collaborators": []
     * }
     * ],
     * "pageable": {"offset": 0, "pageSize": 10, ...},
     * "totalElements": 1,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<ProjectResponseDto>> getMyOwnedProjects(Jwt jwt, Pageable pageable);

    /**
     * Retrieves a paginated list of projects where the current authenticated user is a collaborator.
     *
     * @param jwt The JWT token for authentication and authorization.
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a Page of ProjectResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {
     * "id": "d2e3f4g5-h6i7-8901-2345-678901bcdefa",
     * "title": "Collaborative Work",
     * "description": "Project I am collaborating on.",
     * "owner": {"id": "user-uuid-789", "username": "owner_user", ...},
     * "collaborators": [{"id": "user-uuid-123", "username": "current_user", ...}]
     * }
     * ],
     * "pageable": {"offset": 0, "pageSize": 10, ...},
     * "totalElements": 1,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<ProjectResponseDto>> getMyCollaboratingProjects(Jwt jwt, Pageable pageable);

    /**
     * Retrieves the owner of a specified project.
     *
     * @param projectId The ID of the project.
     * @return A ResponseEntity containing the UserSummaryDto of the project owner and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "user-uuid-123",
     * "username": "project_owner",
     * "firstName": "John",
     * "lastName": "Doe",
     * "location": "New York"
     * }
     * }</pre>
     */
    ResponseEntity<UserSummaryDto> getProjectOwner(@PathVariable UUID projectId);

    /**
     * Retrieves a paginated list of collaborators for a specified project.
     *
     * @param projectId The ID of the project.
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a Page of UserSummaryDto for the collaborators and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {
     * "id": "user-uuid-456",
     * "username": "collaborator1",
     * "firstName": "Jane",
     * "lastName": "Smith",
     * "location": "London"
     * }
     * ],
     * "pageable": {"offset": 0, "pageSize": 10, ...},
     * "totalElements": 1,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<UserSummaryDto>> getProjectCollaborators(@PathVariable UUID projectId, Pageable pageable);

    /**
     * Updates the information of an existing project.
     *
     * @param projectId The ID of the project to update.
     * @param projectDto The project update data.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the updated ProjectResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "c1d2e3f4-g5h6-7890-1234-567890abcdef",
     * "title": "Updated Awesome Project",
     * "description": "This is an updated description.",
     * "owner": {
     * "id": "user-uuid-123",
     * "username": "project_owner", ...
     * },
     * "collaborators": []
     * }
     * }</pre>
     */
    ResponseEntity<ProjectResponseDto> updateProjectInfo(@PathVariable UUID projectId, @Valid @RequestBody ProjectUpdateDto projectDto, Jwt jwt);

    /**
     * Deletes a project.
     *
     * @param projectId The ID of the project to delete.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity with no content and HTTP status 204.
     */
    ResponseEntity<Void> deleteProject(@PathVariable UUID projectId, Jwt jwt);

    /**
     * Adds a collaborator to a project.
     *
     * @param projectId The ID of the project.
     * @param collaboratorId The ID of the user to add as a collaborator.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity containing the updated ProjectResponseDto and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * {
     * "id": "c1d2e3f4-g5h6-7890-1234-567890abcdef",
     * "title": "Awesome Project with New Collaborator",
     * "description": "Description...",
     * "owner": {"id": "user-uuid-123", ...},
     * "collaborators": [
     * {"id": "user-uuid-new-collaborator", "username": "new_collab", ...}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ProjectResponseDto> addCollaborator(@PathVariable UUID projectId, @PathVariable UUID collaboratorId, Jwt jwt);

    /**
     * Removes a collaborator from a project.
     *
     * @param projectId The ID of the project.
     * @param collaboratorId The ID of the user to remove as a collaborator.
     * @param jwt The JWT token for authentication and authorization.
     * @return A ResponseEntity with no content and HTTP status 204.
     */
    ResponseEntity<Void> removeCollaborator(@PathVariable UUID projectId, @PathVariable UUID collaboratorId, Jwt jwt);
}