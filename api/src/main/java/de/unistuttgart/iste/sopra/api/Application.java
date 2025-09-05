package de.unistuttgart.iste.sopra.api;

import jakarta.annotation.Nonnull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main entry point for the Spring Boot application.
 * Configures the application and enables CORS for specified origins and HTTP methods.
 */
@SpringBootApplication
public class Application {

    /**
     * Main method to run the Spring Boot application.
     *
     * @param args Command-line arguments passed during application startup.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Configures CORS settings for the application.
     * Allows HTTP requests from specified origins with specified HTTP methods.
     *
     * @return A {@link WebMvcConfigurer} instance with custom CORS settings.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@Nonnull CorsRegistry registry) {
                // Allow CORS requests for all resources and HTTP methods from the specified frontend origins
                registry.addMapping("/**")
                        .allowedMethods("OPTIONS", "HEAD", "GET", "PUT", "POST", "DELETE", "PATCH")
                        .allowedOrigins("http://localhost:5173", "http://[2001:7c0:2320:1:f816:3eff:fee5:5fef]");
            }
        };
    }
}