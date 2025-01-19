package com.practice.project.SpringBootApplication.controller;

import com.practice.project.SpringBootApplication.DTO.UserDTO;
import com.practice.project.SpringBootApplication.entity.Image;
import com.practice.project.SpringBootApplication.entity.User;
import com.practice.project.SpringBootApplication.repository.UserRepository;
import com.practice.project.SpringBootApplication.service.ImgurService;
import com.practice.project.SpringBootApplication.service.MessagingService;
import com.practice.project.SpringBootApplication.service.UserService;
import com.practice.project.SpringBootApplication.utility.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

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


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid User user) {
        String response = userService.registerUser(user);
        if (response.equals("User registered successfully!")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody  User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        if(username==null || username.isEmpty() || password==null || password.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("username or password must not be blank");
        }

        boolean isAuthenticated = userService.loginUser(username, password);

        if (isAuthenticated) {
            // Generate JWT token
            String token = jwtUtil.generateToken(username);

            // Return token in the response

            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token)
                    .httpOnly(true)
                    .secure(true)  // Enable only for HTTPS
                    .path("/")
                    .maxAge(60 * 60)  // 1 hour
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body("User authenticated successfully");


        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }


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
            messagingService.publishEvent(user.getUsername(), image.getImgurName());

            // Save the user with the updated images list
            userService.addImagetoUser(image, user.getUsername());

            return ResponseEntity.ok("Image uploaded successfully");
        }   catch (Exception e) {
            // Catch any other unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Clear the cookie
        ResponseCookie cookie = ResponseCookie.from("jwtToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully");
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


    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(Principal principal) {

        String username = principal.getName();
        logger.info("Fetching profile information for user with username: {}", username);

        try {

            User user = userRepository.findByUsername(username);
            if (user == null) {
                logger.warn("User with username '{}' not found.", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            logger.debug("Profile information retrieved successfully for user: {}", username);

//            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getImages());
            UserDTO  userDTO = userService.getUserProfile(username);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            logger.error("Error fetching profile information for user", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
