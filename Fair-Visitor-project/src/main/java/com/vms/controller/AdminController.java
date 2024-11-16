package com.vms.controller;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vms.dto.AddressDto;
import com.vms.dto.AllUserResponseDto;
import com.vms.dto.UserDto;
import com.vms.enums.Role;
import com.vms.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Controller", description = "Endpoints for managing users and administrative operations.")
public class AdminController {

    private Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Operation(summary = "Create a new user", description = "Creates a new user based on the provided UserDto.")
    @PostMapping("/createUser")
    ResponseEntity<Long> createUser(@RequestBody @Valid UserDto userDto){
        Long id = adminService.createUser(userDto);
        return ResponseEntity.ok(id);
    }
    
    @Operation(summary = "Deactivate user", description = "Marks a user as inactive by their userId.")
    @PutMapping("/markUserInactive/{userId}")
    public ResponseEntity<Void> markUserInactive(@PathVariable Long userId){
        adminService.markInactive(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate user", description = "Marks a user as active by their userId.")
    @PutMapping("/markUserActive/{userId}")
    public ResponseEntity<Void> markUserActive(@PathVariable Long userId){
        adminService.markActive(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all users paginated", description = "Retrieves all users with pagination support.")
    @GetMapping("/allUsersPaginated")
    public ResponseEntity<AllUserResponseDto> getAllUsers(@RequestParam Integer pageSize, @RequestParam Integer pageNumber){
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        return ResponseEntity.ok(adminService.findAllWithPagination(pageable));
    }
    
    @Operation(summary = "Update user details", description = "Updates a user's details by their userId.")
    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<Void> updateUser(
    		@RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class),
                    examples = @ExampleObject(value = "{\"name\": \"Jane Doe\", \"email\": \"janedoe@example.com\", \"phone\": \"0987654321\", \"flatNo\": \"102\", \"role\": \"ADMIN\", \"address\": {\"line1\": \"456 Market St\", \"line2\": \"Suite 8\", \"city\": \"Metropolis\", \"pincode\": \"54321\"}}")
                )
            ) 
            @Valid UserDto userDto, @PathVariable Long userId){
    	adminService.updateUser(userDto,userId);
		return ResponseEntity.ok().build();    	
    }
    
    @Operation(summary = "Bulk user creation", description = "Creates users in bulk from a CSV file.")
    @PostMapping(path="/bulk-user-creation", consumes= {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<List<String>> createBulkUser(@RequestParam MultipartFile file){
        LOGGER.info("File:"+file.getOriginalFilename());
        List<String> response = new ArrayList<>();

        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            List<CSVRecord> csvRecords = csvParser.getRecords();
            for(CSVRecord csvRecord: csvRecords){

                String email = null;
				try{
                    AddressDto addressDto = AddressDto.builder()
                            .line1(csvRecord.get("line1"))
                            .line2(csvRecord.get("line2"))
                            .city(csvRecord.get("city"))
                            .pincode(csvRecord.get("pincode"))
                            .build();

                    UserDto userDto = UserDto.builder()
                            .name(csvRecord.get("name"))
                            .email(csvRecord.get("email"))
                            .phone(csvRecord.get("phone"))
                            .flatNo(csvRecord.get("flatNo"))
                            .role(Role.valueOf(csvRecord.get("role")))
                            .address(addressDto)
                            .build();
                    email=userDto.getEmail();

                    Long userId = adminService.createUser(userDto);
                    response.add("Created User "+userDto.getName()+" with id "+userId);
                }
                catch (RuntimeException e) {
                    response.add("Got Exception while creating email id "+email);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(response);
    }
}
