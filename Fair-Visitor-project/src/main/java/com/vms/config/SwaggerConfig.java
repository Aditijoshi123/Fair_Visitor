package com.vms.config;import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Fair Visitor API Documentation")
                .description("API documentation for all the endpoints of Fair Visitor Spring Boot application.")
                .version("1.0")
                .contact(new Contact()
                    .name("Aditi Joshi")
                    .email("joshiadisha2001@gmail.com"))
                )
            .components(new Components()
                .addSecuritySchemes("basicAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }    
}
