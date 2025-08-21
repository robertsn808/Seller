package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Room;
import com.realestate.sellerfunnel.model.Transaction;
import com.realestate.sellerfunnel.repository.RoomRepository;
import com.realestate.sellerfunnel.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Transactional
    public Transaction addTransaction(Long roomId, String description, BigDecimal amount, String paidBy, String collectedBy) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        Transaction transaction = new Transaction(room, description, amount, paidBy);
        transaction.setCollectedBy(collectedBy);
        room.addTransaction(transaction);

        transactionRepository.save(transaction);
        roomRepository.save(room);

        return transaction;
    }
}