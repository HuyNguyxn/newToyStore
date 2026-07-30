package com.example.new_toy_store.upload.application;

import com.example.new_toy_store.infrastructure.storage.cloudinary.CloudinaryStorageService;
import com.example.new_toy_store.upload.application.dto.response.UploadImageResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class UploadService {

    private final CloudinaryStorageService storageService;

    public UploadService(CloudinaryStorageService storageService) {
        this.storageService = storageService;
    }

    public UploadImageResponse uploadImage(MultipartFile file, String folder) {
        Map<?, ?> result = storageService.uploadImage(file, folder);

        return new UploadImageResponse(
                String.valueOf(result.get("secure_url")),
                String.valueOf(result.get("public_id")),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );
    }
}
