package com.example.new_toy_store.notification.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BroadcastNotificationRequest {

    @NotBlank
    @Size(max = 80)
    private String requestKey;

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(max = 500)
    private String message;

    @Size(max = 255)
    private String actionUrl;

    private boolean sendEmail;

    public String getRequestKey() { return requestKey; }
    public void setRequestKey(String requestKey) { this.requestKey = requestKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public boolean isSendEmail() { return sendEmail; }
    public void setSendEmail(boolean sendEmail) { this.sendEmail = sendEmail; }
}
