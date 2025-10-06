package com.example.rentaride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class RentarideApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentarideApplication.class, args);
	}

    @Bean
    @ConfigurationProperties(prefix = "app.cors")
    public CorsProps corsProps() {
        return new CorsProps();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(CorsProps corsProps) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(corsProps.getAllowedOrigins().split(","))
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                        .allowCredentials(true);
            }
        };
    }

    public static class CorsProps {
        private String allowedOrigins = "http://localhost:5173";

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }
}

