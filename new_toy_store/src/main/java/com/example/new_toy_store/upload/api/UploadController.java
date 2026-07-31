package com.example.new_toy_store.upload.api;

import com.example.new_toy_store.upload.application.UploadService;
import com.example.new_toy_store.upload.application.dto.response.UploadMediaResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
@Validated
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadMediaResponse uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String folder
    ) {
        return uploadService.uploadImage(file, folder);
    }

    @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadMediaResponse uploadVideo(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String folder
    ) {
        return uploadService.uploadVideo(file, folder);
    }
}
