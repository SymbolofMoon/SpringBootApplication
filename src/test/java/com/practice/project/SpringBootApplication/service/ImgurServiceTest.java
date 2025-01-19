package com.practice.project.SpringBootApplication.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.practice.project.SpringBootApplication.entity.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Base64;

public class ImgurServiceTest {

    @InjectMocks
    private ImgurService imgurService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<Map> responseEntity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // UPLOAD IMAGE SERVICE CHECK
    //1. Successful Image Upload
    @Test
    void shouldUploadImageSuccessfully() {
        // Prepare mock data
        byte[] imageData = new byte[]{1, 2, 3}; // Some example image byte data
        String imgurId = "image123";
        String imgurLink = "https://imgur.com/image123";
        String imgurDeleteHash = "deletehash123";
        String imgurName = "dummyName";

        // Prepare mock response body
        Map<String, Object> data = Map.of(
                "id", imgurId,
                "link", imgurLink,
                "deletehash", imgurDeleteHash
        );
        Map<String, Object> responseBody = Map.of("data", data);

        // Mock RestTemplate to return the expected response
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        // Call the method under test
        Image uploadedImage = imgurService.uploadImage(imageData, imgurName);

        // Verify the results
        assertNotNull(uploadedImage);
        assertEquals(imgurId, uploadedImage.getImgurId());
        assertEquals(imgurLink, uploadedImage.getImgurLink());
        assertEquals(imgurDeleteHash, uploadedImage.getImgurDeleteHash());

        // Verify that the RestTemplate was called with the correct URL and request
        verify(restTemplate, times(1)).postForEntity(eq("https://api.imgur.com/3/image"), any(), eq(Map.class));
    }

