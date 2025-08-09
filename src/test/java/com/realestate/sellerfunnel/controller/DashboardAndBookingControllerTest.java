package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.model.Guest;
import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.repository.BookingRepository;
import com.realestate.sellerfunnel.repository.GuestRepository;
import com.realestate.sellerfunnel.repository.PaymentRepository;
import com.realestate.sellerfunnel.repository.RoomRepository;
import com.realestate.sellerfunnel.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
@ActiveProfiles("test")
class DashboardAndBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private RoomRepository roomRepository;
    @MockBean private GuestRepository guestRepository;
    @MockBean private BookingRepository bookingRepository;
    @MockBean private PaymentRepository paymentRepository;
    @MockBean private PaymentService paymentService;

    private Booking sampleBooking(long id) {
        Booking b = new Booking();
        b.setId(id);
        b.setBookingStatus("ACTIVE");
        b.setCheckInDate(LocalDateTime.now().minusDays(1));
        b.setNightlyRate(new BigDecimal("100"));
        b.setTotalCharges(new BigDecimal("100"));
        return b;
    }

    @Test
    @DisplayName("Dashboard redirects when unauthenticated")
    void dashboardRedirectsWhenNotAuthed() throws Exception {
        mockMvc.perform(get("/property/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/property/login"));
    }

    @Test
    @DisplayName("Dashboard loads with statistics")
    void dashboardLoadsWithStats() throws Exception {
        when(roomRepository.count()).thenReturn(5L);
        when(roomRepository.countVacantRooms()).thenReturn(3L);
        when(roomRepository.countOccupiedRooms()).thenReturn(2L);
        when(bookingRepository.countActiveBookings()).thenReturn(4L);
        when(bookingRepository.countPendingPayments()).thenReturn(1L);
        when(bookingRepository.countOverduePayments()).thenReturn(1L);
        when(bookingRepository.getTotalOutstandingBalance()).thenReturn(new BigDecimal("250.00"));
        List<Booking> recent = List.of(sampleBooking(1L), sampleBooking(2L));
        when(bookingRepository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(recent);
        when(bookingRepository.findOverdueBookings()).thenReturn(Collections.emptyList());
        when(bookingRepository.findBookingsWithOutstandingBalance()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/property/dashboard").sessionAttr("propertyAuthenticated", true))
            .andExpect(status().isOk())
            .andExpect(view().name("property/dashboard"))
            .andExpect(model().attributeExists("totalRooms","vacantRooms","occupiedRooms","activeBookings","pendingPayments","overduePayments","totalOutstanding","recentBookings","overdueBookings","outstandingBookings"))
            .andExpect(model().attribute("totalRooms", 5L))
            .andExpect(model().attribute("vacantRooms", 3L))
            .andExpect(model().attribute("occupiedRooms", 2L))
            .andExpect(model().attribute("activeBookings", 4L))
            .andExpect(model().attribute("pendingPayments", 1L))
            .andExpect(model().attribute("overduePayments", 1L));
    }

    @Test
    @DisplayName("Bookings list redirects when unauthenticated")
    void bookingsListRedirectsWhenNotAuthed() throws Exception {
        mockMvc.perform(get("/property/bookings"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/property/login"));
    }

    @Test
    @DisplayName("Bookings list loads for authenticated user")
    void bookingsListLoads() throws Exception {
        when(bookingRepository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(sampleBooking(10L)));
        mockMvc.perform(get("/property/bookings").sessionAttr("propertyAuthenticated", true))
            .andExpect(status().isOk())
            .andExpect(view().name("property/bookings/list"))
            .andExpect(model().attributeExists("bookings"));
    }

    @Test
    @DisplayName("New booking form loads with available rooms and guests")
    void newBookingFormLoads() throws Exception {
        when(roomRepository.findByIsVacantTrueAndIsActiveTrueOrderByRoomNumberAsc()).thenReturn(List.of(new Room()));
        when(guestRepository.findByIsActiveTrueOrderByLastNameAscFirstNameAsc()).thenReturn(List.of(new Guest()));
        mockMvc.perform(get("/property/bookings/new").sessionAttr("propertyAuthenticated", true))
            .andExpect(status().isOk())
            .andExpect(view().name("property/bookings/form"))
            .andExpect(model().attributeExists("booking","availableRooms","activeGuests","paymentFrequencies","bookingStatuses"));
    }
}
