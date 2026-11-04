package com.biblioteca.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger disponible en http://localhost:8080/swagger-ui.html (botón "Authorize" para el JWT). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String esquema = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Gestión de Biblioteca - API REST")
                        .version("1.0.0")
                        .description("Libros, autores, usuarios, préstamos, devoluciones y reservas"))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
