package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.model.Guest;
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
class BookingRepositoryTest {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private GuestRepository guestRepository;

    private Room room(String num) { return roomRepository.save(new Room(num, null, "Single", new BigDecimal("100"))); }
    private Guest guest(String email) {
        Guest g = new Guest();
        g.setFirstName("Test");
        g.setLastName(email.split("@")[0]);
        g.setEmail(email);
        return guestRepository.save(g);
    }
    private Booking booking(Room r, Guest g, String status, String payStatus, int daysAgo) {
        Booking b = new Booking();
        b.setRoom(r); b.setGuest(g);
        b.setCheckInDate(LocalDateTime.now().minusDays(daysAgo));
        b.setNightlyRate(new BigDecimal("100"));
        b.setTotalCharges(new BigDecimal("100"));
        b.setBookingStatus(status);
        b.setPaymentStatus(payStatus);
        b.setIsActive(true);
        return bookingRepository.save(b);
    }

    @Test
    @DisplayName("Counts active, pending, overdue bookings")
    void countsAggregations() {
        Room r1 = room("501");
        Guest g1 = guest("a@test.com");
        booking(r1,g1,"ACTIVE","PENDING",1);
        booking(r1,g1,"ACTIVE","OVERDUE",2);
        booking(r1,g1,"COMPLETED","PAID",3);

        assertThat(bookingRepository.countActiveBookings()).isEqualTo(2L);
        assertThat(bookingRepository.countPendingPayments()).isEqualTo(1L);
        assertThat(bookingRepository.countOverduePayments()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Find bookings with outstanding balance and overdue")
    void findOutstandingAndOverdue() {
        Room r = room("601");
        Guest g = guest("b@test.com");
        Booking active = booking(r,g,"ACTIVE","PENDING",1);
        active.setCurrentBalance(new BigDecimal("80"));
        bookingRepository.save(active);
        Booking overdue = booking(r,g,"ACTIVE","OVERDUE",2);
        overdue.setCurrentBalance(new BigDecimal("120"));
        bookingRepository.save(overdue);

        List<Booking> outstanding = bookingRepository.findBookingsWithOutstandingBalance();
        List<Booking> overdueList = bookingRepository.findOverdueBookings();
        assertThat(outstanding).hasSize(2);
        assertThat(overdueList).extracting(Booking::getPaymentStatus).containsOnly("OVERDUE");
    }

    @Test
    @DisplayName("Search term matches guest email and room number")
    void searchTermMatches() {
        Room r = room("701");
        Guest g = guest("search@test.com");
        booking(r,g,"ACTIVE","PENDING",0);
        List<Booking> byEmail = bookingRepository.findBySearchTerm("search@");
        List<Booking> byRoom = bookingRepository.findBySearchTerm("701");
        assertThat(byEmail).hasSize(1);
        assertThat(byRoom).hasSize(1);
    }
}
