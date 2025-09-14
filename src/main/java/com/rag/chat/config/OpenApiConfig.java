package com.rag.chat.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
        /**
         * Swagger API documentation
         * Authentication Strategy Included
         * @return
         */
        @Bean
        public OpenAPI chatServiceOpenAPI() {

                SecurityScheme apiKey = new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("Internal API Key authentication (REQUIRED: must be combined with JWT Bearer)");

                SecurityScheme bearer = new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT bearer token (REQUIRED: must be combined with API Key)");

                SecurityRequirement dualAuthRequirement = new SecurityRequirement()
                        .addList("api_key")
                        .addList("bearer_jwt");

                return new OpenAPI()
                        .info(new Info()
                                .title("RAG Chat Storage Service")
                                .version("v1")
                                .description("""
                            Persistence API for chat sessions & messages in a RAG workflow.
                            Features: sessions CRUD, messages, favorites, soft delete, dual auth (API Key + JWT), rate limiting.
                            """)
                                .contact(new Contact().name("Platform Team").email("keamp84@gmail.com"))
                                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
                        )
                        .components(new Components()
                                .addSecuritySchemes("api_key", apiKey)
                                .addSecuritySchemes("bearer_jwt", bearer)
                        )
                        .addSecurityItem(dualAuthRequirement);
        }
}