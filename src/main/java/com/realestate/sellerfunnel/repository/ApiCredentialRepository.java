package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.ApiCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiCredentialRepository extends JpaRepository<ApiCredential, Long> {
    
    Optional<ApiCredential> findByPlatformAndIsActiveTrue(String platform);
    
    Optional<ApiCredential> findByPlatform(String platform);
    
    @Query("SELECT ac FROM ApiCredential ac WHERE ac.platform = :platform AND ac.isActive = true AND (ac.expiresAt IS NULL OR ac.expiresAt > CURRENT_TIMESTAMP)")
    Optional<ApiCredential> findValidCredentialByPlatform(@Param("platform") String platform);
    
    @Query("SELECT COUNT(ac) > 0 FROM ApiCredential ac WHERE ac.platform = :platform AND ac.isActive = true AND (ac.expiresAt IS NULL OR ac.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasValidCredentials(@Param("platform") String platform);
}