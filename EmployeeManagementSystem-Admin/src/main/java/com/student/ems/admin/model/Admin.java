package com.student.ems.admin.model;

import java.sql.Timestamp;

public class Admin {
    private Integer id;
    private String username;
    private String password;
    private String fullName;
    private Timestamp createdAt;

    public Admin() {}

    public Admin(Integer id, String username, String password, String fullName, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
