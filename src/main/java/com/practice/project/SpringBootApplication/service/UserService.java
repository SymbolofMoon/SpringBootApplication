package com.practice.project.SpringBootApplication.service;


import com.practice.project.SpringBootApplication.DTO.UserDTO;
import com.practice.project.SpringBootApplication.entity.Image;
import com.practice.project.SpringBootApplication.entity.User;
import com.practice.project.SpringBootApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    public String registerUser(User user) {
        String username = user.getUsername();
        String email = user.getEmail();

        logger.info("Registering user with username: {}", username);

        try {
            // Check if username already exists
            if (userRepository.existsByUsername(username)) {
                logger.warn("Registration failed. Username '{}' is already taken.", username);
                return "Username is already taken.";
            }

            // Check if email already exists
            if (userRepository.existsByEmail(email)) {
                logger.warn("Registration failed. Email '{}' is already in use.", email);
                return "Email is already in use.";
            }

            // Encode the password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Save the user
            userRepository.save(user);
            logger.debug("User '{}' successfully registered.", username);

            return "User registered successfully!";
        } catch (Exception e) {
            logger.error("Error registering user '{}': {}", username, e.getMessage(), e);
            return "An error occurred during registration.";
        }
    }


    public boolean loginUser(String username, String password) {

        User user = userRepository.findByUsername(username);

       if(user== null) return false;

       return passwordEncoder.matches(password, user.getPassword());

    }

    @Cacheable(value = "userProfiles", key = "#username")
    public UserDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            // Return a DTO containing only non-sensitive data (without password)
            return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getImages());
        }
        return null;
    }

    @Transactional
    @CacheEvict(value = "userProfiles", key = "#username")
    public void addImagetoUser(Image image, String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }

        // Add the image to the user's profile
        user.getImages().add(image);

        // Save the updated user entity to the database
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "userProfiles", key = "#username")
    public void deleteImagetoUser(Image image, String username) {
        // Retrieve the user by username
        User user = userRepository.findByUsername(username);
        String imageName = image.getImgurName();
        if (user == null) {
            throw new IllegalArgumentException("User not found with username: " + username);
        }

        // Find and remove the image from the user's image list
        boolean removed = user.getImages().remove(image);

        if (!removed) {
            throw new IllegalArgumentException("Image not found with name: " + imageName);
        }

        // Save the updated user entity to the database
        userRepository.save(user);
    }
}
