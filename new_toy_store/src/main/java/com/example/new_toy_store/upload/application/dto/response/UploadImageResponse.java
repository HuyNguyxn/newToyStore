package com.example.new_toy_store.upload.application.dto.response;

public class UploadImageResponse {

    private String url;
    private String publicId;
    private String originalFilename;
    private String contentType;
    private long size;

    public UploadImageResponse() {
    }

    public UploadImageResponse(String url, String publicId, String originalFilename, String contentType, long size) {
        this.url = url;
        this.publicId = publicId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
