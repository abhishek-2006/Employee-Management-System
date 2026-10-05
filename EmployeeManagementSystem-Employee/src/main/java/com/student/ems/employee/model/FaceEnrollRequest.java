package com.student.ems.employee.model;

import java.util.List;

public class FaceEnrollRequest {
    private List<Double> descriptor;

    public FaceEnrollRequest() {}

    public List<Double> getDescriptor() { return descriptor; }
    public void setDescriptor(List<Double> descriptor) { this.descriptor = descriptor; }
}
