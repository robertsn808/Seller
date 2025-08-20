package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.model.Guest;
import com.realestate.sellerfunnel.model.Payment;
import com.realestate.sellerfunnel.model.Room;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic integration-style test scaffold for UniversalPaymentProtocolService.
 * Disabled by default because it would call external service if available.
 */
class UniversalPaymentProtocolServiceTest {

    private final UniversalPaymentProtocolService service = new UniversalPaymentProtocolService();

    @Test
    @Disabled("Requires running UPP service")
    @DisplayName("Process payment against live UPP service")
    void liveProcessPayment() {
        Room room = new Room(); room.setRoomNumber("200");
        Guest guest = new Guest(); guest.setFirstName("Test"); guest.setLastName("User"); guest.setEmail("test@example.com");
        Booking booking = new Booking(); booking.setRoom(room); booking.setGuest(guest);
        Payment payment = new Payment(booking, new BigDecimal("10.00"), "UPP_DEVICE");
        payment.setDescription("Test payment");
        payment.setCustomerEmail("test@example.com");
        UniversalPaymentProtocolService.UppPaymentResult result = service.processPayment(payment, "smartphone", "test-device");
        assertThat(result).isNotNull();
    }
}
