package com.student.ems.employee.model;

public class TodayAttendanceStatus {
    private String employeeName;
    private String employeeCode;
    private String department;
    private String designation;
    private boolean faceEnrolled;
    private String todayStatus; // "NOT_PUNCHED", "WORKING", "COMPLETED"
    private String todayPunchIn;
    private String todayPunchOut;
    private String todayWorkHours;

    public TodayAttendanceStatus() {}

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public boolean isFaceEnrolled() { return faceEnrolled; }
    public void setFaceEnrolled(boolean faceEnrolled) { this.faceEnrolled = faceEnrolled; }

    public String getTodayStatus() { return todayStatus; }
    public void setTodayStatus(String todayStatus) { this.todayStatus = todayStatus; }

    public String getTodayPunchIn() { return todayPunchIn; }
    public void setTodayPunchIn(String todayPunchIn) { this.todayPunchIn = todayPunchIn; }

    public String getTodayPunchOut() { return todayPunchOut; }
    public void setTodayPunchOut(String todayPunchOut) { this.todayPunchOut = todayPunchOut; }

    public String getTodayWorkHours() { return todayWorkHours; }
    public void setTodayWorkHours(String todayWorkHours) { this.todayWorkHours = todayWorkHours; }
}
