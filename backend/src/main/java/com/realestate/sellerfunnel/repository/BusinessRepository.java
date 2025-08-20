package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, String> {
    
    // Find businesses by type
    List<Business> findByType(String type);
    
    // Find business by name
    Optional<Business> findByName(String name);
    
    // Find businesses by type ordered by creation date
    List<Business> findByTypeOrderByCreatedAtDesc(String type);
    
    // Find all businesses ordered by creation date
    List<Business> findAllByOrderByCreatedAtDesc();
}