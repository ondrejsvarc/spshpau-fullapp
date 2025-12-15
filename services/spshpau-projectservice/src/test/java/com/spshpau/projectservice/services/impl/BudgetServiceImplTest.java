package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.*;
import com.spshpau.projectservice.model.BudgetExpense;
import com.spshpau.projectservice.model.Project;
import com.spshpau.projectservice.model.ProjectBudget;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.repositories.BudgetExpenseRepository;
import com.spshpau.projectservice.repositories.ProjectBudgetrepository;
import com.spshpau.projectservice.repositories.ProjectRepository;
import com.spshpau.projectservice.services.ProjectService;
import com.spshpau.projectservice.services.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BudgetServiceImplTest {

    @Mock
    private ProjectBudgetrepository projectBudgetRepository;
    @Mock
    private BudgetExpenseRepository budgetExpenseRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectService projectService;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private UUID projectId;
    private UUID expenseId;
    private UUID currentUserId;

    private Project project;
    private ProjectBudget projectBudget;
    private BudgetExpense budgetExpense;

    private BudgetCreateDto budgetCreateDto;
    private BudgetUpdateDto budgetUpdateDto;
    private ExpenseCreateDto expenseCreateDto;
    private ExpenseUpdateDto expenseUpdateDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        expenseId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);
        project.setTitle("Test Project");

        projectBudget = new ProjectBudget();
        projectBudget.setId(projectId);
        projectBudget.setProject(project);
        projectBudget.setCurrency("USD");
        projectBudget.setTotalAmount(1000f);
        projectBudget.setExpenses(new HashSet<>());

        project.setBudget(projectBudget);

        budgetExpense = new BudgetExpense();
        budgetExpense.setId(expenseId);
        budgetExpense.setBudget(projectBudget);
        budgetExpense.setAmount(100f);
        budgetExpense.setDate(new Date());
        budgetExpense.setComment("Test Expense");
        projectBudget.getExpenses().add(budgetExpense);


        budgetCreateDto = new BudgetCreateDto();
        budgetCreateDto.setCurrency("EUR");
        budgetCreateDto.setTotalAmount(2000f);

        budgetUpdateDto = new BudgetUpdateDto();
        budgetUpdateDto.setCurrency("GBP");
        budgetUpdateDto.setTotalAmount(1500f);

        expenseCreateDto = new ExpenseCreateDto();
        expenseCreateDto.setAmount(50f);
        expenseCreateDto.setDate(new Date());
        expenseCreateDto.setComment("New Expense");

        expenseUpdateDto = new ExpenseUpdateDto();
        expenseUpdateDto.setAmount(75f);
        expenseUpdateDto.setComment("Updated Expense Comment");

        pageable = PageRequest.of(0, 10);

        when(projectService.isUserOwnerOfProject(projectId, currentUserId)).thenReturn(true);
        doNothing().when(projectService).verifyUserIsProjectMember(projectId, currentUserId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectBudgetRepository.findById(projectId)).thenReturn(Optional.of(projectBudget));
        when(projectBudgetRepository.existsById(projectId)).thenReturn(true);
        when(budgetExpenseRepository.sumExpensesByBudgetId(projectId)).thenReturn(Optional.of(100f));
        when(budgetExpenseRepository.findByIdAndBudgetId(expenseId, projectId)).thenReturn(Optional.of(budgetExpense));

    }

    // --- createProjectBudget Tests ---
    @Test
    void createProjectBudget_success() {
        // Set up
        projectId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();

        SimpleUser owner = new SimpleUser();
        owner.setId(currentUserId);

        project = new Project();
        project.setId(projectId);
        project.setTitle("Test Project");
        project.setOwner(owner);

        budgetCreateDto = new BudgetCreateDto();
        budgetCreateDto.setCurrency("USD");
        budgetCreateDto.setTotalAmount(10000f);

        // Test
        when(projectService.isUserOwnerOfProject(projectId, currentUserId)).thenReturn(true);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project projectToSave = invocation.getArgument(0);
            if (projectToSave.getBudget() != null && projectToSave.getBudget().getId() == null) {
                projectToSave.getBudget().setId(projectToSave.getId());
            }
            return projectToSave;
        });

        BudgetResponseDto result = budgetService.createProjectBudget(projectId, budgetCreateDto, currentUserId);

        assertNotNull(result);
        assertEquals(projectId, result.getProjectId());
        assertEquals(budgetCreateDto.getCurrency(), result.getCurrency());
        assertEquals(budgetCreateDto.getTotalAmount(), result.getTotalAmount());
        assertEquals(0f, result.getSpentAmount());
        assertEquals(budgetCreateDto.getTotalAmount(), result.getRemainingAmount());
        assertNotNull(result.getExpenses());
        assertTrue(result.getExpenses().isEmpty());

        verify(projectService).isUserOwnerOfProject(projectId, currentUserId);
        verify(projectRepository).findById(projectId);

        ArgumentCaptor<Project> projectArgumentCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectArgumentCaptor.capture());
        Project savedProject = projectArgumentCaptor.getValue();

        assertNotNull(savedProject.getBudget());
        assertEquals(projectId, savedProject.getBudget().getId());
        assertEquals(budgetCreateDto.getCurrency(), savedProject.getBudget().getCurrency());
        assertEquals(budgetCreateDto.getTotalAmount(), savedProject.getBudget().getTotalAmount());
        assertSame(project, savedProject.getBudget().getProject(), "The budget's project reference should be the original project instance.");

        verify(projectBudgetRepository, never()).existsById(any(UUID.class));
    }


    @Test
    void createProjectBudget_fail_notOwner() {
        when(projectService.isUserOwnerOfProject(projectId, currentUserId)).thenReturn(false);
        assertThrows(UnauthorizedOperationException.class, () -> {
            budgetService.createProjectBudget(projectId, budgetCreateDto, currentUserId);
        });
    }

    @Test
    void createProjectBudget_fail_budgetAlreadyExists() {
        when(projectBudgetRepository.existsById(projectId)).thenReturn(true);
        assertThrows(BudgetAlreadyExistsException.class, () -> {
            budgetService.createProjectBudget(projectId, budgetCreateDto, currentUserId);
        });
    }

    @Test
    void createProjectBudget_fail_projectNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        when(projectBudgetRepository.existsById(projectId)).thenReturn(false);
        assertThrows(ProjectNotFoundException.class, () -> {
            budgetService.createProjectBudget(projectId, budgetCreateDto, currentUserId);
        });
    }


    // --- getProjectBudget Tests ---
    @Test
    void getProjectBudget_success() {
        BudgetResponseDto response = budgetService.getProjectBudget(projectId, currentUserId);
        assertNotNull(response);
        assertEquals(projectBudget.getCurrency(), response.getCurrency());
        assertEquals(100f, response.getSpentAmount());
        verify(projectService).verifyUserIsProjectMember(projectId, currentUserId);
    }

    @Test
    void getProjectBudget_fail_budgetNotFound() {
        when(projectBudgetRepository.findById(projectId)).thenReturn(Optional.empty());
        assertThrows(BudgetNotFoundException.class, () -> {
            budgetService.getProjectBudget(projectId, currentUserId);
        });
    }

    // --- updateProjectBudget Tests ---
    @Test
    void updateProjectBudget_success() {
        when(projectBudgetRepository.save(any(ProjectBudget.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BudgetResponseDto response = budgetService.updateProjectBudget(projectId, budgetUpdateDto, currentUserId);

        assertNotNull(response);
        assertEquals(budgetUpdateDto.getCurrency(), response.getCurrency());
        assertEquals(budgetUpdateDto.getTotalAmount(), response.getTotalAmount());
        verify(projectBudgetRepository).save(any(ProjectBudget.class));
    }

    @Test
    void updateProjectBudget_noActualChanges_shouldStillReturnDto() {
        BudgetUpdateDto noChangeDto = new BudgetUpdateDto();
        noChangeDto.setCurrency(projectBudget.getCurrency());
        noChangeDto.setTotalAmount(projectBudget.getTotalAmount());

        BudgetResponseDto response = budgetService.updateProjectBudget(projectId, noChangeDto, currentUserId);

        assertNotNull(response);
        assertEquals(projectBudget.getCurrency(), response.getCurrency());
        assertEquals(projectBudget.getTotalAmount(), response.getTotalAmount());
        verify(projectBudgetRepository, never()).save(any(ProjectBudget.class));
    }


    // --- deleteProjectBudget Tests ---
    @Test
    void deleteProjectBudget_success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        budgetService.deleteProjectBudget(projectId, currentUserId);

        verify(projectRepository).save(project);
        assertNull(project.getBudget());
    }


    // --- getRemainingProjectBudget Tests ---
    @Test
    void getRemainingProjectBudget_success() {
        when(budgetExpenseRepository.sumExpensesByBudgetId(projectId)).thenReturn(Optional.of(100f));
        projectBudget.setTotalAmount(1000f);
        when(projectBudgetRepository.findById(projectId)).thenReturn(Optional.of(projectBudget));


        RemainingBudgetDto response = budgetService.getRemainingProjectBudget(projectId, currentUserId);
        assertNotNull(response);
        assertEquals(1000f, response.getTotalAmount());
        assertEquals(100f, response.getSpentAmount());
        assertEquals(900f, response.getRemainingAmount());
        assertEquals(projectBudget.getCurrency(), response.getCurrency());
    }

    // --- addExpenseToBudget Tests ---
    @Test
    void addExpenseToBudget_success() {
        when(budgetExpenseRepository.save(any(BudgetExpense.class))).thenAnswer(invocation -> {
            BudgetExpense ex = invocation.getArgument(0);
            ex.setId(UUID.randomUUID());
            return ex;
        });
        ExpenseResponseDto response = budgetService.addExpenseToBudget(projectId, expenseCreateDto, currentUserId);
        assertNotNull(response);
        assertEquals(expenseCreateDto.getComment(), response.getComment());
        verify(budgetExpenseRepository).save(any(BudgetExpense.class));
    }

    // --- getExpenseById Tests ---
    @Test
    void getExpenseById_success() {
        ExpenseResponseDto response = budgetService.getExpenseById(projectId, expenseId, currentUserId);
        assertNotNull(response);
        assertEquals(budgetExpense.getComment(), response.getComment());
    }

    @Test
    void getExpenseById_fail_expenseNotFound() {
        when(budgetExpenseRepository.findByIdAndBudgetId(expenseId, projectId)).thenReturn(Optional.empty());
        assertThrows(ExpenseNotFoundException.class, () -> {
            budgetService.getExpenseById(projectId, expenseId, currentUserId);
        });
    }

    // --- getExpensesForProjectBudget Tests ---
    @Test
    void getExpensesForProjectBudget_success() {
        Page<BudgetExpense> expensePage = new PageImpl<>(Collections.singletonList(budgetExpense), pageable, 1);
        when(budgetExpenseRepository.findByBudgetId(projectId, pageable)).thenReturn(expensePage);

        Page<ExpenseResponseDto> response = budgetService.getExpensesForProjectBudget(projectId, currentUserId, pageable);
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    // --- updateExpense Tests ---
    @Test
    void updateExpense_success() {
        when(budgetExpenseRepository.save(any(BudgetExpense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ExpenseResponseDto response = budgetService.updateExpense(projectId, expenseId, expenseUpdateDto, currentUserId);
        assertNotNull(response);
        assertEquals(expenseUpdateDto.getComment(), response.getComment());
        assertEquals(expenseUpdateDto.getAmount(), response.getAmount());
        verify(budgetExpenseRepository).save(any(BudgetExpense.class));
    }

    @Test
    void updateExpense_noActualChanges_shouldReturnDto() {
        expenseUpdateDto.setAmount(budgetExpense.getAmount());
        expenseUpdateDto.setComment(budgetExpense.getComment());
        expenseUpdateDto.setDate(budgetExpense.getDate());

        ExpenseResponseDto response = budgetService.updateExpense(projectId, expenseId, expenseUpdateDto, currentUserId);

        assertNotNull(response);
        assertEquals(budgetExpense.getAmount(), response.getAmount());
        assertEquals(budgetExpense.getComment(), response.getComment());
        assertEquals(budgetExpense.getDate(), response.getDate());
        assertEquals(expenseId, response.getId());
        assertEquals(projectId, response.getBudgetId());

        verify(budgetExpenseRepository, never()).save(any(BudgetExpense.class));
    }


    // --- removeExpense Tests ---
    @Test
    void removeExpense_success() {
        doNothing().when(budgetExpenseRepository).delete(budgetExpense);
        budgetService.removeExpense(projectId, expenseId, currentUserId);
        verify(budgetExpenseRepository).delete(budgetExpense);
    }
}