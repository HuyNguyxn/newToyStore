package com.example.new_toy_store.user.application.dto.response;

public class UserAdminSummaryResponse {

    private final long totalUsers;
    private final long adminCount;
    private final long managerCount;
    private final long staffCount;
    private final long customerCount;
    private final long activeCount;
    private final long lockedCount;
    private final long unverifiedCount;

    public UserAdminSummaryResponse(
            long totalUsers,
            long adminCount,
            long managerCount,
            long staffCount,
            long customerCount,
            long activeCount,
            long lockedCount,
            long unverifiedCount
    ) {
        this.totalUsers = totalUsers;
        this.adminCount = adminCount;
        this.managerCount = managerCount;
        this.staffCount = staffCount;
        this.customerCount = customerCount;
        this.activeCount = activeCount;
        this.lockedCount = lockedCount;
        this.unverifiedCount = unverifiedCount;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getAdminCount() { return adminCount; }
    public long getManagerCount() { return managerCount; }
    public long getStaffCount() { return staffCount; }
    public long getCustomerCount() { return customerCount; }
    public long getActiveCount() { return activeCount; }
    public long getLockedCount() { return lockedCount; }
    public long getUnverifiedCount() { return unverifiedCount; }
}
