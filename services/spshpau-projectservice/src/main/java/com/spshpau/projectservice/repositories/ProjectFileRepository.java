package com.spshpau.projectservice.repositories;

import com.spshpau.projectservice.model.ProjectFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, UUID> {

    @Query(value = "SELECT pf_ranked.* FROM (" +
            "    SELECT pf.*, " +
            "           pf.upload_timestamp AS \"uploadTimestamp\", " +
            "           ROW_NUMBER() OVER (PARTITION BY pf.original_filename ORDER BY pf.upload_timestamp DESC, pf.id DESC) as rn " +
            "    FROM project_files pf " +
            "    WHERE pf.project_id = :projectId" +
            ") AS pf_ranked " +
            "WHERE pf_ranked.rn = 1 " +
            "ORDER BY pf_ranked.original_filename ASC",
            nativeQuery = true)
    List<ProjectFile> findLatestVersionOfEachFileByProjectId(@Param("projectId") UUID projectId);

    Page<ProjectFile> findByProjectIdOrderByUploadTimestampDesc(UUID projectId, Pageable pageable);

    List<ProjectFile> findByProjectIdAndOriginalFilenameOrderByUploadTimestampDesc(UUID projectId, String originalFilename);

    Optional<ProjectFile> findByIdAndProjectId(UUID id, UUID projectId);
}
