package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    
    List<Guest> findByIsActiveTrueOrderByLastNameAscFirstNameAsc();
    
    Optional<Guest> findByEmail(String email);
    
    Optional<Guest> findByPhoneNumber(String phoneNumber);
    
    Optional<Guest> findByIdNumber(String idNumber);
    
    @Query("SELECT g FROM Guest g WHERE g.isActive = true AND (g.firstName LIKE %:searchTerm% OR g.lastName LIKE %:searchTerm% OR g.email LIKE %:searchTerm% OR g.phoneNumber LIKE %:searchTerm%)")
    List<Guest> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT g FROM Guest g WHERE g.isActive = true AND (g.firstName LIKE %:firstName% AND g.lastName LIKE %:lastName%)")
    List<Guest> findByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);
    
    @Query("SELECT g FROM Guest g WHERE g.isActive = true AND g.vehicleLicensePlate LIKE %:licensePlate%")
    List<Guest> findByVehicleLicensePlate(@Param("licensePlate") String licensePlate);
    
    @Query("SELECT COUNT(g) FROM Guest g WHERE g.isActive = true")
    Long countActiveGuests();
    
    @Query("SELECT g.idType, COUNT(g) FROM Guest g WHERE g.isActive = true AND g.idType IS NOT NULL GROUP BY g.idType")
    List<Object[]> countByIdType();
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, Long id);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
    
    boolean existsByIdNumber(String idNumber);
    
    boolean existsByIdNumberAndIdNot(String idNumber, Long id);
}
