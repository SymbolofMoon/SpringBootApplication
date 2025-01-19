package com.practice.project.SpringBootApplication.repository;

import com.practice.project.SpringBootApplication.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
