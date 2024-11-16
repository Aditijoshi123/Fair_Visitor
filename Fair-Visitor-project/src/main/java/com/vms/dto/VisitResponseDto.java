package com.vms.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vms.enums.VisitStatus;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VisitResponseDto implements Serializable{
    private VisitStatus status;
    @NotNull
    @Size(max = 255)
    private String purpose;
    @Size(max = 255)
    private String urlOfImage;
    @NotNull
    private Integer noOfPeople;
    @NotNull
    private String email;
    @NotNull
    private String flatNumber;
    private String visitorName;
    private String visitorPhone;
    private Long visitId;
//    private Date inTime;
//    private Date outTime;
}
