package com.student.ems.admin.model;

public class DashboardStats {
    private int totalEmployees;
    private int activeEmployees;
    private int inactiveEmployees;
    private int departmentsCount;
    private int todayPresent;
    private int todayPunchOut;
    private int pendingLeaveRequests;

    public DashboardStats() {}

    public DashboardStats(int totalEmployees, int activeEmployees, int inactiveEmployees, int departmentsCount, int todayPresent, int todayPunchOut, int pendingLeaveRequests) {
        this.totalEmployees = totalEmployees;
        this.activeEmployees = activeEmployees;
        this.inactiveEmployees = inactiveEmployees;
        this.departmentsCount = departmentsCount;
        this.todayPresent = todayPresent;
        this.todayPunchOut = todayPunchOut;
        this.pendingLeaveRequests = pendingLeaveRequests;
    }

    public int getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }

    public int getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(int activeEmployees) { this.activeEmployees = activeEmployees; }

    public int getInactiveEmployees() { return inactiveEmployees; }
    public void setInactiveEmployees(int inactiveEmployees) { this.inactiveEmployees = inactiveEmployees; }

    public int getDepartmentsCount() { return departmentsCount; }
    public void setDepartmentsCount(int departmentsCount) { this.departmentsCount = departmentsCount; }

    public int getTodayPresent() { return todayPresent; }
    public void setTodayPresent(int todayPresent) { this.todayPresent = todayPresent; }

    public int getTodayPunchOut() { return todayPunchOut; }
    public void setTodayPunchOut(int todayPunchOut) { this.todayPunchOut = todayPunchOut; }

    public int getPendingLeaveRequests() { return pendingLeaveRequests; }
    public void setPendingLeaveRequests(int pendingLeaveRequests) { this.pendingLeaveRequests = pendingLeaveRequests; }
}
