package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.model.Guest;
import com.realestate.sellerfunnel.model.Payment;
import com.realestate.sellerfunnel.model.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private GuestRepository guestRepository;

    private Booking booking(String roomNum, String email) {
        Room r = roomRepository.save(new Room(roomNum, null, "Single", new BigDecimal("90")));
        Guest g = new Guest();
        g.setFirstName("Pay"); g.setLastName("User"); g.setEmail(email);
        g = guestRepository.save(g);
        Booking b = new Booking();
        b.setRoom(r); b.setGuest(g);
        b.setCheckInDate(LocalDateTime.now().minusDays(1));
        b.setNightlyRate(new BigDecimal("90"));
        b.setTotalCharges(new BigDecimal("90"));
        b.setBookingStatus("ACTIVE");
        b.setPaymentStatus("PENDING");
        b.setIsActive(true);
        return bookingRepository.save(b);
    }

    private Payment payment(Booking b, BigDecimal amount, String method, String status) {
        Payment p = new Payment(b, amount, method);
        p.setPaymentStatus(status);
        p.setProcessedAt(LocalDateTime.now());
        return paymentRepository.save(p);
    }

    @Test
    @DisplayName("Sum completed UPP payments")
    void sumCompletedUpp() {
        Booking b = booking("801","p@test.com");
        payment(b,new BigDecimal("50"),"UPP_DEVICE","COMPLETED");
        payment(b,new BigDecimal("30"),"UPP_DEVICE","COMPLETED");
        payment(b,new BigDecimal("20"),"UPP_DEVICE","FAILED");
        assertThat(paymentRepository.sumCompletedUppPayments()).isEqualByComparingTo("80");
    }

    @Test
    @DisplayName("Search description and recent list")
    void searchAndRecent() {
        Booking b = booking("901","q@test.com");
        Payment p1 = payment(b,new BigDecimal("40"),"CASH","COMPLETED"); p1.setDescription("Breakfast charge"); paymentRepository.save(p1);
        Payment p2 = payment(b,new BigDecimal("60"),"CASH","COMPLETED"); p2.setDescription("Dinner service"); paymentRepository.save(p2);
        List<Payment> search = paymentRepository.searchByDescription("Dinner");
        assertThat(search).extracting(Payment::getDescription).containsExactly("Dinner service");
        List<Payment> recent = paymentRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
        assertThat(recent.size()).isGreaterThanOrEqualTo(2);
    }
}
