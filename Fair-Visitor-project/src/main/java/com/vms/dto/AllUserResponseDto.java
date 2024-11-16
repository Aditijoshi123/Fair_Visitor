package com.vms.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllUserResponseDto {
	
	private List<UserResponseDto> userResponseDtoList;
	private Long totalRows;
	private Integer totalPages;
}
