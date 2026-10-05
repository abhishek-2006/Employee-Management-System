package com.student.ems.employee.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Date;
import java.sql.Timestamp;

public class Employee {
    private Integer id;
    private String employeeCode;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String address;
    private Date joinDate;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String faceDescriptor;

    private boolean faceEnrolled;
    private String status;
    private String gender;
    private Date dob;
    private Double salary;
    private String photo;
    private Timestamp createdAt;

    public Employee() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFaceDescriptor() { return faceDescriptor; }
    public void setFaceDescriptor(String faceDescriptor) {
        this.faceDescriptor = faceDescriptor;
        this.faceEnrolled = (faceDescriptor != null && !faceDescriptor.trim().isEmpty());
    }

    public boolean isFaceEnrolled() { return faceEnrolled; }
    public void setFaceEnrolled(boolean faceEnrolled) { this.faceEnrolled = faceEnrolled; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
