package com.atozshop.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSupplierRequest {

    private String name;
    private String contactPerson;
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String gstNumber;
    private String panNumber;
    private String bankName;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String supplierType;  // LOCAL, NATIONAL, INTERNATIONAL
    private Boolean isActive;
    private String notes;
}
