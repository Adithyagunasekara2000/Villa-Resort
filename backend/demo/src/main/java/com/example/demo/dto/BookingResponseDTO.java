package com.example.demo.dto;

public class BookingResponseDTO {

    public Long bookingId;
    public String message;

    public BookingResponseDTO(Long bookingId, String message) {
        this.bookingId = bookingId;
        this.message = message;
    }
}
