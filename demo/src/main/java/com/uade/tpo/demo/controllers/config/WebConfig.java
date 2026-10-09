package com.uade.tpo.demo.controllers.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sirve las fotos subidas: GET /uploads/abc.jpg devuelve el archivo
 * uploads/abc.jpg de la carpeta del proyecto.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
