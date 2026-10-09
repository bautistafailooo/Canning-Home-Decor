package com.uade.tpo.demo.controllers.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Path carpeta = Paths.get(uploadDir).toAbsolutePath().normalize();

        // La carpeta se crea ACA, al arrancar, y no recien cuando alguien
        // sube la primera foto.
        //
        // El motivo es una trampa de Path.toUri(): agrega la barra final
        // solo si la carpeta YA existe. Sin esa barra, Spring toma la
        // ubicacion como si fuera un archivo y responde 404 a cualquier
        // GET /uploads/lo-que-sea, aunque el archivo este en el disco.
        // Como este metodo corre antes que cualquier subida, en un
        // proyecto recien descargado la carpeta no existia todavia y las
        // fotos no se veian hasta reiniciar la aplicacion.
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear la carpeta de subidas: " + carpeta, e);
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(carpeta.toUri().toString());
    }
}