package com.example.new_toy_store.statistics.application.dto.response;

public class StatusCountResponse {

    private final String code;
    private final String label;
    private final long count;

    public StatusCountResponse(String code, String label, long count) {
        this.code = code;
        this.label = label;
        this.count = count;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public long getCount() { return count; }
}
