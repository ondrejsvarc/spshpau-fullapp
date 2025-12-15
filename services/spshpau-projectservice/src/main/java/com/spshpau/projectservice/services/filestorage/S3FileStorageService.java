package com.spshpau.projectservice.services.filestorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-duration-minutes}")
    private long presignedUrlDurationMinutes;

    /**
     * Uploads a file to S3.
     * @param key The key under which to store the new object.
     * @param file The file to upload.
     * @return The version ID of the uploaded object.
     * @throws IOException If an I/O error occurs.
     */
    public String uploadFile(String key, MultipartFile file) throws IOException {
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
