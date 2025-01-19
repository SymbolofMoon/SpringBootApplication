package com.practice.project.SpringBootApplication.DTO;

import com.practice.project.SpringBootApplication.entity.Image;

import java.util.ArrayList;
import java.util.List;

public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private List<Image> images = new ArrayList<>();

    public UserDTO(Long id, String username, String email, List<Image> images) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.images = images;
    }

    public UserDTO(String username, String email, List<Image> images) {
        this.username = username;
        this.email = email;
        this.images = images;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
}
