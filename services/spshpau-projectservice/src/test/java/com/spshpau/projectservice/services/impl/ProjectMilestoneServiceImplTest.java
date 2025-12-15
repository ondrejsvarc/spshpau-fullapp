package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.MilestoneCreateDto;
import com.spshpau.projectservice.dto.MilestoneResponseDto;
import com.spshpau.projectservice.dto.MilestoneUpdateDto;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.ProjectMilestone;
import com.spshpau.projectservice.repositories.ProjectMilestoneRepository;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.services.ProjectService;
import com.spshpau.projectservice.services.exceptions.MilestoneNotFoundException;
import com.spshpau.projectservice.services.exceptions.ProjectNotFoundException;
import com.spshpau.projectservice.services.exceptions.UnauthorizedOperationException;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectMilestoneServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMilestoneRepository projectMilestoneRepository;
    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectMilestoneServiceImpl projectMilestoneService;

    private UUID projectId;
    private UUID milestoneId;
    private UUID currentUserId;

    private Project project;
    private ProjectMilestone projectMilestone;
    private MilestoneCreateDto milestoneCreateDto;
    private MilestoneUpdateDto milestoneUpdateDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);
        project.setTitle("Test Project");

        projectMilestone = new ProjectMilestone();
        projectMilestone.setId(milestoneId);
        projectMilestone.setProject(project);
        projectMilestone.setTitle("Test Milestone");
        projectMilestone.setDescription("Test Description");
        projectMilestone.setDueDate(Timestamp.from(Instant.now().plusSeconds(86400)));

        milestoneCreateDto = new MilestoneCreateDto();
        milestoneCreateDto.setTitle("New Milestone");
        milestoneCreateDto.setDescription("New Description");
        milestoneCreateDto.setDueDate(Timestamp.from(Instant.now().plusSeconds(172800)));

        milestoneUpdateDto = new MilestoneUpdateDto();
        milestoneUpdateDto.setTitle("Updated Milestone Title");
        milestoneUpdateDto.setDescription("Updated Milestone Description");
        milestoneUpdateDto.setDueDate(Timestamp.from(Instant.now().plusSeconds(259200)));

        pageable = PageRequest.of(0, 10);

        // Common stubs
        doNothing().when(projectService).verifyUserIsProjectMember(projectId, currentUserId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(projectMilestone));
    }

    // --- createMilestone Tests ---
    @Test
    void createMilestone_success() {
        when(projectMilestoneRepository.save(any(ProjectMilestone.class))).thenAnswer(invocation -> {
            ProjectMilestone ms = invocation.getArgument(0);
            ms.setId(UUID.randomUUID());
            return ms;
        });

        MilestoneResponseDto result = projectMilestoneService.createMilestone(projectId, milestoneCreateDto, currentUserId);

        assertNotNull(result);
        assertEquals(milestoneCreateDto.getTitle(), result.getTitle());
        assertEquals(projectId, result.getProjectId());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectRepository, times(1)).findById(projectId);
        verify(projectMilestoneRepository, times(1)).save(any(ProjectMilestone.class));
    }

    @Test
    void createMilestone_fail_projectNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> {
            projectMilestoneService.createMilestone(projectId, milestoneCreateDto, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    @Test
    void createMilestone_fail_userNotProjectMember() {
        doThrow(new UnauthorizedOperationException("User not member")).when(projectService).verifyUserIsProjectMember(projectId, currentUserId);

        assertThrows(UnauthorizedOperationException.class, () -> {
            projectMilestoneService.createMilestone(projectId, milestoneCreateDto, currentUserId);
        });
    }

    // --- getMilestoneById Tests ---
    @Test
    void getMilestoneById_success() {
        MilestoneResponseDto result = projectMilestoneService.getMilestoneById(projectId, milestoneId, currentUserId);

        assertNotNull(result);
        assertEquals(projectMilestone.getTitle(), result.getTitle());
        assertEquals(projectId, result.getProjectId());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectMilestoneRepository, times(1)).findById(milestoneId);
    }

    @Test
    void getMilestoneById_fail_milestoneNotFound() {
        when(projectMilestoneRepository.findById(milestoneId)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class, () -> {
            projectMilestoneService.getMilestoneById(projectId, milestoneId, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    @Test
    void getMilestoneById_fail_milestoneDoesNotBelongToProject() {
        Project otherProject = new Project();
        otherProject.setId(UUID.randomUUID());
        projectMilestone.setProject(otherProject);

        when(projectMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(projectMilestone));

        assertThrows(UnauthorizedOperationException.class, () -> {
            projectMilestoneService.getMilestoneById(projectId, milestoneId, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    // --- getMilestonesForProject Tests ---
    @Test
    void getMilestonesForProject_success() {
        Page<ProjectMilestone> milestonePage = new PageImpl<>(Collections.singletonList(projectMilestone), pageable, 1);
        when(projectMilestoneRepository.findByProjectId(projectId, pageable)).thenReturn(milestonePage);

        Page<MilestoneResponseDto> resultPage = projectMilestoneService.getMilestonesForProject(projectId, currentUserId, pageable);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(projectMilestone.getTitle(), resultPage.getContent().get(0).getTitle());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectMilestoneRepository, times(1)).findByProjectId(projectId, pageable);
    }

    // --- updateMilestone Tests ---
    @Test
    void updateMilestone_success() {
        when(projectMilestoneRepository.save(any(ProjectMilestone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MilestoneResponseDto result = projectMilestoneService.updateMilestone(projectId, milestoneId, milestoneUpdateDto, currentUserId);

        assertNotNull(result);
        assertEquals(milestoneUpdateDto.getTitle(), result.getTitle());
        assertEquals(milestoneUpdateDto.getDescription(), result.getDescription());
        assertEquals(milestoneUpdateDto.getDueDate(), result.getDueDate());
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectMilestoneRepository, times(1)).findById(milestoneId);
        verify(projectMilestoneRepository, times(1)).save(any(ProjectMilestone.class));
    }

    @Test
    void updateMilestone_success_clearDueDate() {
        milestoneUpdateDto.setDueDate(null);
        projectMilestone.setDueDate(Timestamp.from(Instant.now()));

        when(projectMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(projectMilestone));
        when(projectMilestoneRepository.save(any(ProjectMilestone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MilestoneResponseDto result = projectMilestoneService.updateMilestone(projectId, milestoneId, milestoneUpdateDto, currentUserId);

        assertNotNull(result);
        assertNull(result.getDueDate());
        verify(projectMilestoneRepository, times(1)).save(argThat(ms -> ms.getDueDate() == null));
    }

    @Test
    void updateMilestone_noChangesInDto_SaveUnchanged() {
        MilestoneUpdateDto noChangeDto = new MilestoneUpdateDto();

        projectMilestone.setDescription(null);
        projectMilestone.setDueDate(null);
        when(projectMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(projectMilestone));
        when(projectMilestoneRepository.save(any(ProjectMilestone.class))).thenReturn(projectMilestone);


        MilestoneResponseDto result = projectMilestoneService.updateMilestone(projectId, milestoneId, noChangeDto, currentUserId);

        assertNotNull(result);
        assertEquals(projectMilestone.getTitle(), result.getTitle());
        verify(projectMilestoneRepository, times(1)).save(projectMilestone);
    }


    @Test
    void updateMilestone_fail_milestoneNotFound() {
        when(projectMilestoneRepository.findById(milestoneId)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class, () -> {
            projectMilestoneService.updateMilestone(projectId, milestoneId, milestoneUpdateDto, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }

    // --- deleteMilestone Tests ---
    @Test
    void deleteMilestone_success() {
        doNothing().when(projectMilestoneRepository).delete(projectMilestone);

        projectMilestoneService.deleteMilestone(projectId, milestoneId, currentUserId);

        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
        verify(projectMilestoneRepository, times(1)).findById(milestoneId);
        verify(projectMilestoneRepository, times(1)).delete(projectMilestone);
    }

    @Test
    void deleteMilestone_fail_milestoneNotFound() {
        when(projectMilestoneRepository.findById(milestoneId)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class, () -> {
            projectMilestoneService.deleteMilestone(projectId, milestoneId, currentUserId);
        });
        verify(projectService, times(1)).verifyUserIsProjectMember(projectId, currentUserId);
    }
}