package com.realestate.sellerfunnel.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.enabled=false")
@ActiveProfiles("test")
class PropertyManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private RoomRepository roomRepository;
    @MockBean private GuestRepository guestRepository;
    @MockBean private BookingRepository bookingRepository;
    @MockBean private PaymentRepository paymentRepository;
    @MockBean private PaymentService paymentService;

    @Test
    @DisplayName("Unauthenticated access to /property/rooms redirects to login")
    void roomsListRedirectsWhenNotAuthed() throws Exception {
        mockMvc.perform(get("/property/rooms"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/property/login"));
    }

    @Test
    @DisplayName("Authenticated access lists rooms")
    void roomsListShowsWhenAuthed() throws Exception {
        Room r = new Room();
        r.setId(1L); r.setRoomNumber("101"); r.setRoomType("Single"); r.setBaseRate(new BigDecimal("100"));
        when(roomRepository.findByIsActiveTrueOrderByRoomNumberAsc()).thenReturn(List.of(r));

        mockMvc.perform(get("/property/rooms").sessionAttr("propertyAuthenticated", true))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("rooms"))
            .andExpect(view().name("property/rooms/list"));
    }

    @Test
    @DisplayName("New room form loads for authenticated user")
    void newRoomFormLoads() throws Exception {
        mockMvc.perform(get("/property/rooms/new").sessionAttr("propertyAuthenticated", true))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("room"))
            .andExpect(view().name("property/rooms/form"));
    }

    @Test
    @DisplayName("Create room success path")
    void createRoomSuccess() throws Exception {
        when(roomRepository.existsByRoomNumber("102")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> { Room saved = inv.getArgument(0); saved.setId(2L); return saved;});

        MockHttpServletRequestBuilder req = post("/property/rooms")
            .sessionAttr("propertyAuthenticated", true)
            .param("roomNumber", "102")
            .param("roomType", "Single")
            .param("baseRate", "120");

        mockMvc.perform(req)
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/property/rooms"));
    }

    @Test
    @DisplayName("Duplicate room number validation")
    void createRoomDuplicateFails() throws Exception {
        when(roomRepository.existsByRoomNumber("103")).thenReturn(true);

        mockMvc.perform(post("/property/rooms")
                .sessionAttr("propertyAuthenticated", true)
                .param("roomNumber", "103")
                .param("roomType", "Single")
                .param("baseRate", "150"))
            .andExpect(status().isOk())
            .andExpect(view().name("property/rooms/form"));
    }
}
