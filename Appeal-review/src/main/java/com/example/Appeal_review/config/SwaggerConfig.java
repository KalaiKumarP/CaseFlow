package com.example.Appeal_review.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI caseflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CaseFlow — Appeals & Review Management API")
                        .description(
                                "Module 4.5 of CaseFlow Legal Case Management System. " +
                                        "Handles appeal filing, judge review assignment, " +
                                        "decision recording, and review tracking."
                        )
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CaseFlow Dev Team")
                                .email("dev@caseflow.com"))
                        .license(new License()
                                .name("Internal Use Only")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Local Development Server")
                ));
    }
}
