package com.vms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vms.enums.Role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    @NotNull
    @Size(max = 255)
    private String name;
    @NotNull
    @Size(max = 255)
    private String email;
    @NotNull
    @Size(min = 10)
    private String phone;
    private Role role;
    private String flatNo;
    private AddressDto address;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

}
