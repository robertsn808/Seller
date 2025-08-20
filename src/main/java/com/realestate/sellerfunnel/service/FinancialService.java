package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FinancialService {

    @Autowired
    private RoomRepository roomRepository;

    public BigDecimal calculateTotalHoldings() {
        return roomRepository.findAll().stream()
                .map(Room::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
