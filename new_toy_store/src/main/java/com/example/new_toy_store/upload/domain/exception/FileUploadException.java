package com.example.new_toy_store.upload.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class FileUploadException extends RuntimeException {

    private final HttpStatus status;
    private final String errorType;
    private final Map<String, ?> contextData;

    private FileUploadException(HttpStatus status, String errorType, String message, Map<String, ?> contextData) {
        super(message);
        this.status = status;
        this.errorType = errorType;
        this.contextData = contextData == null ? Map.of() : contextData;
    }

    public static FileUploadException missingConfiguration() {
        return new FileUploadException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "CLOUDINARY_CONFIGURATION_MISSING",
                "Cloudinary chưa được cấu hình. Vui lòng kiểm tra CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY và CLOUDINARY_API_SECRET.",
                Map.of("requiredEnvironmentVariables", "CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET")
        );
    }

    public static FileUploadException emptyFile() {
        return new FileUploadException(
                HttpStatus.BAD_REQUEST,
                "UPLOAD_EMPTY_FILE",
                "File ảnh không được để trống.",
                Map.of("field", "file")
        );
    }

    public static FileUploadException invalidFileType(String filename, String contentType) {
        return new FileUploadException(
                HttpStatus.BAD_REQUEST,
                "UPLOAD_INVALID_FILE_TYPE",
                "Chỉ cho phép upload file hình ảnh.",
                Map.of("filename", filename, "contentType", contentType == null ? "unknown" : contentType)
        );
    }

    public static FileUploadException fileTooLarge(String filename, long size, long maxSize) {
        return new FileUploadException(
                HttpStatus.BAD_REQUEST,
                "UPLOAD_FILE_TOO_LARGE",
                "File ảnh vượt quá dung lượng cho phép.",
                Map.of("filename", filename, "size", size, "maxSize", maxSize)
        );
    }

    public static FileUploadException uploadFailed(String filename, String reason) {
        return new FileUploadException(
                HttpStatus.BAD_GATEWAY,
                "CLOUDINARY_UPLOAD_FAILED",
                "Upload ảnh lên Cloudinary thất bại. Vui lòng thử lại.",
                Map.of("filename", filename, "reason", reason == null ? "unknown" : reason)
        );
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorType() {
        return errorType;
    }

    public Map<String, ?> getContextData() {
        return contextData;
    }
}
