package com.spshpau.projectservice.repositories;

import com.spshpau.projectservice.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findByOwnerId(UUID ownerId, Pageable pageable);

    Page<Project> findByCollaboratorsId(UUID collaboratorId, Pageable pageable);

}
