package com.example.new_toy_store.upload.application;

import com.example.new_toy_store.infrastructure.storage.cloudinary.CloudinaryStorageService;
import com.example.new_toy_store.upload.application.dto.response.UploadMediaResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class UploadService {

    private final CloudinaryStorageService storageService;

    public UploadService(CloudinaryStorageService storageService) {
        this.storageService = storageService;
    }

    public UploadMediaResponse uploadImage(MultipartFile file, String folder) {
        Map<?, ?> result = storageService.uploadImage(file, folder);
        return toResponse(result, file, "IMAGE");
    }

    public UploadMediaResponse uploadVideo(MultipartFile file, String folder) {
        Map<?, ?> result = storageService.uploadVideo(file, folder);
        return toResponse(result, file, "VIDEO");
    }

    private UploadMediaResponse toResponse(Map<?, ?> result, MultipartFile file, String mediaType) {
        return new UploadMediaResponse(
                String.valueOf(result.get("secure_url")),
                String.valueOf(result.get("public_id")),
                file.getOriginalFilename(),
                file.getContentType(),
                mediaType,
                file.getSize()
        );
    }
}
