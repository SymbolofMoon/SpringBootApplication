package com.practice.project.SpringBootApplication.service;

import com.practice.project.SpringBootApplication.entity.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class ImgurService {

    private static final String IMGUR_API_URL = "https://api.imgur.com/3/";

    @Value("${imgur.client.id}")
    private String clientId;


    private static final Logger logger = LoggerFactory.getLogger(ImgurService.class);

    @Autowired
    private RestTemplate restTemplate;

    public Image uploadImage(byte[] imageData, String fileName) {

        logger.info("Starting image upload to Imgur.");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + clientId);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", Base64.getEncoder().encodeToString(imageData));
        body.add("name", fileName);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try{

            // Send request to Imgur API
            logger.debug("Sending image upload request to Imgur API: {}", IMGUR_API_URL + "image");


            ResponseEntity<Map> response = restTemplate.postForEntity(IMGUR_API_URL + "image", requestEntity, Map.class);

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            Image image = new Image();
            image.setImgurId(data.get("id").toString());
            image.setImgurLink(data.get("link").toString());
            image.setImgurDeleteHash(data.get("deletehash").toString());
            image.setImgurName(fileName);

            logger.info("Image uploaded successfully. Imgur ID: {}, Link: {}", image.getImgurId(), image.getImgurLink());
            return image;

        } catch (Exception e) {
            logger.error("Error occurred while uploading image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to Imgur.", e);
        }

    }

    public String getImageUrl(String imageId) {
        if (imageId == null || imageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Image ID must not be null or empty");
        }

        // Validate that the imageId contains only alphanumeric characters
        if (!imageId.matches("^[a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("Invalid Image ID format: " + imageId);
        }

        // Construct and return the Imgur-hosted image URL
        return "https://i.imgur.com/" + imageId + ".jpg";
    }

    public ResponseEntity<byte[]> fetchImageFromUrl(String imageUrl) {
        // Validate URL
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL must not be null or empty");
        }

        try {
            // Make an HTTP GET request to fetch the image
            ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String contentType = response.getHeaders().getContentType().toString(); // Extract content type
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType) // Dynamically set content type
                        .body(response.getBody());
            } else {
                throw new RuntimeException("Failed to fetch image. HTTP Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while fetching the image: " + e.getMessage(), e);
        }
    }


    public void deleteImage(String deleteHash) {

        logger.info("User is deleting an image with imageId : {}", deleteHash);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + clientId);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try{
            logger.debug("Sending DELETE request to Imgur API: {}", IMGUR_API_URL + "image/" + deleteHash);
            restTemplate.exchange(
                    IMGUR_API_URL + "image/" + deleteHash,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );

            logger.info("Image with delete hash '{}' deleted successfully.", deleteHash);
        } catch (Exception e) {
            logger.error("Error occurred while deleting image with ImageID '{}': {}", deleteHash, e.getMessage(), e);
            throw new RuntimeException("Failed to delete image with ImageID: " + deleteHash, e);

        }
    }
}

