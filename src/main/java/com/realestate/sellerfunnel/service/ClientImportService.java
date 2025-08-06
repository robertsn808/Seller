package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.repository.ClientRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ClientImportService {

    @Autowired
    private ClientRepository clientRepository;

    public ImportResult importClientsFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Get header row to map columns
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.addError("No header row found in Excel file");
                return result;
            }
            
            Map<String, Integer> columnMap = createColumnMap(headerRow);
            
            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Client client = createClientFromRow(row, columnMap);
                    if (client != null) {
                        // Check for duplicate email
                        Optional<Client> existingClient = clientRepository.findByEmail(client.getEmail());
                        if (existingClient.isPresent()) {
                            result.addSkipped("Row " + (i + 1) + ": Email already exists - " + client.getEmail());
                        } else {
                            clientRepository.save(client);
                            result.addSuccess("Row " + (i + 1) + ": " + client.getFirstName() + " " + client.getLastName());
                        }
                    }
                } catch (Exception e) {
                    result.addError("Row " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        
        return result;
    }
    
    private Map<String, Integer> createColumnMap(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String headerValue = cell.getStringCellValue().trim().toLowerCase();
                columnMap.put(headerValue, i);
            }
        }
        
        return columnMap;
    }
    
    private Client createClientFromRow(Row row, Map<String, Integer> columnMap) {
        Client client = new Client();
        
        // Required fields
        String firstName = getCellValueAsString(row, columnMap.get("first name"));
        String lastName = getCellValueAsString(row, columnMap.get("last name"));
        String email = getCellValueAsString(row, columnMap.get("email"));
        
        if (firstName == null || lastName == null || email == null) {
            throw new IllegalArgumentException("First name, last name, and email are required");
        }
        
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEmail(email);
        
        // Optional fields
        client.setPhoneNumber(getCellValueAsString(row, columnMap.get("phone")));
        client.setClientType(getCellValueAsString(row, columnMap.get("client type")));
        client.setClientStatus(getCellValueAsString(row, columnMap.get("client status")));
        client.setLeadSource(getCellValueAsString(row, columnMap.get("lead source")));
        client.setCompanyName(getCellValueAsString(row, columnMap.get("company")));
        client.setJobTitle(getCellValueAsString(row, columnMap.get("job title")));
        client.setAddress(getCellValueAsString(row, columnMap.get("address")));
        client.setCity(getCellValueAsString(row, columnMap.get("city")));
        client.setState(getCellValueAsString(row, columnMap.get("state")));
        client.setZipCode(getCellValueAsString(row, columnMap.get("zip code")));
        client.setNotes(getCellValueAsString(row, columnMap.get("notes")));
        
        // Boolean fields
        String activeStr = getCellValueAsString(row, columnMap.get("active"));
        if (activeStr != null) {
            client.setIsActive("yes".equalsIgnoreCase(activeStr) || "true".equalsIgnoreCase(activeStr) || "1".equals(activeStr));
        }
        
        String emailOptedInStr = getCellValueAsString(row, columnMap.get("email opted in"));
        if (emailOptedInStr != null) {
            client.setEmailOptedIn("yes".equalsIgnoreCase(emailOptedInStr) || "true".equalsIgnoreCase(emailOptedInStr) || "1".equals(emailOptedInStr));
        }
        
        // Date fields
        String dateAddedStr = getCellValueAsString(row, columnMap.get("date added"));
        if (dateAddedStr != null && !dateAddedStr.trim().isEmpty()) {
            try {
                LocalDateTime dateAdded = parseDate(dateAddedStr);
                client.setCreatedAt(dateAdded);
            } catch (Exception e) {
                // Use current date if parsing fails
                client.setCreatedAt(LocalDateTime.now());
            }
        }
        
        // Contact counts
        String emailCountStr = getCellValueAsString(row, columnMap.get("email contact count"));
        if (emailCountStr != null && !emailCountStr.trim().isEmpty()) {
            try {
                client.setEmailContactCount(Integer.parseInt(emailCountStr.trim()));
            } catch (NumberFormatException e) {
                client.setEmailContactCount(0);
            }
        }
        
        String phoneCountStr = getCellValueAsString(row, columnMap.get("phone contact count"));
        if (phoneCountStr != null && !phoneCountStr.trim().isEmpty()) {
            try {
                client.setPhoneContactCount(Integer.parseInt(phoneCountStr.trim()));
            } catch (NumberFormatException e) {
                client.setPhoneContactCount(0);
            }
        }
        
        return client;
    }
    
    private String getCellValueAsString(Row row, Integer columnIndex) {
        if (columnIndex == null) return null;
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    
    private LocalDateTime parseDate(String dateStr) {
        // Try different date formats
        String[] formats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy"
        };
        
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                if (format.contains("HH:mm:ss")) {
                    return LocalDateTime.parse(dateStr, formatter);
                } else {
                    return LocalDateTime.parse(dateStr + " 00:00:00", 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            } catch (Exception e) {
                // Continue to next format
            }
        }
        
        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }
    
    public static class ImportResult {
        private List<String> successes = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        private List<String> skipped = new ArrayList<>();
        
        public void addSuccess(String message) {
            successes.add(message);
        }
        
        public void addError(String message) {
            errors.add(message);
        }
        
        public void addSkipped(String message) {
            skipped.add(message);
        }
        
        public List<String> getSuccesses() { return successes; }
        public List<String> getErrors() { return errors; }
        public List<String> getSkipped() { return skipped; }
        
        public int getTotalProcessed() {
            return successes.size() + errors.size() + skipped.size();
        }
        
        public int getSuccessCount() { return successes.size(); }
        public int getErrorCount() { return errors.size(); }
        public int getSkippedCount() { return skipped.size(); }
    }
}
