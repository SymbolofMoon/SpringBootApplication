package com.practice.project.SpringBootApplication.entity;

import jakarta.persistence.*;

@Entity
@Table(name="image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    private String imgurId;
    private String imgurLink;
    private String imgurDeleteHash;
    private String imgurName;



    public Image() {
    }


    public Image(String imgurId, String imgurLink, String imgurDeleteHash, Long id, String imgurName) {
        this.imgurId = imgurId;
        this.imgurLink = imgurLink;
        this.imgurDeleteHash = imgurDeleteHash;
        this.id = id;
        this.imgurName = imgurName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImgurId() {
        return imgurId;
    }

    public String getImgurName() {
        return imgurName;
    }

    public void setImgurName(String imgurName) {
        this.imgurName = imgurName;
    }

    public void setImgurId(String imgurId) {
        this.imgurId = imgurId;
    }

    public String getImgurLink() {
        return imgurLink;
    }

    public void setImgurLink(String imgurLink) {
        this.imgurLink = imgurLink;
    }

    public String getImgurDeleteHash() {
        return imgurDeleteHash;
    }

    public void setImgurDeleteHash(String imgurDeleteHash) {
        this.imgurDeleteHash = imgurDeleteHash;
    }
}


