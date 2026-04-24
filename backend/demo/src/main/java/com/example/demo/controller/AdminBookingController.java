package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bookings")  
@CrossOrigin(origins = "http://localhost:3000")
public class AdminBookingController {

    @Autowired
    private BookingService bookingService;

    // Get all bookings (admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            e.printStackTrace(); // Add logging
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get booking by ID
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update payment status
    @PutMapping("/{bookingId}/payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request) {
        try {
            String paymentStatus = request.get("paymentStatus");
            Booking booking = bookingService.updatePaymentStatus(bookingId, paymentStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment status updated successfully");
            response.put("booking", booking);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to update payment status: " + e.getMessage()));
        }
    }

    // Approve booking
    @PutMapping("/{bookingId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.approveBooking(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Booking approved successfully");
            response.put("booking", booking);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to approve booking: " + e.getMessage()));
        }
    }

    // Reject booking
    @PutMapping("/{bookingId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.rejectBooking(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Booking rejected successfully");
            response.put("booking", booking);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to reject booking: " + e.getMessage()));
        }
    }

    // Get booking statistics
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingStats() {
        try {
            Map<String, Object> stats = bookingService.getBookingStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace(); // Add logging
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get pending bookings
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getPendingBookings() {
        try {
            List<Booking> bookings = bookingService.getPendingBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}