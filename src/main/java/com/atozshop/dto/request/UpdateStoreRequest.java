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
public class UpdateStoreRequest {

    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String gstNumber;
    private String logoUrl;
    private Boolean isActive;
}
