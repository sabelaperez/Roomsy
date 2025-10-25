package com.roomsy.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI roomsyOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setEmail("gonzalezdiosmartin@gmail.com");
        contact.setName("Roomsy Team");

        License mitLicense = new License()
                .name("MIT License")
                .url("");

        Info info = new Info()
                .title("Roomsy Backend API")
                .version("0.0.1-SNAPSHOT")
                .contact(contact)
                .description("API documentation for Roomsy backend application")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
