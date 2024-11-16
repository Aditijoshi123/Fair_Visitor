package com.vms.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AllPendingVisitsDTO implements Serializable{

    private List<VisitResponseDto> visits;
}
