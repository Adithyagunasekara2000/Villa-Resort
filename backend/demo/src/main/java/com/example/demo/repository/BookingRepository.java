package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	 List<Booking> findByUser(User user);
	 List<Booking> findByUserId(Long userId);
	 List<Booking> findAllByOrderByCreatedAtDesc();
	    
	 List<Booking> findByStatus(BookingStatus status);
	    
	 List<Booking> findByPaymentStatus(String paymentStatus);
	    
	 @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = ?1")
	    long countByStatus(BookingStatus status);
	    
	 @Query("SELECT COUNT(b) FROM Booking b WHERE b.paymentStatus = ?1")
	    long countByPaymentStatus(String paymentStatus);
	    
	 @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.paymentStatus = 'PAID'")
	    Double getTotalRevenue();   
}
