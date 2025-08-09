package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Booking;
import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByIsActiveTrueOrderByCreatedAtDesc();
    
    List<Booking> findByRoomAndIsActiveTrueOrderByCreatedAtDesc(Room room);
    
    List<Booking> findByGuestAndIsActiveTrueOrderByCreatedAtDesc(Guest guest);
    
    List<Booking> findByBookingStatusAndIsActiveTrue(String bookingStatus);
    
    List<Booking> findByPaymentStatusAndIsActiveTrue(String paymentStatus);
    
    List<Booking> findByPaymentStatusInAndIsActiveTrue(List<String> paymentStatuses);
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.bookingStatus = 'ACTIVE' ORDER BY b.checkInDate ASC")
    List<Booking> findActiveBookings();
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.bookingStatus = 'ACTIVE' AND b.room = :room")
    Optional<Booking> findActiveBookingByRoom(@Param("room") Room room);
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.bookingStatus = 'ACTIVE' AND b.guest = :guest")
    List<Booking> findActiveBookingsByGuest(@Param("guest") Guest guest);
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.checkInDate >= :startDate AND b.checkInDate <= :endDate")
    List<Booking> findByCheckInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.checkOutDate >= :startDate AND b.checkOutDate <= :endDate")
    List<Booking> findByCheckOutDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.currentBalance > 0 ORDER BY b.currentBalance DESC")
    List<Booking> findBookingsWithOutstandingBalance();
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.paymentStatus = 'OVERDUE' ORDER BY b.currentBalance DESC")
    List<Booking> findOverdueBookings();
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.paymentFrequency = :frequency")
    List<Booking> findByPaymentFrequency(@Param("frequency") String frequency);
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND (b.guest.firstName LIKE %:searchTerm% OR b.guest.lastName LIKE %:searchTerm% OR b.guest.email LIKE %:searchTerm% OR b.room.roomNumber LIKE %:searchTerm%)")
    List<Booking> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.isActive = true AND b.bookingStatus = 'ACTIVE'")
    Long countActiveBookings();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.isActive = true AND b.paymentStatus = 'PENDING'")
    Long countPendingPayments();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.isActive = true AND b.paymentStatus = 'OVERDUE'")
    Long countOverduePayments();
    
    @Query("SELECT SUM(b.currentBalance) FROM Booking b WHERE b.isActive = true AND b.currentBalance > 0")
    BigDecimal getTotalOutstandingBalance();
    
    @Query("SELECT SUM(b.totalCharges) FROM Booking b WHERE b.isActive = true AND b.checkInDate >= :startDate AND b.checkInDate <= :endDate")
    BigDecimal getTotalChargesForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(b.totalPayments) FROM Booking b WHERE b.isActive = true AND b.checkInDate >= :startDate AND b.checkInDate <= :endDate")
    BigDecimal getTotalPaymentsForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b.paymentFrequency, COUNT(b) FROM Booking b WHERE b.isActive = true GROUP BY b.paymentFrequency")
    List<Object[]> countByPaymentFrequency();
    
    @Query("SELECT b.bookingStatus, COUNT(b) FROM Booking b WHERE b.isActive = true GROUP BY b.bookingStatus")
    List<Object[]> countByBookingStatus();
    
    @Query("SELECT b.paymentStatus, COUNT(b) FROM Booking b WHERE b.isActive = true GROUP BY b.paymentStatus")
    List<Object[]> countByPaymentStatus();
    
    @Query("SELECT b.room.roomType, COUNT(b) FROM Booking b WHERE b.isActive = true AND b.bookingStatus = 'ACTIVE' GROUP BY b.room.roomType")
    List<Object[]> countActiveBookingsByRoomType();
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.expectedCheckOutDate <= :date AND b.bookingStatus = 'ACTIVE'")
    List<Booking> findBookingsDueForCheckout(@Param("date") LocalDateTime date);
    
    @Query("SELECT b FROM Booking b WHERE b.isActive = true AND b.checkInDate <= :date AND b.checkOutDate IS NULL AND b.bookingStatus = 'ACTIVE'")
    List<Booking> findExtendedStayBookings(@Param("date") LocalDateTime date);
}
