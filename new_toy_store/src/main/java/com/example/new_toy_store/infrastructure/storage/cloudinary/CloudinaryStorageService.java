package com.example.new_toy_store.infrastructure.storage.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.new_toy_store.upload.domain.exception.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryStorageService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE_BYTES = 50 * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryStorageService(Cloudinary cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    public Map<?, ?> uploadImage(MultipartFile file, String targetFolder) {
        validateConfiguration();
        validateMediaFile(file, "image/", MAX_IMAGE_SIZE_BYTES);

        return upload(file, targetFolder, "image");
    }

    public Map<?, ?> uploadVideo(MultipartFile file, String targetFolder) {
        validateConfiguration();
        validateMediaFile(file, "video/", MAX_VIDEO_SIZE_BYTES);

        return upload(file, targetFolder, "video");
    }

    private Map<?, ?> upload(MultipartFile file, String targetFolder, String resourceType) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", buildFolderPath(targetFolder),
                            "resource_type", resourceType
                    )
            );
        } catch (IOException ex) {
            throw FileUploadException.uploadFailed(file.getOriginalFilename(), ex.getMessage());
        }
    }

    private void validateConfiguration() {
        if (isBlank(properties.getCloudName()) || isBlank(properties.getApiKey()) || isBlank(properties.getApiSecret())) {
            throw FileUploadException.missingConfiguration();
        }
    }

    private void validateMediaFile(MultipartFile file, String expectedContentTypePrefix, long maxFileSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw FileUploadException.emptyFile();
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw FileUploadException.fileTooLarge(file.getOriginalFilename(), file.getSize(), maxFileSizeBytes);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(expectedContentTypePrefix)) {
            throw FileUploadException.invalidFileType(file.getOriginalFilename(), contentType, expectedContentTypePrefix + "*");
        }
    }

    private String buildFolderPath(String targetFolder) {
        String rootFolder = sanitizeFolderSegment(properties.getFolder());
        String childFolder = sanitizeFolderSegment(targetFolder);
        if (isBlank(childFolder)) {
            return rootFolder;
        }
        return rootFolder + "/" + childFolder;
    }

    private String sanitizeFolderSegment(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace("\\", "/")
                .replaceAll("[^a-zA-Z0-9/_-]", "-")
                .replaceAll("/+", "/")
                .replaceAll("^/|/$", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