    //UPLOAD IMAGE SERVICE CHECK
    //2. Upload Failure Check
    @Test
    void shouldThrowRuntimeExceptionWhenImgurUploadFails() {
        // Prepare mock data
        byte[] imageData = new byte[]{1, 2, 3}; // Some example image byte data
        String imgurName = "dummyName";
        // Mock RestTemplate to throw an exception
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Failed to upload"));

        // Call the method under test and assert that an exception is thrown
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            imgurService.uploadImage(imageData, imgurName);
        });

        // Verify that the exception message is correct
        assertEquals("Failed to upload image to Imgur.", thrownException.getMessage());

        // Verify that the RestTemplate was called
        verify(restTemplate, times(1)).postForEntity(eq("https://api.imgur.com/3/image"), any(), eq(Map.class));
    }

    //DELETE IMAGE SERVICE CHECK
    //1. Successful Delete Image
    @Test
    void shouldDeleteImageSuccessfully() {
        // Prepare mock data
        String deleteHash = "deleteHash123";

        // Mock RestTemplate to simulate successful delete request
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Call the method under test
        imgurService.deleteImage(deleteHash);

        // Verify that the RestTemplate's exchange method was called with the correct arguments
        verify(restTemplate, times(1)).exchange(
                eq("https://api.imgur.com/3/image/" + deleteHash),
                eq(HttpMethod.DELETE),
                any(),
                eq(Void.class)
        );
    }

    //DELETE IMAGE SERVICE CHECK
    //2. Delete Fail
    @Test
    void shouldThrowRuntimeExceptionWhenDeleteFails() {
        // Prepare mock data
        String deleteHash = "deleteHash123";

        // Mock RestTemplate to simulate failure in deleting the image
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Failed to delete"));

        // Call the method under test and assert that an exception is thrown
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            imgurService.deleteImage(deleteHash);
        });

        // Verify the exception message
        assertEquals("Failed to delete image with ImageID: " + deleteHash, thrownException.getMessage());

        // Verify that the RestTemplate's exchange method was called
        verify(restTemplate, times(1)).exchange(
                eq("https://api.imgur.com/3/image/" + deleteHash),
                eq(HttpMethod.DELETE),
                any(),
                eq(Void.class)
        );
    }

    //IMAGEURL SERVICE CHECK
    //1. Valid Image ID
    @Test
    void testGetImageUrl_ValidImageId() {
        String validImageId = "abc123";
        String expectedUrl = "https://i.imgur.com/abc123.jpg";

        String actualUrl = imgurService.getImageUrl(validImageId);

        assertEquals(expectedUrl, actualUrl, "The generated URL should match the expected Imgur URL.");
    }

    //IMAGEURL SERVICE CHECK
    //2. Null Image ID
    @Test
    void testGetImageUrl_NullImageId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imgurService.getImageUrl(null);
        });

        assertEquals("Image ID must not be null or empty", exception.getMessage());
    }

    //IMAGEURL SERVICE CHECK
    //3. Empty Image ID
    @Test
    void testGetImageUrl_EmptyImageId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imgurService.getImageUrl("");
        });

        assertEquals("Image ID must not be null or empty", exception.getMessage());
    }

    //IMAGEURL SERVICE CHECK
    //4. WhiteSpace Image ID
    @Test
    void testGetImageUrl_WhitespaceImageId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imgurService.getImageUrl("   ");
        });

        assertEquals("Image ID must not be null or empty", exception.getMessage());
    }

    //IMAGEURL SERVICE CHECK
    //5. InValid Image ID
    @Test
    void testGetImageUrl_InvalidImageId_NonAlphanumeric() {
        String invalidImageId = "abc@123";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imgurService.getImageUrl(invalidImageId);
        });

        assertEquals("Invalid Image ID format: " + invalidImageId, exception.getMessage());
    }

    //IMAGEURL SERVICE CHECK
    //6. Special Character Image ID
    @Test
    void testGetImageUrl_InvalidImageId_SpecialCharacters() {
        String invalidImageId = "!@#$%^&*()";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imgurService.getImageUrl(invalidImageId);
        });

        assertEquals("Invalid Image ID format: " + invalidImageId, exception.getMessage());
    }

    //IMAGEURL SERVICE CHECK
    //7. InValid Image ID with Spaces
    @Test
    void testGetImageUrl_InvalidImageId_WithSpaces() {
        String invalidImageId = "abc 123";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imgurService.getImageUrl(invalidImageId);
        });

        assertEquals("Invalid Image ID format: " + invalidImageId, exception.getMessage());
    }

    //FETCH IMAGE FROM URL SERVICE CHECK
    //1. Successful Fetch Image from url
    @Test
    void testFetchImageFromUrl_SuccessfulResponse() {
        String imageUrl = "https://i.imgur.com/example.jpg";
        byte[] mockImageBytes = "mockImageData".getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        // Mock RestTemplate behavior
        ResponseEntity<byte[]> mockResponse = new ResponseEntity<>(mockImageBytes, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(mockResponse);

        // Call the method
        ResponseEntity<byte[]> response = imgurService.fetchImageFromUrl(imageUrl);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG.toString(), response.getHeaders().getContentType().toString());
        assertArrayEquals(mockImageBytes, response.getBody());
    }

    //FETCH IMAGE FROM URL SERVICE CHECK
    //2. UnSuccessful Fetch Image from Empty url
    @Test
    void testFetchImageFromUrl_EmptyUrl() {
        String imageUrl = "";

        // Assertions
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imgurService.fetchImageFromUrl(imageUrl);
        });

        assertEquals("Image URL must not be null or empty", exception.getMessage());
    }

    //FETCH IMAGE FROM URL SERVICE CHECK
    //3. UnSuccessful Fetch Image from null url
    @Test
    void testFetchImageFromUrl_NullUrl() {
        String imageUrl = null;

        // Assertions
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imgurService.fetchImageFromUrl(imageUrl);
        });

        assertEquals("Image URL must not be null or empty", exception.getMessage());
    }

    //FETCH IMAGE FROM URL SERVICE CHECK
    //4. UnSuccessful response
    @Test
    void testFetchImageFromUrl_UnsuccessfulResponse() {
        String imageUrl = "https://i.imgur.com/example.jpg";

        // Mock RestTemplate behavior
        ResponseEntity<byte[]> mockResponse = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(mockResponse);

        // Assertions
        Exception exception = assertThrows(RuntimeException.class, () -> {
            imgurService.fetchImageFromUrl(imageUrl);
        });

        assertEquals("Error occurred while fetching the image: Failed to fetch image. HTTP Status: 404 NOT_FOUND", exception.getMessage());
    }

    //FETCH IMAGE FROM URL SERVICE CHECK
    //5. Some Exception thrown
    @Test
    void testFetchImageFromUrl_ExceptionThrown() {
        String imageUrl = "https://i.imgur.com/example.jpg";

        // Mock RestTemplate to throw an exception
        when(restTemplate.getForEntity(imageUrl, byte[].class)).thenThrow(new RuntimeException("Connection timeout"));

        // Assertions
        Exception exception = assertThrows(RuntimeException.class, () -> {
            imgurService.fetchImageFromUrl(imageUrl);
        });

        assertTrue(exception.getMessage().contains("Error occurred while fetching the image: Connection timeout"));
    }
}



