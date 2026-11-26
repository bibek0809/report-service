package com.example.reportservice.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Generic customer record for customer list reports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRecord {

    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private LocalDate registrationDate;
    private String status;  // ACTIVE, INACTIVE, SUSPENDED
    private Integer totalOrders;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
