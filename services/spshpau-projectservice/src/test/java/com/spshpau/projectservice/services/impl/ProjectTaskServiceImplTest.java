package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.TaskCreateDto;
import com.spshpau.projectservice.dto.TaskResponseDto;
import com.spshpau.projectservice.dto.TaskUpdateDto;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.ProjectTask;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.model.enums.TaskStatus;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.repositories.ProjectTaskRepository;
import com.spshpau.projectservice.services.ProjectService;
import com.spshpau.projectservice.services.SimpleUserService;
import com.spshpau.projectservice.services.exceptions.ProjectNotFoundException;
import com.spshpau.projectservice.services.exceptions.TaskNotFoundException;
import com.spshpau.projectservice.services.exceptions.UnauthorizedOperationException;
import com.spshpau.projectservice.services.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectTaskServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectTaskRepository projectTaskRepository;
    @Mock
    private SimpleUserService simpleUserService;
    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectTaskServiceImpl projectTaskService;

    private UUID projectId;
    private UUID taskId;
    private UUID currentUserId;
    private UUID assigneeUserId;

    private Project project;
    private SimpleUser currentUser;
    private SimpleUser assigneeUser;
    private ProjectTask projectTask;
    private TaskCreateDto taskCreateDto;
    private TaskUpdateDto taskUpdateDto;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
        assigneeUserId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);
        project.setTitle("Test Project");

        currentUser = new SimpleUser(currentUserId, "currentUser", "Current", "User", "Location", null, null, null);
        assigneeUser = new SimpleUser(assigneeUserId, "assigneeUser", "Assignee", "User", "Location", null, null, null);

        projectTask = new ProjectTask();
        projectTask.setId(taskId);
        projectTask.setProject(project);
        projectTask.setTitle("Test Task");
        projectTask.setDescription("Test Description");
        projectTask.setStatus(TaskStatus.TODO);
        projectTask.setCreatedAt(Timestamp.from(Instant.now()));

        taskCreateDto = new TaskCreateDto();
        taskCreateDto.setTitle("New Task");
        taskCreateDto.setDescription("New Description");
        taskCreateDto.setStatus(TaskStatus.TODO);

        taskUpdateDto = new TaskUpdateDto();
        taskUpdateDto.setTitle("Updated Task Title");
        taskUpdateDto.setStatus(TaskStatus.IN_PROGRESS);
    }

    // --- createTask Tests ---
    @Test
    void createTask_success_noAssignee() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(invocation -> {
            ProjectTask task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        TaskResponseDto result = projectTaskService.createTask(projectId, taskCreateDto, currentUserId);

        assertNotNull(result);
        assertEquals(taskCreateDto.getTitle(), result.getTitle());
        assertNull(result.getAssignedUser());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectTaskRepository, times(1)).save(any(ProjectTask.class));
    }

    @Test
    void createTask_success_withAssignee() {
        taskCreateDto.setAssignedUserId(assigneeUserId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(simpleUserService.findUserById(assigneeUserId)).thenReturn(assigneeUser);
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(invocation -> {
            ProjectTask task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        TaskResponseDto result = projectTaskService.createTask(projectId, taskCreateDto, currentUserId);

        assertNotNull(result);
        assertEquals(taskCreateDto.getTitle(), result.getTitle());
        assertNotNull(result.getAssignedUser());
        assertEquals(assigneeUserId, result.getAssignedUser().getId());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, assigneeUserId);
        verify(simpleUserService, times(1)).findUserById(assigneeUserId);
        verify(projectTaskRepository, times(1)).save(any(ProjectTask.class));
    }

    @Test
    void createTask_fail_projectNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> {
            projectTaskService.createTask(projectId, taskCreateDto, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    @Test
    void createTask_fail_currentUserNotMember() {
        doThrow(new UnauthorizedOperationException("User not member")).when(projectService).verifyUserIsProjectMember(projectId, currentUserId);

        assertThrows(UnauthorizedOperationException.class, () -> {
            projectTaskService.createTask(projectId, taskCreateDto, currentUserId);
        });
    }

    @Test
    void createTask_fail_assigneeUserNotFound() {
        taskCreateDto.setAssignedUserId(assigneeUserId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(simpleUserService.findUserById(assigneeUserId)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> {
            projectTaskService.createTask(projectId, taskCreateDto, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, assigneeUserId);
    }


    // --- getTaskById Tests ---
    @Test
    void getTaskById_success() {
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));

        TaskResponseDto result = projectTaskService.getTaskById(projectId, taskId, currentUserId);

        assertNotNull(result);
        assertEquals(projectTask.getTitle(), result.getTitle());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    @Test
    void getTaskById_fail_taskNotFound() {
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            projectTaskService.getTaskById(projectId, taskId, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    @Test
    void getTaskById_fail_taskDoesNotBelongToProject() {
        Project otherProject = new Project();
        otherProject.setId(UUID.randomUUID());
        projectTask.setProject(otherProject);

        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));

        assertThrows(UnauthorizedOperationException.class, () -> {
            projectTaskService.getTaskById(projectId, taskId, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    // --- getTasksForProject Tests ---
    @Test
    void getTasksForProject_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProjectTask> taskPage = new PageImpl<>(Collections.singletonList(projectTask), pageable, 1);
        when(projectTaskRepository.findByProjectId(projectId, pageable)).thenReturn(taskPage);

        Page<TaskResponseDto> resultPage = projectTaskService.getTasksForProject(projectId, currentUserId, pageable);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(projectTask.getTitle(), resultPage.getContent().get(0).getTitle());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    // --- updateTask Tests ---
    @Test
    void updateTask_success() {
        projectTask.setAssignedUser(null);
        taskUpdateDto.setAssignedUserId(assigneeUserId);

        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
        when(simpleUserService.findUserById(assigneeUserId)).thenReturn(assigneeUser);
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto result = projectTaskService.updateTask(projectId, taskId, taskUpdateDto, currentUserId);

        assertNotNull(result);
        assertEquals(taskUpdateDto.getTitle(), result.getTitle());
        assertEquals(taskUpdateDto.getStatus(), result.getStatus());
        assertNotNull(result.getAssignedUser());
        assertEquals(assigneeUserId, result.getAssignedUser().getId());

        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, assigneeUserId);
        verify(simpleUserService, times(1)).findUserById(assigneeUserId);
        verify(projectTaskRepository, times(1)).save(any(ProjectTask.class));
    }

    @Test
    void updateTask_success_unassignUser() {
        projectTask.setAssignedUser(assigneeUser);
        taskUpdateDto.setAssignedUserId(null);

        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto result = projectTaskService.updateTask(projectId, taskId, taskUpdateDto, currentUserId);

        assertNotNull(result);
        assertEquals(taskUpdateDto.getTitle(), result.getTitle());
        assertNull(result.getAssignedUser());

        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(simpleUserService, never()).findUserById(any());
        verify(projectTaskRepository, times(1)).save(any(ProjectTask.class));
    }


    @Test
    void updateTask_fail_taskNotFound() {
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            projectTaskService.updateTask(projectId, taskId, taskUpdateDto, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    // --- deleteTask Tests ---
    @Test
    void deleteTask_success() {
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
        doNothing().when(projectTaskRepository).delete(projectTask);

        projectTaskService.deleteTask(projectId, taskId, currentUserId);

        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectTaskRepository, times(1)).delete(projectTask);
    }

    @Test
    void deleteTask_fail_taskNotFound() {
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> {
            projectTaskService.deleteTask(projectId, taskId, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    // --- assignUserToTask Tests ---
    @Test
    void assignUserToTask_success() {
        projectTask.setAssignedUser(null);
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
        when(simpleUserService.findUserById(assigneeUserId)).thenReturn(assigneeUser);
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto result = projectTaskService.assignUserToTask(projectId, taskId, assigneeUserId, currentUserId);

        assertNotNull(result);
        assertNotNull(result.getAssignedUser());
        assertEquals(assigneeUserId, result.getAssignedUser().getId());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, assigneeUserId);
        verify(simpleUserService, times(1)).findUserById(assigneeUserId);
        verify(projectTaskRepository, times(1)).save(projectTask);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void assignUserToTask_fail_assigneeNotMember() {
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));

        doNothing().when(projectService).verifyUserIsProjectMember(projectId, currentUserId);

        doThrow(new UnauthorizedOperationException("Assignee not member"))
                .when(projectService).verifyUserIsProjectMember(projectId, assigneeUserId);

        assertThrows(UnauthorizedOperationException.class, () -> {
            projectTaskService.assignUserToTask(projectId, taskId, assigneeUserId, currentUserId);
        });

        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, assigneeUserId);
        verify(simpleUserService, never()).findUserById(any());
        verify(projectTaskRepository, never()).save(any());
    }


    // --- removeUserFromTask Tests ---
    @Test
    void removeUserFromTask_success_userWasAssigned() {
        projectTask.setAssignedUser(assigneeUser);
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto result = projectTaskService.removeUserFromTask(projectId, taskId, currentUserId);

        assertNotNull(result);
        assertNull(result.getAssignedUser());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectTaskRepository, times(1)).save(projectTask);
    }

    @Test
    void removeUserFromTask_success_userWasNotAssigned() {
        projectTask.setAssignedUser(null);
        when(projectTaskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto result = projectTaskService.removeUserFromTask(projectId, taskId, currentUserId);

        assertNotNull(result);
        assertNull(result.getAssignedUser());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectTaskRepository, times(1)).save(projectTask);
    }
}