package com.practice.project.SpringBootApplication.controller;

import com.practice.project.SpringBootApplication.entity.Image;
import com.practice.project.SpringBootApplication.entity.User;
import com.practice.project.SpringBootApplication.repository.UserRepository;
import com.practice.project.SpringBootApplication.service.ImgurService;
import com.practice.project.SpringBootApplication.service.MessagingService;
import com.practice.project.SpringBootApplication.service.UserService;
import com.practice.project.SpringBootApplication.utility.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;


@RestController
@RequestMapping("/api")
public class ImageController {


    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImgurService imgurService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is missing or empty.");
        }

        try {
            // Validate the user
            User user = userRepository.findByUsername(principal.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not found");
            }

            // Attempt to upload the image
            Image image = imgurService.uploadImage(file.getBytes(), file.getOriginalFilename());

            // Publish event to Kafka
            boolean kafka = messagingService.publishEvent(user.getUsername(), image.getImgurName());
            if(kafka){
                logger.info("Message is sent to publisher");
            }

            // Save the user with the updated images list
            userService.addImagetoUser(image, user.getUsername());

            return ResponseEntity.ok("Image uploaded successfully");
        }   catch (Exception e) {
            // Catch any other unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/images/{imgurId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imgurId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());

        if (user == null) {
            String errorMessage = "Invalid credentials";
            byte[] errorBytes = errorMessage.getBytes(StandardCharsets.UTF_8); // Encode error message to bytes
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.TEXT_PLAIN) // Set appropriate content type
                    .body(errorBytes);
        }

        try {

            Image image = user.getImages().stream()
                    .filter(img -> img.getImgurId().equals(imgurId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Image not found"));
            String imageUrl = imgurService.getImageUrl(imgurId);
            return imgurService.fetchImageFromUrl(imageUrl);

        } catch (Exception e) {
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Error handling
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error fetching image: " + e.getMessage()).getBytes());
        }
    }

    @DeleteMapping("/images/{imgurId}")
    public ResponseEntity<String> deleteImage(@PathVariable String imgurId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        try {
            Image image = user.getImages().stream()
                    .filter(img -> img.getImgurId().equals(imgurId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Image not found"));


            imgurService.deleteImage(image.getImgurDeleteHash());

            userService.deleteImagetoUser(image, user.getUsername());

            return ResponseEntity.ok("Image deleted successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("An unexpected error occurred: " + e.getMessage()));
        }
    }
}
