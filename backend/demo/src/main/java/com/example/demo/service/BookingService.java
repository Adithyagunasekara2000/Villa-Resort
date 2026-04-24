package com.example.demo.service;

import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.Room;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Booking createBooking(BookingRequestDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                throw new RuntimeException("No authentication found");
            }
            
            String username = authentication.getName();
            System.out.println("Authenticated username: " + username);
            
            if (username == null || "anonymousUser".equals(username)) {
                throw new RuntimeException("User is not authenticated. Please login first.");
            }
            
            User currentUser = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + username));
            
            System.out.println("Found user: " + currentUser.getEmail());
            
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + dto.getRoomId()));
            
            System.out.println("Found room: " + room.getName() + ", Available: " + room.getIsAvailable());
            
            if (!room.getIsAvailable()) {
                throw new RuntimeException("Room is not available for booking");
            }
            
            Booking booking = new Booking();
            booking.setUser(currentUser);
            booking.setRoom(room);
            booking.setCheckIn(dto.getCheckIn());
            booking.setCheckOut(dto.getCheckOut());
            booking.setAdults(dto.getAdults());
            booking.setChildren(dto.getChildren() != null ? dto.getChildren() : 0);
            booking.setFirstName(dto.getFirstName());
            booking.setLastName(dto.getLastName());
            booking.setEmail(dto.getEmail());
            booking.setPhone(dto.getPhone());
            booking.setSpecialRequests(dto.getSpecialRequests());
            booking.setTotalAmount(dto.getTotalAmount());
            booking.setPaymentStatus("PENDING");
            
            Booking savedBooking = bookingRepository.save(booking);
            System.out.println("Booking saved with ID: " + savedBooking.getId());
            
            return savedBooking;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating booking: " + e.getMessage(), e);
        }
    }
    
   
    public java.util.List<Booking> getUserBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return bookingRepository.findByUser(currentUser);
    }
    
    @Transactional
    public void cancelBooking(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        if (!booking.getUser().getEmail().equals(username)) {
            throw new RuntimeException("You can only cancel your own bookings");
        }
        
        booking.setPaymentStatus("CANCELLED");
        bookingRepository.save(booking);
    }
    
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
    
    


    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public Booking updatePaymentStatus(Long id, String paymentStatus) {
        Booking booking = getBookingById(id);
        booking.setPaymentStatus(paymentStatus);
        
        // If payment is marked as PAID, also update booking status to CONFIRMED
        if ("PAID".equalsIgnoreCase(paymentStatus)) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        
        return bookingRepository.save(booking);
    }

    public Booking approveBooking(Long id) {
        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus("PAID"); // Auto-mark as paid when approved
        return bookingRepository.save(booking);
    }

    public Booking rejectBooking(Long id) {
        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus(BookingStatus.PENDING);
    }

    public Map<String, Object> getBookingStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count by status
        stats.put("total", bookingRepository.count());
        stats.put("pending", bookingRepository.countByStatus(BookingStatus.PENDING));
        stats.put("confirmed", bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        stats.put("cancelled", bookingRepository.countByStatus(BookingStatus.CANCELLED));
        stats.put("completed", bookingRepository.countByStatus(BookingStatus.COMPLETED));
        
        // Payment stats
        stats.put("paid", bookingRepository.countByPaymentStatus("PAID"));
        stats.put("pending_payment", bookingRepository.countByPaymentStatus("PENDING"));
        
        // Revenue stats
        Double totalRevenue = bookingRepository.getTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0);
        
        return stats;
    }
}