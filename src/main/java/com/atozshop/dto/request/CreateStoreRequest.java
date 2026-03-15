package com.atozshop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStoreRequest {

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotBlank(message = "Store name is required")
    private String name;

    @NotBlank(message = "Store code is required")
    private String code;

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
}
