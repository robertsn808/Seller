package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.repository.ClientRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BatchImportService {
    private static final Logger logger = LoggerFactory.getLogger(BatchImportService.class);
    private static final int BATCH_SIZE = 500;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private ClientImportService clientImportService;
    
    private final Map<String, ImportProgress> importProgress = new ConcurrentHashMap<>();
    
    public static class ImportProgress {
        private int totalRecords;
        private int processedRecords;
        private int successCount;
        private int errorCount;
        private int skippedCount;
        private String status; // PROCESSING, COMPLETED, FAILED
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<String> recentErrors = new ArrayList<>();
        
        public ImportProgress(int totalRecords) {
            this.totalRecords = totalRecords;
            this.processedRecords = 0;
            this.status = "PROCESSING";
            this.startTime = LocalDateTime.now();
        }
        
        public synchronized void incrementSuccess() {
            successCount++;
            processedRecords++;
        }
        
        public synchronized void incrementError(String error) {
            errorCount++;
            processedRecords++;
            if (recentErrors.size() < 10) {
                recentErrors.add(error);
            }
        }
        
        public synchronized void incrementSkipped() {
            skippedCount++;
            processedRecords++;
        }
        
        public double getProgress() {
            return totalRecords == 0 ? 0 : (double) processedRecords / totalRecords * 100;
        }
        
        // Getters
        public int getTotalRecords() { return totalRecords; }
        public int getProcessedRecords() { return processedRecords; }
        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public int getSkippedCount() { return skippedCount; }
        public String getStatus() { return status; }
        public String getErrorMessage() { return errorMessage; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public List<String> getRecentErrors() { return recentErrors; }
        
        // Setters
        public void setStatus(String status) { this.status = status; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
    
    @Async
    public CompletableFuture<String> processLargeImport(MultipartFile file) throws IOException {
        String importId = UUID.randomUUID().toString();
        
        // Save file temporarily
        Path tempFile = Files.createTempFile("import_", ".xlsx");
        file.transferTo(tempFile.toFile());
        
        // Start processing in background
        CompletableFuture.runAsync(() -> {
            try {
                processFile(tempFile.toFile(), importId);
            } catch (Exception e) {
                logger.error("Error processing import {}: {}", importId, e.getMessage(), e);
                ImportProgress progress = importProgress.get(importId);
                if (progress != null) {
                    progress.setStatus("FAILED");
                    progress.setErrorMessage(e.getMessage());
                    progress.setEndTime(LocalDateTime.now());
                }
            } finally {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    logger.warn("Could not delete temporary file: {}", tempFile);
                }
            }
        });
        
        return CompletableFuture.completedFuture(importId);
    }
    
    private void processFile(File file, String importId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Get header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("No header row found in Excel file");
            }
            
            Map<String, Integer> columnMap = clientImportService.createColumnMap(headerRow);
            int totalRows = sheet.getLastRowNum();
            
            ImportProgress progress = new ImportProgress(totalRows);
            importProgress.put(importId, progress);
            
            List<Client> batch = new ArrayList<>(BATCH_SIZE);
            Set<String> existingEmails = new HashSet<>();
            
            // Process rows in batches
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    progress.incrementSkipped();
                    continue;
                }
                
                try {
                    Client client = clientImportService.createClientFromRow(row, columnMap);
                    if (client != null) {
                        String email = client.getEmail();
                        if (email == null || email.trim().isEmpty()) {
                            progress.incrementError("Row " + (i + 1) + ": Email is required");
                            continue;
                        }
                        
                        // Check for duplicates in current batch and database
                        if (existingEmails.contains(email.toLowerCase())) {
                            progress.incrementSkipped();
                            continue;
                        }
                        
                        Optional<Client> existingClient = clientRepository.findByEmail(email);
                        if (existingClient.isPresent()) {
                            progress.incrementSkipped();
                            continue;
                        }
                        
                        existingEmails.add(email.toLowerCase());
                        batch.add(client);
                        progress.incrementSuccess();
                        
                        // Save batch when it reaches the batch size
                        if (batch.size() >= BATCH_SIZE) {
                            clientRepository.saveAll(batch);
                            batch.clear();
                        }
                    }
                } catch (Exception e) {
                    progress.incrementError("Row " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            // Save any remaining clients in the last batch
            if (!batch.isEmpty()) {
                clientRepository.saveAll(batch);
            }
            
            progress.setStatus("COMPLETED");
            progress.setEndTime(LocalDateTime.now());
            
        } catch (Exception e) {
            ImportProgress progress = importProgress.get(importId);
            if (progress != null) {
                progress.setStatus("FAILED");
                progress.setErrorMessage(e.getMessage());
                progress.setEndTime(LocalDateTime.now());
            }
            throw e;
        }
    }
    
    public ImportProgress getImportProgress(String importId) {
        return importProgress.get(importId);
    }
    
    public void cleanupOldImports() {
        // Remove completed imports older than 24 hours
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        importProgress.entrySet().removeIf(entry -> {
            ImportProgress progress = entry.getValue();
            return progress.getEndTime() != null && progress.getEndTime().isBefore(cutoff);
        });
    }
}
