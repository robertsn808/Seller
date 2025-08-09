package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.model.Guest;
import com.realestate.sellerfunnel.model.Payment;
import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.repository.BookingRepository;
import com.realestate.sellerfunnel.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UniversalPaymentProtocolService uppService;

    @InjectMocks
    private PaymentService paymentService;

    private Booking booking;
    private Room room;
    private Guest guest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        room = new Room();
        room.setRoomNumber("101");
        guest = new Guest();
        guest.setFirstName("John");
        guest.setLastName("Doe");
        guest.setEmail("john@example.com");
        booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setCheckInDate(LocalDateTime.now().minusDays(1));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
    }

    @Test
    @DisplayName("Process traditional payment successfully")
    void processTraditionalPaymentSuccess() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(10L);
            return p;
        });

        PaymentService.PaymentResult result = paymentService.processPayment(1L, new BigDecimal("50.00"), "CASH", null, null, "john@example.com");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayment()).isNotNull();
        assertThat(result.getPayment().getPaymentStatus()).isEqualTo("COMPLETED");
        // Only one save for payment (booking update does not re-save payment)
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Process UPP payment success path")
    void processUppPaymentSuccess() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(20L);
            return p;
        });

        UniversalPaymentProtocolService.UppPaymentResult uppResult = new UniversalPaymentProtocolService.UppPaymentResult();
        uppResult.setSuccess(true);
        uppResult.setTransactionId("tx_123");
        uppResult.setPaymentIntentId("pi_123");
        uppResult.setProcessedAt(LocalDateTime.now());
        uppResult.setRiskScore(42);
        when(uppService.processPayment(any(Payment.class), anyString(), anyString())).thenReturn(uppResult);

        PaymentService.PaymentResult result = paymentService.processPayment(1L, new BigDecimal("75.00"), "UPP_DEVICE", "smartphone", "dev-1", "john@example.com");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayment().getPaymentStatus()).isEqualTo("COMPLETED");
        assertThat(result.getPayment().getUppTransactionId()).isEqualTo("tx_123");
        verify(uppService).processPayment(any(Payment.class), eq("smartphone"), eq("dev-1"));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Process UPP payment failure path")
    void processUppPaymentFailure() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(30L);
            return p;
        });

        UniversalPaymentProtocolService.UppPaymentResult uppResult = new UniversalPaymentProtocolService.UppPaymentResult();
        uppResult.setSuccess(false);
        uppResult.setErrorMessage("Device offline");
        uppResult.setProcessedAt(LocalDateTime.now());
        when(uppService.processPayment(any(Payment.class), anyString(), anyString())).thenReturn(uppResult);

        PaymentService.PaymentResult result = paymentService.processPayment(1L, new BigDecimal("60.00"), "UPP_DEVICE", "smart_tv", "dev-2", "john@example.com");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getPayment().getPaymentStatus()).isEqualTo("FAILED");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refund completed payment")
    void refundPaymentSuccess() {
        Payment original = new Payment(booking, new BigDecimal("100.00"), "CASH");
        original.setId(40L);
        original.setPaymentStatus("COMPLETED");
        when(paymentRepository.findById(40L)).thenReturn(Optional.of(original));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(41L);
            return p;
        });

        PaymentService.PaymentResult result = paymentService.refundPayment(40L, new BigDecimal("25.00"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPayment().getAmount()).isEqualTo(new BigDecimal("-25.00"));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Reject refund exceeding original amount")
    void refundTooLarge() {
        Payment original = new Payment(booking, new BigDecimal("100.00"), "CASH");
        original.setId(50L);
        original.setPaymentStatus("COMPLETED");
        when(paymentRepository.findById(50L)).thenReturn(Optional.of(original));

        PaymentService.PaymentResult result = paymentService.refundPayment(50L, new BigDecimal("150.00"));

        assertThat(result.isSuccess()).isFalse();
        verify(paymentRepository, never()).save(argThat(p -> p.getId() == null));
    }

    @Test
    @DisplayName("Get payment statistics aggregates repository data")
    void getPaymentStatistics() {
        when(paymentRepository.countByPaymentStatusAndIsActiveTrue("PENDING")).thenReturn(1L);
        when(paymentRepository.countByPaymentStatusAndIsActiveTrue("COMPLETED")).thenReturn(2L);
        when(paymentRepository.countByPaymentStatusAndIsActiveTrue("FAILED")).thenReturn(3L);
        when(paymentRepository.countByPaymentMethodAndIsActiveTrue("UPP_DEVICE")).thenReturn(4L);
        when(paymentRepository.countByPaymentMethodAndIsActiveTrue("CARD")).thenReturn(5L);
        when(paymentRepository.countByPaymentMethodAndIsActiveTrue("CASH")).thenReturn(6L);
        when(paymentRepository.countByPaymentMethodAndIsActiveTrue("TRANSFER")).thenReturn(7L);
        when(paymentRepository.sumCompletedUppPayments()).thenReturn(new BigDecimal("123.45"));
        when(paymentRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of());

        PaymentService.PaymentStatistics stats = paymentService.getPaymentStatistics();

        assertThat(stats.getPendingCount()).isEqualTo(1L);
        assertThat(stats.getCompletedCount()).isEqualTo(2L);
        assertThat(stats.getFailedCount()).isEqualTo(3L);
        assertThat(stats.getUppCount()).isEqualTo(4L);
        assertThat(stats.getCardCount()).isEqualTo(5L);
        assertThat(stats.getCashCount()).isEqualTo(6L);
        assertThat(stats.getTransferCount()).isEqualTo(7L);
        assertThat(stats.getTotalUppPayments()).isEqualTo(new BigDecimal("123.45"));
    }
}
