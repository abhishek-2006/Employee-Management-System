package com.student.ems.employee.model;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;

public class Attendance {
    private Integer id;
    private Integer employeeId;
    private Date workDate;
    private Time punchIn;
    private Time punchOut;
    private Integer workMinutes;
    private String punchMethod;
    private String formattedWorkHours;
    private String detailedSummary;
    private String punchInPhoto;
    private String punchOutPhoto;
    private String status;

    public Attendance() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }

    public Time getPunchIn() { return punchIn; }
    public void setPunchIn(Time punchIn) { 
        this.punchIn = punchIn; 
        updateCalculatedFields();
    }

    public Time getPunchOut() { return punchOut; }
    public void setPunchOut(Time punchOut) { 
        this.punchOut = punchOut; 
        updateCalculatedFields();
    }

    public Integer getWorkMinutes() { return workMinutes; }
    public void setWorkMinutes(Integer workMinutes) { 
        this.workMinutes = workMinutes; 
        updateCalculatedFields();
    }

    public String getPunchMethod() { return punchMethod; }
    public void setPunchMethod(String punchMethod) { this.punchMethod = punchMethod; }

    public String getFormattedWorkHours() { return formattedWorkHours; }
    public void setFormattedWorkHours(String formattedWorkHours) { this.formattedWorkHours = formattedWorkHours; }

    public String getDetailedSummary() { return detailedSummary; }
    public void setDetailedSummary(String detailedSummary) { this.detailedSummary = detailedSummary; }

    public String getPunchInPhoto() { return punchInPhoto; }
    public void setPunchInPhoto(String punchInPhoto) { this.punchInPhoto = punchInPhoto; }

    public String getPunchOutPhoto() { return punchOutPhoto; }
    public void setPunchOutPhoto(String punchOutPhoto) { this.punchOutPhoto = punchOutPhoto; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    private void updateCalculatedFields() {
        SimpleDateFormat tf = new SimpleDateFormat("hh:mm a");
        String inStr = punchIn != null ? tf.format(punchIn) : "-";
        String outStr = punchOut != null ? tf.format(punchOut) : "-";

        if (workMinutes != null && workMinutes > 0) {
            int h = workMinutes / 60;
            int m = workMinutes % 60;
            this.formattedWorkHours = h + "h " + m + "m";
            this.detailedSummary = inStr + " - " + outStr + " = " + formattedWorkHours;
        } else if (punchIn != null && punchOut == null) {
            this.formattedWorkHours = "In Progress";
            this.detailedSummary = inStr + " - Working...";
        } else {
            this.formattedWorkHours = "0h 0m";
            this.detailedSummary = "-";
        }
    }
}
