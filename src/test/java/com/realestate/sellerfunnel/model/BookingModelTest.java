package com.realestate.sellerfunnel.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingModelTest {

    @Test
    @DisplayName("Balance and payment status update correctly with charges and payments")
    void balanceAndStatusUpdates() {
        Room room = new Room();
        room.setRoomNumber("101");
        Guest guest = new Guest();
        guest.setFirstName("Jane");
        guest.setLastName("Smith");
        guest.setEmail("jane@example.com");
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setCheckInDate(LocalDateTime.now().minusDays(2));

        booking.addCharge(new BigDecimal("200.00"));
        assertThat(booking.getCurrentBalance()).isEqualTo(new BigDecimal("200.00"));
        assertThat(booking.getPaymentStatus()).isEqualTo("PENDING");

        booking.addPayment(new BigDecimal("50.00"));
        assertThat(booking.getCurrentBalance()).isEqualTo(new BigDecimal("150.00"));
        assertThat(booking.getPaymentStatus()).isEqualTo("PARTIAL");

        booking.addPayment(new BigDecimal("150.00"));
        assertThat(booking.getCurrentBalance()).isEqualTo(new BigDecimal("0.00"));
        assertThat(booking.getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("calculateNights computes number of nights correctly")
    void calculateNights() {
        Room room = new Room();
        room.setRoomNumber("102");
        Guest guest = new Guest();
        guest.setFirstName("Mark");
        guest.setLastName("Jones");
        guest.setEmail("mark@example.com");
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setCheckInDate(LocalDateTime.now().minusHours(50)); // ~2 days, 2 nights
        booking.calculateNights();
        assertThat(booking.getNumberOfNights()).isBetween(1,3); // allow timing variance
    }
}
