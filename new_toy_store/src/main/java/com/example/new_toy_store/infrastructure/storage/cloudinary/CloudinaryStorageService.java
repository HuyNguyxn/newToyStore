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

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryStorageService(Cloudinary cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    public Map<?, ?> uploadImage(MultipartFile file, String targetFolder) {
        validateConfiguration();
        validateImageFile(file);

        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", buildFolderPath(targetFolder),
                            "resource_type", "image"
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

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw FileUploadException.emptyFile();
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw FileUploadException.fileTooLarge(file.getOriginalFilename(), file.getSize(), MAX_FILE_SIZE_BYTES);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw FileUploadException.invalidFileType(file.getOriginalFilename(), contentType);
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
