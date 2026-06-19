package com.example.new_toy_store.supplier.application.dto.response;

public class SupplierResponse {
    private Integer id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private String status;
    private String statusDisplayName;

    public SupplierResponse(Integer id, String name, String phoneNumber, String email, String address, String status, String statusDisplayName) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.status = status;
        this.statusDisplayName = statusDisplayName;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public String getStatusDisplayName() { return statusDisplayName; }
}