package com.practice.project.SpringBootApplication.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="candidate", indexes = @Index(name = "idx_username", columnList = "username"))
public class User {

    public User() {

    }
    public User(Long id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @NotBlank(message = "Username is mandatory")
    @Column(unique = true, nullable = false, name="username")
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Column(nullable = false, name="password")
    private String password;

    @NotBlank(message = "Email is mandatory")
    @Column(unique = true, nullable = false, name="email")
    private String email;

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();


    // Constructors, getters, and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Username is mandatory") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Username is mandatory") String username) {
        this.username = username;
    }

    public @NotBlank(message = "Password is mandatory") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is mandatory") String password) {
        this.password = password;
    }

    public @NotBlank(message = "Email is mandatory") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email is mandatory") String email) {
        this.email = email;
    }

}
