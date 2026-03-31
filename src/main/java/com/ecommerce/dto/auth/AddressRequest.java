package com.ecommerce.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressRequest {
    private String fullName;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private boolean isDefault;
}
