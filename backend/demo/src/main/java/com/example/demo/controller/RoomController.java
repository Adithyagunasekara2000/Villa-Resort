package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.model.RoomImage;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.RoomImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, 
             allowedHeaders = "*", 
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class RoomController {
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomImageRepository roomImageRepository;
    
    @Value("${upload.path:uploads}")
    private String uploadPath;
    
    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Room API is working");
        response.put("status", "OK");
        response.put("time", new Date().toString());
        return ResponseEntity.ok(response);
    }
    
    // Get all rooms
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return ResponseEntity.ok(rooms);
    }
    
    // Get room by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomRepository.findById(id);
        if (room.isPresent()) {
            return ResponseEntity.ok(room.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found with id: " + id);
            return ResponseEntity.status(404).body(error);
        }
    }
    
    // Create room (without images )
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody Room room) {
        try {
            System.out.println("Creating room: " + room.getName());
            
            // Set default values if not provided
            if (room.getIsAvailable() == null) {
                room.setIsAvailable(true);
            }
            if (room.getAmenities() == null) {
                room.setAmenities(new ArrayList<>());
            }
            
            Room savedRoom = roomRepository.save(room);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Room created successfully");
            response.put("roomId", savedRoom.getId());
            response.put("room", savedRoom);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create room");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Create room with images (multipart form data)
    @PostMapping(value = "/create-with-images", consumes = "multipart/form-data")
    public ResponseEntity<?> createRoomWithImages(
        @RequestParam("name") String name,
        @RequestParam("category") String category,
        @RequestParam("description") String description,
        @RequestParam("price") Double price,
        @RequestParam("size") Double size,
        @RequestParam("capacity") Integer capacity,
        @RequestParam("beds") String beds,
        @RequestParam("amenities") String amenities,
        @RequestParam(value = "promotion", required = false) String promotion,
        @RequestParam(value = "isAvailable", defaultValue = "true") Boolean isAvailable,
        @RequestParam(value = "images", required = false) MultipartFile[] images) {
    
        try {
            System.out.println("Creating room with images: " + name);
            
            
            Room room = new Room();
            room.setName(name);
            room.setCategory(category);
            room.setDescription(description);
            room.setPrice(price);
            room.setSize(size);
            room.setCapacity(capacity);
            room.setBeds(beds);
            room.setPromotion(promotion);
            room.setIsAvailable(isAvailable);
            
           
            if (amenities != null && !amenities.trim().isEmpty()) {
                String[] amenitiesArray = amenities.split(",");
                List<String> amenitiesList = new ArrayList<>();
                for (String amenity : amenitiesArray) {
                    if (amenity != null && !amenity.trim().isEmpty()) {
                        amenitiesList.add(amenity.trim());
                    }
                }
                room.setAmenities(amenitiesList);
            } else {
                room.setAmenities(new ArrayList<>());
            }
            
            
            Room savedRoom = roomRepository.save(room);
            System.out.println("✅ Room saved with ID: " + savedRoom.getId());
            
            List<RoomImage> roomImages = new ArrayList<>();
            if (images != null && images.length > 0) {
                Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                    System.out.println("Created upload directory: " + uploadDir);
                }
                
                for (MultipartFile image : images) {
                    if (image == null || image.isEmpty()) {
                        continue;
                    }
                    
                    String originalFilename = image.getOriginalFilename();
                    if (originalFilename == null || originalFilename.trim().isEmpty()) {
                        continue;
                    }
                    
                    String fileExtension = "";
                    if (originalFilename.contains(".")) {
                        fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    String filename = UUID.randomUUID().toString() + fileExtension;
                    
                    Path filePath = uploadDir.resolve(filename);
                    Files.copy(image.getInputStream(), filePath);
                    
                    RoomImage roomImage = new RoomImage();
                    roomImage.setFileName(filename);
                    roomImage.setFileUrl("/uploads/" + filename);
                    roomImage.setRoom(savedRoom);
                    
                    RoomImage savedImage = roomImageRepository.save(roomImage);
                    roomImages.add(savedImage);
                }
                
                if (!roomImages.isEmpty()) {
                    savedRoom.setImages(roomImages);
                    roomRepository.save(savedRoom);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Room created successfully with images");
            response.put("roomId", savedRoom.getId());
            response.put("roomName", savedRoom.getName());
            response.put("imagesCount", roomImages.size());
            response.put("room", savedRoom);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create room with images");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Update room
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        try {
            System.out.println("Updating room with ID: " + id);
            
            Optional<Room> roomOptional = roomRepository.findById(id);
            if (roomOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Room not found with id: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            Room room = roomOptional.get();
            
            // Update fields if provided
            if (roomDetails.getName() != null) {
                room.setName(roomDetails.getName());
            }
            if (roomDetails.getCategory() != null) {
                room.setCategory(roomDetails.getCategory());
            }
            if (roomDetails.getDescription() != null) {
                room.setDescription(roomDetails.getDescription());
            }
            if (roomDetails.getPrice() != null) {
                room.setPrice(roomDetails.getPrice());
            }
            if (roomDetails.getSize() != null) {
                room.setSize(roomDetails.getSize());
            }
            if (roomDetails.getCapacity() != null) {
                room.setCapacity(roomDetails.getCapacity());
            }
            if (roomDetails.getBeds() != null) {
                room.setBeds(roomDetails.getBeds());
            }
            if (roomDetails.getPromotion() != null) {
                room.setPromotion(roomDetails.getPromotion());
            }
            if (roomDetails.getIsAvailable() != null) {
                room.setIsAvailable(roomDetails.getIsAvailable());
            }
            if (roomDetails.getAmenities() != null) {
                room.setAmenities(roomDetails.getAmenities());
            }
            
            Room updatedRoom = roomRepository.save(room);
            System.out.println("✅ Room updated successfully: " + updatedRoom.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Room updated successfully");
            response.put("room", updatedRoom);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update room");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Delete room
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            if (!roomRepository.existsById(id)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Room not found with id: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            roomRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Room deleted successfully");
            response.put("roomId", id.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete room");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Add images to existing room
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<?> addRoomImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] images) {
        
        try {
            System.out.println("Adding images to room ID: " + id);
            
            Optional<Room> roomOptional = roomRepository.findById(id);
            if (roomOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Room not found with id: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            Room room = roomOptional.get();
            List<RoomImage> roomImages = new ArrayList<>();
            
            if (images != null && images.length > 0) {
                Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                
                for (MultipartFile image : images) {
                    if (image == null || image.isEmpty()) {
                        continue;
                    }
                    
                    String originalFilename = image.getOriginalFilename();
                    if (originalFilename == null || originalFilename.trim().isEmpty()) {
                        continue;
                    }
                    
                    String fileExtension = "";
                    if (originalFilename.contains(".")) {
                        fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    String filename = UUID.randomUUID().toString() + fileExtension;
                    
                    Path filePath = uploadDir.resolve(filename);
                    Files.copy(image.getInputStream(), filePath);
                    
                    RoomImage roomImage = new RoomImage();
                    roomImage.setFileName(filename);
                    roomImage.setFileUrl("/uploads/" + filename);
                    roomImage.setRoom(room);
                    
                    RoomImage savedImage = roomImageRepository.save(roomImage);
                    roomImages.add(savedImage);
                    System.out.println("✅ Image saved: " + filename);
                }
                
                List<RoomImage> existingImages = room.getImages();
                if (existingImages == null) {
                    existingImages = new ArrayList<>();
                }
                existingImages.addAll(roomImages);
                room.setImages(existingImages);
                roomRepository.save(room);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Images added successfully");
            response.put("roomId", id);
            response.put("imagesAdded", roomImages.size());
            
            System.out.println("✅ Total images added: " + roomImages.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add images");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}