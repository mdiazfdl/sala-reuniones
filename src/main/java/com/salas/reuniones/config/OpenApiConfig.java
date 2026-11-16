package com.salas.reuniones.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Sala de reuniones API",
        version = "v1",
        description = "API para reservar salas de reuniones. Las consultas son públicas; crear y cancelar requieren HTTP Basic."))
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Usuario por defecto: admin / admin123 (APP_USER y APP_PASSWORD)")
public class OpenApiConfig {
}
