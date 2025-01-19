package com.practice.project.SpringBootApplication.controller;

import com.practice.project.SpringBootApplication.entity.Image;
import com.practice.project.SpringBootApplication.entity.User;
import com.practice.project.SpringBootApplication.repository.UserRepository;

import com.practice.project.SpringBootApplication.service.ImgurService;
import com.practice.project.SpringBootApplication.service.UserService;
import com.practice.project.SpringBootApplication.utility.JwtUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImgurService imgurService;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    private User mockUser;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // Mock User is creating
//        mockUser = new User();
//        mockUser.setUsername("testUser");
//        mockUser.setPassword("testPassword");
//        mockUser.setEmail("testEmail@example.com");
//        mockUser.setImages(new ArrayList<>());
//
//        // Mock repository to return the mock user when findByUsername is called
//        lenient().when(userRepository.findByUsername("testUser")).thenReturn(mockUser);
//
//        // Mock imgurService behavior for image upload
//        Image mockImage = new Image();
//        mockImage.setId(1L); // Set image ID
//        lenient().when(imgurService.uploadImage(any(byte[].class), any(String.class))).thenReturn(mockImage);


    }


    // REGISTRATION API
    // 1. Test For Successful registration
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // Mock behavior for successful registration
        when(userService.registerUser(any(User.class))).thenReturn("User registered successfully!");

        // Perform POST request
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\", \"email\":\"test@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));

        // Verify interaction with the service
        verify(userService, times(1)).registerUser(any(User.class));
    }

    // REGISTRATION API
    // 2. Test For Already exist Username registration
    @Test
    void shouldReturnBadRequestWhenRegistrationFails() throws Exception {
        // Mock behavior for failed registration
        when(userService.registerUser(any(User.class))).thenReturn("Username already in use!");

        // Perform POST request
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\", \"email\":\"test@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already in use!"));

        // Verify interaction with the service
        verify(userService, times(1)).registerUser(any(User.class));
    }

    // REGISTRATION API
    // 3. Test For Already Invalid User Input
    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        // Perform POST request with invalid input
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\", \"email\":\"invalid\", \"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        // Verify that the service is not called due to validation failure
        verify(userService, never()).registerUser(any(User.class));
    }

    //LOGIN API
    //1. Test case for successful user login
    @Test
    public void testLoginUser_Success() throws Exception {
        String username = "testUser";
        String password = "testPassword";
        String token = "mockJwtToken";


        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword(password);


        // Mock service behavior
        when(userService.loginUser(username, password)).thenReturn(true);
        when(jwtUtil.generateToken(username)).thenReturn(token);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"testUser\", \"password\": \"testPassword\" }"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("jwtToken=" + token)))
                .andExpect(content().string("User authenticated successfully"));
    }

    //LOGIN API
    //2. Test case for Invalid Credentials
    @Test
    public void testLoginUser_InvalidCredentials() throws Exception {
        String username = "invalidUser";
        String password = "invalidPassword";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPassword(password);

        // Mock service behavior
        when(userService.loginUser(username, password)).thenReturn(false);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"invalidUser\", \"password\": \"invalidPassword\" }"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    //LOGIN API
    //3. Test case for Missing username or password
    @Test
    public void testLoginUser_MissingUsernameOrPassword() throws Exception {
        // Missing username
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"password\": \"testPassword\" }"))
                .andExpect(status().isBadRequest());

        // Missing password
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"testUser\" }"))
                .andExpect(status().isBadRequest());

        //Empty Credentials
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"\", \"password\": \"\" }"))
                .andExpect(status().isBadRequest());
    }

    //GET PROFILE API
    // 1. Test case for successful user retrieval
    @Test
    public void testGetProfile_Success() throws Exception {
        // Mock Principal
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("testUser");

        // Mock User
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("testUser@example.com");
        mockUser.setImages(Collections.emptyList());

        // Mock Repository
        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);

        // Perform Test
        mockMvc.perform(get("/api/users/profile")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("testUser@example.com"))
                .andExpect(jsonPath("$.images").isEmpty());
    }


    //GET PROFILE API
    // 2. Test case for User Not Found
    @Test
    public void testGetProfile_UserNotFound() throws Exception {
        // Mock Principal
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("unknownUser");

        // Mock Repository
        when(userRepository.findByUsername("unknownUser")).thenReturn(null);

        // Perform Test
        mockMvc.perform(get("/api/users/profile")
                        .principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }


    //GET PROFILE API
    // 3. Test case for internal error (exception thrown in the service layer)
    @Test
    public void testGetProfile_DatabaseError() throws Exception {
        // Mock Principal
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("testUser");

        // Mock Repository with Exception
        when(userRepository.findByUsername("testUser")).thenThrow(new RuntimeException("Database error"));

        // Perform Test
        mockMvc.perform(get("/api/users/profile")
                        .principal(mockPrincipal))
                .andExpect(status().isInternalServerError());
    }

    //IMAGE UPLOAD API
    //1. Test for Successful Image Upload API
    @Test
    public void testUploadImage_Success() throws Exception {
        // Mock Principal
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("testUser");

        // Mock User
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("testUser@example.com");
        mockUser.setImages(new ArrayList<>());

        // Mock Repository
        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);

        // Mock Image Upload
        Image mockImage = new Image();
        mockImage.setImgurId("12345");
        mockImage.setImgurLink("https://i.imgur.com/12345.jpg");
        mockImage.setImgurDeleteHash("deleteHash123");

        when(imgurService.uploadImage(Mockito.any(), Mockito.anyString())).thenReturn(mockImage);

        // Prepare MultipartFile
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Perform Test
        mockMvc.perform(multipart("/api/users/upload")
                        .file(mockFile)
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("Image uploaded successfully"));
    }


    //IMAGE UPLOAD API
    //2. Test for UserNotFound for Image Upload API
    @Test
    public void testUploadImage_UserNotFound() throws Exception {
        // Mock Principal
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("unknownUser");

        // Mock Repository
        when(userRepository.findByUsername("unknownUser")).thenReturn(null);

        // Prepare MultipartFile
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Perform Test
        mockMvc.perform(multipart("/api/users/upload")
                        .file(mockFile)
                        .principal(mockPrincipal))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }


    //IMAGE UPLOAD API
    //3. Test for Image Upload Failure
    @Test
    public void testUploadImage_NullFile() throws Exception {
        // Mock Principal
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("testUser");

        // Mock User
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("testUser@example.com");
        mockUser.setImages(new ArrayList<>());

        lenient().when(userRepository.findByUsername("testUser")).thenReturn(mockUser);

        // Perform Test without file
        mockMvc.perform(multipart("/api/users/upload")
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest());
    }


    //IMAGE UPLOAD API
    //4. Test for Upload Failure
    @Test
    public void testUploadImage_ServiceFailure() throws Exception {
        // Mock Principal
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("testUser");

        // Mock User
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("testUser@example.com");
        mockUser.setImages(new ArrayList<>());

        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);

        // Mock Service Exception
        when(imgurService.uploadImage(Mockito.any(), Mockito.anyString()))
                .thenThrow(new RuntimeException("Imgur upload failed"));

        // Prepare MultipartFile
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // Perform Test
        mockMvc.perform(multipart("/api/users/upload")
                        .file(mockFile)
                        .principal(mockPrincipal))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Imgur upload failed"));
    }


    //GET IMAGE API
    //1. Test for Gat Image Successfull
    @Test
    public void testGetImage_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testUser");

        Image mockImage = new Image();
        mockImage.setImgurId("validImgurId");
        mockUser.setImages(Collections.singletonList(mockImage));

        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);
        when(imgurService.getImageUrl("validImgurId")).thenReturn("https://i.imgur.com/validImgurId.jpg");

        // Simulate image bytes returned by the Imgur service
        byte[] mockImageBytes = "mockImageData".getBytes(StandardCharsets.UTF_8);
        when(imgurService.fetchImageFromUrl("https://i.imgur.com/validImgurId.jpg"))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(mockImageBytes));

        mockMvc.perform(get("/api/users/images/validImgurId")
                        .principal(() -> "testUser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(mockImageBytes));
    }

    //GET IMAGE API
    //2. Test for User Not Found
    @Test
    public void testGetImage_UnauthorizedUser() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/users/images/someImgurId")
                        .principal(() -> "testUser"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Invalid credentials"));
    }

    //GET IMAGE API
    //3. Test for Image Not Found
    @Test
    public void testGetImage_ImageNotFound() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testUser");
        mockUser.setImages(Collections.emptyList()); // User has no images

        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/images/someImgurId")
                        .principal(() -> "testUser"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error fetching image: Image not found"));
    }

    //GET IMAGE API
    //4. Test for Image Service Failure
    @Test
    public void testGetImage_ExceptionHandling() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testUser");

        Image mockImage = new Image();
        mockImage.setImgurId("validImgurId");
        mockUser.setImages(Collections.singletonList(mockImage));

        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);
        when(imgurService.getImageUrl("validImgurId")).thenReturn("https://i.imgur.com/validImgurId.jpg");
        when(imgurService.fetchImageFromUrl(anyString()))
                .thenThrow(new RuntimeException("Imgur service failure"));

        mockMvc.perform(get("/api/users/images/validImgurId")
                        .principal(() -> "testUser"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error fetching image: Imgur service failure"));
    }

    //DELETE IMAGE UPLOAD API
    //1. Test for Delete Image Successful
    @Test
    public void testDeleteImage_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testUser");

        Image mockImage = new Image();
        mockImage.setImgurId("validImgurId");
        mockImage.setImgurDeleteHash("validDeleteHash");
        mockUser.setImages(new ArrayList<>(Collections.singletonList(mockImage)));

        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);
        doNothing().when(imgurService).deleteImage("validDeleteHash");

        mockMvc.perform(delete("/api/users/images/validImgurId")
                        .principal(() -> "testUser"))
                .andExpect(status().isOk())
                .andExpect(content().string("Image deleted successfully"));
    }

    //DELETE IMAGE UPLOAD API
    //2. Test for Image Does Not exist
    @Test
    public void testDeleteImage_ImageNotFound() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testUser");
        mockUser.setImages(Collections.emptyList()); // User has no images

        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);

        mockMvc.perform(delete("/api/users/images/someImgurId")
                        .principal(() -> "testUser"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Image not found"));
    }

    //DELETE IMAGE UPLOAD API
    //3. Test for Unauthorized user
    @Test
    public void testDeleteImage_InvalidCredentials() throws Exception {
        when(userRepository.findByUsername("testUser")).thenReturn(null);

        mockMvc.perform(delete("/api/users/images/someImgurId")
                        .principal(() -> "testUser"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    //DELETE IMAGE UPLOAD API
    //4. Test for Exceptional Handling
    @Test
    public void testDeleteImage_ExceptionHandling() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testUser");

        Image mockImage = new Image();
        mockImage.setImgurId("validImgurId");
        mockImage.setImgurDeleteHash("validDeleteHash");
        mockUser.setImages(Collections.singletonList(mockImage));

        when(userRepository.findByUsername("testUser")).thenReturn(mockUser);
        doThrow(new RuntimeException("Imgur service error")).when(imgurService).deleteImage("validDeleteHash");

        mockMvc.perform(delete("/api/users/images/validImgurId")
                        .principal(() -> "testUser"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Imgur service error"));
    }

}
