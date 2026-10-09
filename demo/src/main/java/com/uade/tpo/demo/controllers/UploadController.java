package com.uade.tpo.demo.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Subida de fotos de productos.
 *
 * El front manda la imagen como multipart/form-data (campo "file"). El
 * archivo se guarda en la carpeta "uploads" del proyecto con un nombre
 * unico, y se devuelve la URL publica para guardarla en imageUrls del
 * producto. La base sigue guardando direcciones, no archivos.
 *
 * Subir es solo del ADMIN y ver las fotos es publico: lo define
 * SecurityConfig. WebConfig es quien sirve los archivos en GET /uploads/**.
 */
@RestController
@RequestMapping("uploads")
public class UploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "No llego ningun archivo"));

        String extension = extensionOf(file.getOriginalFilename());
        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")
                || !ALLOWED_EXTENSIONS.contains(extension))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Solo se aceptan imagenes JPG, PNG, WEBP o GIF"));

        // Nombre unico: dos fotos llamadas "foto.jpg" no se pisan.
        String fileName = UUID.randomUUID() + "." + extension;

        Path folder = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(folder);
        Files.copy(file.getInputStream(), folder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        // URL completa (http://localhost:4002/uploads/...), porque el front
        // corre en otro puerto y la usa tal cual en el <img>.
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(Map.of("url", url));
    }

    private String extensionOf(String originalName) {
        if (originalName == null || !originalName.contains("."))
            return "";
        return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
    }
}
