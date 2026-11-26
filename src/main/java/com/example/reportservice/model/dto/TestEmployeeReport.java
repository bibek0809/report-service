package com.example.reportservice.model.dto;

public class TestEmployeeReport {
    private final String name;
    private final String department;
    private final double salary;

    public TestEmployeeReport(String name, String department, double salary) {
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public double getSalary() {
        return salary;
    }
}
