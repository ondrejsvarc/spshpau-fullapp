package com.spshpau.projectservice.repositories;

import com.spshpau.projectservice.model.ProjectTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, UUID> {
    Page<ProjectTask> findByProjectId(UUID projectId, Pageable pageable);

    List<ProjectTask> findByProjectIdAndAssignedUserId(UUID projectId, UUID assignedUserId);

    @Modifying
    @Query("UPDATE ProjectTask pt SET pt.assignedUser = null WHERE pt.project.id = :projectId AND pt.assignedUser.id = :userId")
    void unassignUserFromTasksInProject(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
