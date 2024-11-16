package com.vms.controller;


import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vms.dto.VisitDto;
import com.vms.dto.VisitorDto;
import com.vms.service.GateKeeperService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/gatekeeper")
@Tag(name = "GateKeeperController", description = "Operations for managing visitors and visits by the Gatekeeper")
public class GateKeeperController {

    private Logger LOGGER = LoggerFactory.getLogger(GateKeeperController.class);

    @Autowired
    private GateKeeperService gateKeeperService;

    @Value("${static.domain.name}")
    private String staticDomainName;

    @Value("${image.upload.home}")
    private String imageUploadHome;

    @Operation(summary = "Create a new visitor")
    @PostMapping("/createVisitor")
    ResponseEntity<Long> createVisitor( @RequestBody(
            description = "Details of the visitor to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = VisitorDto.class),
                examples = @ExampleObject(value = """
                {
                  "name": "John Doe",
                  "email": "johndoe@example.com",
                  "phone": "1234567890",
                  "address": {
                    "line1": "123 Main St",
                    "line2": "Apt 4B",
                    "city": "Springfield",
                    "pincode": "98765"
                  }
                }
                """)
            )
        )@Valid VisitorDto visitorDto){
        Long id = gateKeeperService.createVisitor(visitorDto);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Create a new visit")
    @PostMapping("/createVisit")
    ResponseEntity<Long> createVisit(@RequestBody(
            description = "Details of the visit to create",
            required = true,
            content = @Content(
                schema = @Schema(implementation = VisitDto.class),
                examples = @ExampleObject(value = """
                {
                  "flatNumber": "101A",
                  "email": "johndoe@example.com",
                  "noOfPeople": 3,
                  "purpose": "Business Meeting",
                  "urlOfImage": "http://example.com/image.jpg"
                }
                """)
            )
        )  @Valid VisitDto visitDto){
        Long id = gateKeeperService.createVisit(visitDto);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "Mark entry for a visit")
    @PutMapping("/markEntry/{id}")
    ResponseEntity<String> markEntry(@PathVariable Long id){
        return ResponseEntity.ok(gateKeeperService.markEntry(id));
    }

    @Operation(summary = "Mark exit for a visit")
    @PutMapping("/markExit/{id}")
    ResponseEntity<String> markExit(@PathVariable Long id){
        return ResponseEntity.ok(gateKeeperService.markExit(id));
    }

    @Operation(summary = "Upload an image")
    @PostMapping(path="/image-upload", consumes= {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> imageUpload(@RequestParam 
        @RequestBody(
            description = "The image file to upload (JPEG or PNG format only)",
            required = true,
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(type = "string", format = "binary")
            )
        )MultipartFile file){
        String fileName = UUID.randomUUID()+"_"+file.getOriginalFilename();
        String uploadPath = imageUploadHome+fileName;
        String publicUrl =staticDomainName+"content/"+fileName;

        try {
            file.transferTo(new File(uploadPath));
        } catch (IOException e) {
        	 LOGGER.error("Exception while uploading image: {}", e);
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception while uploading image: " + e.getMessage());
        }
        return ResponseEntity.ok(publicUrl);
    }


}
