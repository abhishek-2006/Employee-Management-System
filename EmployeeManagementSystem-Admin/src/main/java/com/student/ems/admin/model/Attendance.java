package com.student.ems.admin.model;

import java.sql.Date;
import java.sql.Time;

public class Attendance {
    private Integer id;
    private Integer employeeId;
    private String employeeCode;
    private String employeeName;
    private String department;
    private Date workDate;
    private Time punchIn;
    private Time punchOut;
    private Integer workMinutes;
    private String punchMethod;
    private String formattedWorkHours;
    private String punchInPhoto;
    private String punchOutPhoto;
    private String status;
    private String photo;

    public Attendance() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }

    public Time getPunchIn() { return punchIn; }
    public void setPunchIn(Time punchIn) { this.punchIn = punchIn; }

    public Time getPunchOut() { return punchOut; }
    public void setPunchOut(Time punchOut) { this.punchOut = punchOut; }

    public Integer getWorkMinutes() { return workMinutes; }
    public void setWorkMinutes(Integer workMinutes) { 
        this.workMinutes = workMinutes; 
        if (workMinutes != null && workMinutes > 0) {
            int hours = workMinutes / 60;
            int mins = workMinutes % 60;
            this.formattedWorkHours = hours + "h " + mins + "m";
        } else if (punchIn != null && punchOut == null) {
            this.formattedWorkHours = "In Progress";
        } else {
            this.formattedWorkHours = "0h 0m";
        }
    }

    public String getPunchMethod() { return punchMethod; }
    public void setPunchMethod(String punchMethod) { this.punchMethod = punchMethod; }

    public String getFormattedWorkHours() { return formattedWorkHours; }
    public void setFormattedWorkHours(String formattedWorkHours) { this.formattedWorkHours = formattedWorkHours; }

    public String getPunchInPhoto() { return punchInPhoto; }
    public void setPunchInPhoto(String punchInPhoto) { this.punchInPhoto = punchInPhoto; }

    public String getPunchOutPhoto() { return punchOutPhoto; }
    public void setPunchOutPhoto(String punchOutPhoto) { this.punchOutPhoto = punchOutPhoto; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}
