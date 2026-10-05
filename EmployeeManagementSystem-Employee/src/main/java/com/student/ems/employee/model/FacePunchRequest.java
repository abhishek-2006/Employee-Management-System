package com.student.ems.employee.model;

import java.util.List;

public class FacePunchRequest {
    private List<Double> descriptor;

    public FacePunchRequest() {}

    public List<Double> getDescriptor() { return descriptor; }
    public void setDescriptor(List<Double> descriptor) { this.descriptor = descriptor; }
}
