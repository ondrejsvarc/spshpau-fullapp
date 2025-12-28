package com.spshpau.projectservice.services.filestorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class S3FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final long presignedUrlDurationMinutes;

    @Autowired
    public S3FileStorageService(
            @Autowired(required = false) S3Client s3Client,
            @Autowired(required = false) S3Presigner s3Presigner,
            @Value("${aws.s3.bucket-name:#{null}}") String bucketName,
            @Value("${aws.s3.presigned-url-duration-minutes:60}") long presignedUrlDurationMinutes
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.presignedUrlDurationMinutes = presignedUrlDurationMinutes;
    }

    private void checkS3Configured() {
        if (s3Client == null || s3Presigner == null || bucketName == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "S3 storage is not configured. This feature is currently unavailable.");
        }
    }

    /**
     * Uploads a file to S3.
     * @param key The key under which to store the new object.
     * @param file The file to upload.
     * @return The version ID of the uploaded object.
     * @throws IOException If an I/O error occurs.
     */
    public String uploadFile(String key, MultipartFile file) throws IOException {
        checkS3Configured();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .metadata(Map.of("originalFilename", file.getOriginalFilename()))
                .build();

        PutObjectResponse response = s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        log.info("File {} uploaded to S3 with key {}. VersionId: {}", file.getOriginalFilename(), key, response.versionId());
        return response.versionId();
    }

    /**
     * Generates a pre-signed URL for downloading an object version.
     * @param key The S3 object key.
     * @param versionId The specific version ID of the object.
     * @return The pre-signed URL.
     */
    public URL generatePresignedDownloadUrl(String key, String versionId) {
        checkS3Configured();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .versionId(versionId)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlDurationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        log.info("Generated presigned URL for key {}, versionId {}", key, versionId);
        return presignedRequest.url();
    }

    /**
     * Deletes a specific version of an object from S3.
     * @param key The S3 object key.
     * @param versionId The specific version ID to delete.
     */
    public void deleteFileVersion(String key, String versionId) {
        checkS3Configured();
        if (versionId == null || versionId.isEmpty() || "null".equalsIgnoreCase(versionId)) {
            log.warn("Attempted to delete object {} with null/empty versionId. This will create a delete marker if versioning is enabled, or delete the object if not versioned.", key);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Created delete marker or deleted object for key {} (no specific versionId provided or versioning off).", key);
            return;
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .versionId(versionId)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        log.info("Deleted version {} of file {} from S3.", versionId, key);
    }

    /**
     * Lists all versions of a specific object in S3.
     * @param objectKey The key of the object.
     * @return A list of ObjectVersion.
     */
    public List<ObjectVersion> listObjectVersions(String objectKey) {
        checkS3Configured();
        List<ObjectVersion> versions = new ArrayList<>();
        try {
            ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                    .bucket(bucketName)
                    .prefix(objectKey)
                    .build();

            ListObjectVersionsResponse response;
            do {
                response = s3Client.listObjectVersions(listRequest);
                response.versions().stream()
                        .filter(v -> v.key().equals(objectKey))
                        .forEach(versions::add);
                response.deleteMarkers().stream()
                        .filter(dm -> dm.key().equals(objectKey))
                        .forEach(dm -> log.info("Delete marker found for key {}: versionId {}", dm.key(), dm.versionId()));

                listRequest = listRequest.toBuilder().keyMarker(response.nextKeyMarker()).versionIdMarker(response.nextVersionIdMarker()).build();
            } while (response.isTruncated());

        } catch (S3Exception e) {
            log.error("Error listing object versions for key {}: {}", objectKey, e.getMessage(), e);
        }
        return versions;
    }
}