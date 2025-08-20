package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    List<Buyer> findAllByOrderByCreatedAtDesc();
    List<Buyer> findTop5ByOrderByCreatedAtDesc();
}