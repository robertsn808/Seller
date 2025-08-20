package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.PropertyPhoto;
import com.realestate.sellerfunnel.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PropertyPhotoRepository extends JpaRepository<PropertyPhoto, Long> {
    List<PropertyPhoto> findBySellerOrderByDisplayOrderAsc(Seller seller);
    void deleteBySellerAndFileName(Seller seller, String fileName);
}