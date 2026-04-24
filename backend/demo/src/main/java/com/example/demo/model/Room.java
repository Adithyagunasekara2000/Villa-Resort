package com.example.demo.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")  
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String category;
    private String description;
    private Double price;
    private Double size;
    private Integer capacity;
    private String beds;
    private String promotion;
    private Boolean isAvailable = true;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "room_amenities",
        joinColumns = @JoinColumn(name = "room_id")
    )
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RoomImage> images = new ArrayList<>();
    
    public Room() {
    }
    
    public Room(String name, String category, String description, Double price) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Double getSize() {
        return size;
    }
    
    public void setSize(Double size) {
        this.size = size;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public String getBeds() {
        return beds;
    }
    
    public void setBeds(String beds) {
        this.beds = beds;
    }
    
    public String getPromotion() {
        return promotion;
    }
    
    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public List<String> getAmenities() {
        return amenities;
    }
    
    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }
    
    public List<RoomImage> getImages() {
        return images;
    }
    
    public void setImages(List<RoomImage> images) {
        this.images = images;
    }
}