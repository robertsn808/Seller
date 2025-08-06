package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByIsActiveTrueOrderByRoomNumberAsc();
    
    List<Room> findByIsVacantTrueAndIsActiveTrueOrderByRoomNumberAsc();
    
    List<Room> findByIsVacantFalseAndIsActiveTrueOrderByRoomNumberAsc();
    
    Optional<Room> findByRoomNumber(String roomNumber);
    
    List<Room> findByRoomTypeAndIsActiveTrue(String roomType);
    
    List<Room> findByGateKeyAssignedTrueAndIsActiveTrue();
    
    @Query("SELECT r FROM Room r WHERE r.isActive = true AND (r.roomNumber LIKE %:searchTerm% OR r.roomName LIKE %:searchTerm% OR r.roomType LIKE %:searchTerm%)")
    List<Room> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(r) FROM Room r WHERE r.isVacant = true AND r.isActive = true")
    Long countVacantRooms();
    
    @Query("SELECT COUNT(r) FROM Room r WHERE r.isVacant = false AND r.isActive = true")
    Long countOccupiedRooms();
    
    @Query("SELECT r.roomType, COUNT(r) FROM Room r WHERE r.isActive = true GROUP BY r.roomType")
    List<Object[]> countByRoomType();
    
    @Query("SELECT r.gateKeyAssigned, COUNT(r) FROM Room r WHERE r.isActive = true GROUP BY r.gateKeyAssigned")
    List<Object[]> countByGateKeyStatus();
    
    boolean existsByRoomNumber(String roomNumber);
    
    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);
}
