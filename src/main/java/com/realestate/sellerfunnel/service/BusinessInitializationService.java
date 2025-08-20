package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Business;
import com.realestate.sellerfunnel.repository.BusinessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessInitializationService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BusinessInitializationService.class);

    @Autowired
    private BusinessRepository businessRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        initializeDefaultBusinesses();
    }

    private void initializeDefaultBusinesses() {
        // Create default real estate business if it doesn't exist
        if (!businessRepository.existsById("real-estate")) {
            Business realEstate = new Business(
                "real-estate",
                "Real Estate Connect",
                "real-estate",
                "Real estate buyer and seller funnel platform"
            );
            businessRepository.save(realEstate);
            logger.info("Created default real estate business");
        }

        // Create sample restaurant business for demonstration
        if (!businessRepository.existsById("sample-restaurant")) {
            Business restaurant = new Business(
                "sample-restaurant",
                "Sample Restaurant",
                "restaurant",
                "Sample restaurant business for multi-tenant demonstration"
            );
            businessRepository.save(restaurant);
            logger.info("Created sample restaurant business");
        }

        logger.info("Business initialization completed");
    }
}