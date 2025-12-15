package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.ProjectCreateDto;
import com.spshpau.projectservice.dto.ProjectResponseDto;
import com.spshpau.projectservice.dto.ProjectUpdateDto;
import com.spshpau.projectservice.dto.UserSummaryDto;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.otherservices.UserClient;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.repositories.ProjectTaskRepository;
import com.spshpau.projectservice.services.SimpleUserService;
import com.spshpau.projectservice.services.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectTaskRepository projectTaskRepository;
    @Mock
    private SimpleUserService simpleUserService;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID projectId;
    private UUID ownerId;
    private UUID collaboratorId;
    private UUID anotherUserId;

    private SimpleUser owner;
    private SimpleUser collaborator;
    private SimpleUser anotherUser;

    private Project project;
    private ProjectCreateDto projectCreateDto;
    private ProjectUpdateDto projectUpdateDto;
    private UserSummaryDto ownerSummaryDto;
    private UserSummaryDto collaboratorSummaryDto;
    private Pageable pageable;
    private String bearerToken = "Bearer test-token";

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        collaboratorId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();

        owner = new SimpleUser(ownerId, "ownerUser", "Owner", "User", "OwnerLocation", new HashSet<>(), new HashSet<>(), new HashSet<>());
        collaborator = new SimpleUser(collaboratorId, "collabUser", "Collab", "Orator", "CollabLocation", new HashSet<>(), new HashSet<>(), new HashSet<>());
        anotherUser = new SimpleUser(anotherUserId, "anotherUser", "Another", "Person", "AnyLocation", new HashSet<>(), new HashSet<>(), new HashSet<>());


        project = new Project();
        project.setId(projectId);
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setOwner(owner);
        project.setCollaborators(new HashSet<>(Collections.singletonList(collaborator)));
        owner.getOwnedProjects().add(project);
        collaborator.getCollaboratingProjects().add(project);


        projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setTitle("New Project Title");
        projectCreateDto.setDescription("New Project Description");

        projectUpdateDto = new ProjectUpdateDto();
        projectUpdateDto.setTitle("Updated Project Title");
        projectUpdateDto.setDescription("Updated Project Description");

        ownerSummaryDto = UserSummaryDto.fromEntity(owner);
        collaboratorSummaryDto = UserSummaryDto.fromEntity(collaborator);

        pageable = PageRequest.of(0, 10);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.findById(not(eq(projectId)))).thenReturn(Optional.empty());
        when(simpleUserService.findUserById(ownerId)).thenReturn(owner);
        when(simpleUserService.findUserById(collaboratorId)).thenReturn(collaborator);
        when(simpleUserService.findUserById(anotherUserId)).thenReturn(anotherUser);
    }

    // --- createProject ---
    @Test
    void createProject_success() {
        when(simpleUserService.getOrCreateSimpleUser(ownerId, "ownerUser", "OwnerF", "OwnerL", "OwnerLoc"))
                .thenReturn(owner);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProjectResponseDto response = projectService.createProject(projectCreateDto, ownerId, "ownerUser", "OwnerF", "OwnerL", "OwnerLoc");

        assertNotNull(response);
        assertEquals(projectCreateDto.getTitle(), response.getTitle());
        assertEquals(ownerId, response.getOwner().getId());
        verify(projectRepository).save(any(Project.class));
    }

    // --- getProjectById ---
    @Test
    void getProjectById_asOwner_success() {
        ProjectResponseDto response = projectService.getProjectById(projectId, ownerId);
        assertNotNull(response);
        assertEquals(projectId, response.getId());
    }

    @Test
    void getProjectById_asCollaborator_success() {
        ProjectResponseDto response = projectService.getProjectById(projectId, collaboratorId);
        assertNotNull(response);
        assertEquals(projectId, response.getId());
    }

    @Test
    void getProjectById_asNonMember_throwsUnauthorized() {
        assertThrows(UnauthorizedOperationException.class, () -> {
            projectService.getProjectById(projectId, anotherUserId);
        });
    }

    @Test
    void getProjectById_notFound_throwsProjectNotFound() {
        UUID nonExistentProjectId = UUID.randomUUID();
        assertThrows(ProjectNotFoundException.class, () -> {
            projectService.getProjectById(nonExistentProjectId, ownerId);
        });
    }

    // --- getOwnedProjects ---
    @Test
    void getOwnedProjects_success() {
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project), pageable, 1);
        when(projectRepository.findByOwnerId(ownerId, pageable)).thenReturn(projectPage);

        Page<ProjectResponseDto> response = projectService.getOwnedProjects(ownerId, pageable);
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(projectId, response.getContent().get(0).getId());
    }

    // --- getCollaboratingProjects ---
    @Test
    void getCollaboratingProjects_success() {
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project), pageable, 1);
        when(projectRepository.findByCollaboratorsId(collaboratorId, pageable)).thenReturn(projectPage);

        Page<ProjectResponseDto> response = projectService.getCollaboratingProjects(collaboratorId, pageable);
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(projectId, response.getContent().get(0).getId());
    }

    // --- getProjectOwner ---
    @Test
    void getProjectOwner_success() {
        UserSummaryDto response = projectService.getProjectOwner(projectId);
        assertNotNull(response);
        assertEquals(ownerId, response.getId());
    }

    // --- getProjectCollaborators ---
    @Test
    void getProjectCollaborators_success() {
        Page<UserSummaryDto> response = projectService.getProjectCollaborators(projectId, pageable);
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(collaboratorId, response.getContent().get(0).getId());
    }

    @Test
    void getProjectCollaborators_noCollaborators_success() {
        project.setCollaborators(new HashSet<>());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Page<UserSummaryDto> response = projectService.getProjectCollaborators(projectId, pageable);
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
    }


    // --- updateProject ---
    @Test
    void updateProject_success() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        ProjectResponseDto response = projectService.updateProject(projectId, projectUpdateDto, ownerId);

        assertNotNull(response);
        assertEquals(projectUpdateDto.getTitle(), response.getTitle());
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_notOwner_throwsUnauthorized() {
        assertThrows(UnauthorizedOperationException.class, () -> {
            projectService.updateProject(projectId, projectUpdateDto, collaboratorId);
        });
    }

    // --- deleteProject ---
    @Test
    void deleteProject_success() {
        doNothing().when(projectRepository).delete(project);
        projectService.deleteProject(projectId, ownerId);
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_notOwner_throwsUnauthorized() {
        assertThrows(UnauthorizedOperationException.class, () -> {
            projectService.deleteProject(projectId, collaboratorId);
        });
    }

    // --- addCollaborator ---
    @Test
    void addCollaborator_success() {
        UUID newCollaboratorId = UUID.randomUUID();
        SimpleUser newCollaborator = new SimpleUser(newCollaboratorId, "newCollab", "New", "Collab", "Loc", new HashSet<>(), new HashSet<>(), new HashSet<>());
        UserSummaryDto newCollaboratorSummary = UserSummaryDto.fromEntity(newCollaborator);

        when(userClient.findConnectionsByJwt(bearerToken)).thenReturn(Collections.singletonList(newCollaboratorSummary));
        when(simpleUserService.getOrCreateSimpleUser(newCollaboratorSummary)).thenReturn(newCollaborator);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponseDto response = projectService.addCollaborator(projectId, newCollaboratorId, ownerId, bearerToken);

        assertNotNull(response);
        assertTrue(response.getCollaborators().stream().anyMatch(c -> c.getId().equals(newCollaboratorId)));
        assertTrue(project.getCollaborators().contains(newCollaborator));
        assertTrue(newCollaborator.getCollaboratingProjects().contains(project));
        verify(projectRepository).save(project);
    }

    @Test
    void addCollaborator_notOwner_throwsUnauthorized() {
        assertThrows(UnauthorizedOperationException.class, () -> {
            projectService.addCollaborator(projectId, anotherUserId, collaboratorId, bearerToken);
        });
    }

    @Test
    void addCollaborator_alreadyExists_throwsCollaboratorAlreadyExists() {
        assertThrows(CollaboratorAlreadyExistsException.class, () -> {
            projectService.addCollaborator(projectId, collaboratorId, ownerId, bearerToken);
        });
    }

    @Test
    void addCollaborator_ownerAsCollaborator_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.addCollaborator(projectId, ownerId, ownerId, bearerToken);
        });
    }


    @Test
    void addCollaborator_notConnected_throwsNotConnected() {
        UUID nonConnectionId = UUID.randomUUID();
        when(userClient.findConnectionsByJwt(bearerToken)).thenReturn(Collections.emptyList());

        assertThrows(NotConnectedException.class, () -> {
            projectService.addCollaborator(projectId, nonConnectionId, ownerId, bearerToken);
        });
    }

    // --- removeCollaborator ---
    @Test
    void removeCollaborator_success() {
        assertTrue(project.getCollaborators().contains(collaborator));

        doNothing().when(projectTaskRepository).unassignUserFromTasksInProject(projectId, collaboratorId);
        when(projectRepository.save(any(Project.class))).thenReturn(project);


        projectService.removeCollaborator(projectId, collaboratorId, ownerId);

        assertFalse(project.getCollaborators().contains(collaborator));
        assertFalse(collaborator.getCollaboratingProjects().contains(project));
        verify(projectTaskRepository).unassignUserFromTasksInProject(projectId, collaboratorId);
        verify(projectRepository).save(project);
    }

    @Test
    void removeCollaborator_notOwner_throwsUnauthorized() {
        assertThrows(UnauthorizedOperationException.class, () -> {
            projectService.removeCollaborator(projectId, collaboratorId, anotherUserId);
        });
    }

    @Test
    void removeCollaborator_collaboratorNotFoundInProject_throwsCollaboratorNotFound() {
        project.getCollaborators().remove(collaborator);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(CollaboratorNotFoundException.class, () -> {
            projectService.removeCollaborator(projectId, collaboratorId, ownerId);
        });
    }

    // --- verifyUserIsProjectMember ---
    @Test
    void verifyUserIsProjectMember_isOwner_doesNotThrow() {
        assertDoesNotThrow(() -> {
            projectService.verifyUserIsProjectMember(projectId, ownerId);
        });
    }

    @Test
    void verifyUserIsProjectMember_isCollaborator_doesNotThrow() {
        assertDoesNotThrow(() -> {
            projectService.verifyUserIsProjectMember(projectId, collaboratorId);
        });
    }

    @Test
    void verifyUserIsProjectMember_isNotMember_throwsUnauthorized() {
        assertThrows(UnauthorizedOperationException.class, () -> {
            projectService.verifyUserIsProjectMember(projectId, anotherUserId);
        });
    }

    // --- isUserOwnerOfProject ---
    @Test
    void isUserOwnerOfProject_isOwner_returnsTrue() {
        assertTrue(projectService.isUserOwnerOfProject(projectId, ownerId));
    }

    @Test
    void isUserOwnerOfProject_isNotOwner_returnsFalse() {
        assertFalse(projectService.isUserOwnerOfProject(projectId, collaboratorId));
    }
}