package com.example.demo.controller;

import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.dto.BookingResponseDTO;
import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:3000")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @RequestBody BookingRequestDTO dto) {

        Booking booking = bookingService.createBooking(dto);

        return ResponseEntity.ok(
                new BookingResponseDTO(
                        booking.getId(),
                        "Booking created successfully"
                )
        );
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getUserBookings(@PathVariable Long userId) {
        try {
            // You might want to add authentication check here
            List<Booking> bookings = bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
