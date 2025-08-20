package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Buyer;
import com.realestate.sellerfunnel.model.Seller;
import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.repository.BuyerRepository;
import com.realestate.sellerfunnel.repository.SellerRepository;
import com.realestate.sellerfunnel.repository.ClientRepository;
import com.realestate.sellerfunnel.service.FileUploadService;
import com.realestate.sellerfunnel.service.PersonalFollowUpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class RestHomeController {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private PersonalFollowUpService personalFollowUpService;

    @GetMapping("/buyers")
    public ResponseEntity<List<Buyer>> getAllBuyers() {
        List<Buyer> buyers = buyerRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(buyers);
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<Seller>> getAllSellers() {
        List<Seller> sellers = sellerRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(sellers);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = clientRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return ResponseEntity.ok(clients);
    }

    @PostMapping("/buyer")
    public ResponseEntity<Map<String, Object>> submitBuyer(@Valid @RequestBody Buyer buyer) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Save the buyer
            Buyer savedBuyer = buyerRepository.save(buyer);
            
            // Create or update client record
            createOrUpdateClientFromBuyer(savedBuyer);
            
            // Send personal follow-up email and SMS
            personalFollowUpService.sendBuyerFollowUp(savedBuyer);
            
            response.put("success", true);
            response.put("message", "Thank you! Your buyer information has been submitted successfully.");
            response.put("buyer", savedBuyer);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting buyer information: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/seller")
    public ResponseEntity<Map<String, Object>> submitSeller(
            @Valid @RequestPart("seller") Seller seller,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Save seller first
            Seller savedSeller = sellerRepository.save(seller);
            
            // Create or update client record
            createOrUpdateClientFromSeller(savedSeller);
            
            // Send personal follow-up email and SMS
            personalFollowUpService.sendSellerFollowUp(savedSeller);
            
            // Then save photos if provided
            if (photos != null && !photos.isEmpty()) {
                fileUploadService.savePropertyPhotos(photos, savedSeller);
            }
            
            response.put("success", true);
            response.put("message", "Thank you! Your property information has been submitted successfully.");
            response.put("seller", savedSeller);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "There was an error uploading your photos. Please try again.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting seller information: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/photos/{fileName}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String fileName) {
        try {
            byte[] photoData = fileUploadService.getPhotoData(fileName);
            if (photoData != null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                        .body(photoData);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get counts
        long buyerCount = buyerRepository.count();
        long sellerCount = sellerRepository.count();
        long clientCount = clientRepository.countByIsActiveTrue();
        
        // Get recent activity
        List<Buyer> recentBuyers = buyerRepository.findTop5ByOrderByCreatedAtDesc();
        List<Seller> recentSellers = sellerRepository.findTop5ByOrderByCreatedAtDesc();
        
        stats.put("buyerCount", buyerCount);
        stats.put("sellerCount", sellerCount);
        stats.put("clientCount", clientCount);
        stats.put("recentBuyers", recentBuyers);
        stats.put("recentSellers", recentSellers);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Creates or updates a client record from buyer form submission
     */
    private void createOrUpdateClientFromBuyer(Buyer buyer) {
        // Check if client already exists by email
        clientRepository.findByEmail(buyer.getEmail()).ifPresentOrElse(
            existingClient -> {
                // Update existing client with buyer information
                updateClientWithBuyerInfo(existingClient, buyer);
                clientRepository.save(existingClient);
            },
            () -> {
                // Create new client from buyer information
                Client newClient = createClientFromBuyer(buyer);
                clientRepository.save(newClient);
            }
        );
    }
    
    /**
     * Creates or updates a client record from seller form submission
     */
    private void createOrUpdateClientFromSeller(Seller seller) {
        // Check if client already exists by email
        clientRepository.findByEmail(seller.getEmail()).ifPresentOrElse(
            existingClient -> {
                // Update existing client with seller information
                updateClientWithSellerInfo(existingClient, seller);
                clientRepository.save(existingClient);
            },
            () -> {
                // Create new client from seller information
                Client newClient = createClientFromSeller(seller);
                clientRepository.save(newClient);
            }
        );
    }
    
    /**
     * Creates a new client from buyer information
     */
    private Client createClientFromBuyer(Buyer buyer) {
        Client client = new Client();
        
        // Parse name into first and last name
        String[] nameParts = buyer.getName().split(" ", 2);
        client.setFirstName(nameParts[0]);
        client.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        // Set basic contact information
        client.setEmail(buyer.getEmail());
        client.setPhoneNumber(buyer.getPhone());
        
        // Set client type and status
        client.setClientType("BUYER");
        client.setClientStatus("LEAD");
        
        // Set lead source
        client.setLeadSource("Website Form - Buyer");
        
        // Set location information if available
        if (buyer.getPreferredAreas() != null && !buyer.getPreferredAreas().trim().isEmpty()) {
            client.setCity(buyer.getPreferredAreas());
        }
        
        // Set notes with buyer-specific information
        StringBuilder notes = new StringBuilder();
        notes.append("Buyer Information:\n");
        notes.append("- Budget Range: $").append(buyer.getMinBudget()).append(" - $").append(buyer.getMaxBudget()).append("\n");
        
        if (buyer.getPreferredAreas() != null && !buyer.getPreferredAreas().trim().isEmpty()) {
            notes.append("- Preferred Areas: ").append(buyer.getPreferredAreas()).append("\n");
        }
        
        if (buyer.getMinBedrooms() != null) {
            notes.append("- Min Bedrooms: ").append(buyer.getMinBedrooms()).append("\n");
        }
        
        if (buyer.getMinBathrooms() != null) {
            notes.append("- Min Bathrooms: ").append(buyer.getMinBathrooms()).append("\n");
        }
        
        if (buyer.getPropertyType() != null && !buyer.getPropertyType().trim().isEmpty()) {
            notes.append("- Property Type: ").append(buyer.getPropertyType()).append("\n");
        }
        
        if (buyer.getPurchasePurpose() != null && !buyer.getPurchasePurpose().trim().isEmpty()) {
            notes.append("- Purchase Purpose: ").append(buyer.getPurchasePurpose()).append("\n");
        }
        
        if (buyer.getTimeframe() != null && !buyer.getTimeframe().trim().isEmpty()) {
            notes.append("- Timeframe: ").append(buyer.getTimeframe()).append("\n");
        }
        
        if (buyer.getNeedsFinancing() != null && buyer.getNeedsFinancing()) {
            notes.append("- Needs Financing: Yes\n");
        }
        
        if (buyer.getOpenToCreativeFinancing() != null && buyer.getOpenToCreativeFinancing()) {
            notes.append("- Open to Creative Financing: Yes\n");
        }
        
        if (buyer.getAdditionalNotes() != null && !buyer.getAdditionalNotes().trim().isEmpty()) {
            notes.append("- Additional Notes: ").append(buyer.getAdditionalNotes()).append("\n");
        }
        
        client.setNotes(notes.toString());
        
        // Set email opt-in to true for form submissions
        client.setEmailOptedIn(true);
        client.setIsActive(true);
        
        return client;
    }
    
    /**
     * Creates a new client from seller information
     */
    private Client createClientFromSeller(Seller seller) {
        Client client = new Client();
        
        // Parse name into first and last name
        String[] nameParts = seller.getName().split(" ", 2);
        client.setFirstName(nameParts[0]);
        client.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        // Set basic contact information
        client.setEmail(seller.getEmail());
        client.setPhoneNumber(seller.getPhone());
        
        // Set client type and status
        client.setClientType("SELLER");
        client.setClientStatus("LEAD");
        
        // Set lead source
        client.setLeadSource("Website Form - Seller");
        
        // Set location information
        client.setCity(seller.getCity());
        client.setState(seller.getState());
        
        // Set notes with seller-specific information
        StringBuilder notes = new StringBuilder();
        notes.append("Seller Information:\n");
        notes.append("- Property Address: ").append(seller.getPropertyAddress()).append("\n");
        
        if (seller.getAskingPrice() != null) {
            notes.append("- Asking Price: $").append(seller.getAskingPrice()).append("\n");
        }
        
        if (seller.getPropertyType() != null && !seller.getPropertyType().trim().isEmpty()) {
            notes.append("- Property Type: ").append(seller.getPropertyType()).append("\n");
        }
        
        if (seller.getBedrooms() != null) {
            notes.append("- Bedrooms: ").append(seller.getBedrooms()).append("\n");
        }
        
        if (seller.getBathrooms() != null) {
            notes.append("- Bathrooms: ").append(seller.getBathrooms()).append("\n");
        }
        
        if (seller.getSquareFootage() != null) {
            notes.append("- Square Footage: ").append(seller.getSquareFootage()).append("\n");
        }
        
        if (seller.getYearBuilt() != null) {
            notes.append("- Year Built: ").append(seller.getYearBuilt()).append("\n");
        }
        
        if (seller.getCondition() != null && !seller.getCondition().trim().isEmpty()) {
            notes.append("- Condition: ").append(seller.getCondition()).append("\n");
        }
        
        if (seller.getSellingReason() != null && !seller.getSellingReason().trim().isEmpty()) {
            notes.append("- Selling Reason: ").append(seller.getSellingReason()).append("\n");
        }
        
        if (seller.getTimeframe() != null && !seller.getTimeframe().trim().isEmpty()) {
            notes.append("- Timeframe: ").append(seller.getTimeframe()).append("\n");
        }
        
        if (seller.getOwnerFinancing() != null && seller.getOwnerFinancing()) {
            notes.append("- Owner Financing: Yes\n");
        }
        
        if (seller.getOpenToCreativeFinancing() != null && seller.getOpenToCreativeFinancing()) {
            notes.append("- Open to Creative Financing: Yes\n");
        }
        
        if (seller.getNeedsRepairs() != null && seller.getNeedsRepairs()) {
            notes.append("- Needs Repairs: Yes\n");
        }
        
        if (seller.getRepairDetails() != null && !seller.getRepairDetails().trim().isEmpty()) {
            notes.append("- Repair Details: ").append(seller.getRepairDetails()).append("\n");
        }
        
        if (seller.getAdditionalNotes() != null && !seller.getAdditionalNotes().trim().isEmpty()) {
            notes.append("- Additional Notes: ").append(seller.getAdditionalNotes()).append("\n");
        }
        
        client.setNotes(notes.toString());
        
        // Set email opt-in to true for form submissions
        client.setEmailOptedIn(true);
        client.setIsActive(true);
        
        return client;
    }
    
    /**
     * Updates an existing client with buyer information
     */
    private void updateClientWithBuyerInfo(Client client, Buyer buyer) {
        // Update name if not already set
        if (client.getFirstName() == null || client.getFirstName().trim().isEmpty()) {
            String[] nameParts = buyer.getName().split(" ", 2);
            client.setFirstName(nameParts[0]);
            client.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        }
        
        // Update phone if not already set
        if (client.getPhoneNumber() == null || client.getPhoneNumber().trim().isEmpty()) {
            client.setPhoneNumber(buyer.getPhone());
        }
        
        // Update client type if not already set or if it's different
        if (client.getClientType() == null || client.getClientType().trim().isEmpty()) {
            client.setClientType("BUYER");
        } else if (!client.getClientType().equals("BUYER")) {
            // If client was previously a seller, update to reflect both
            client.setClientType("BUYER_SELLER");
        }
        
        // Update status to LEAD if currently SUSPECT
        if ("SUSPECT".equals(client.getClientStatus())) {
            client.setClientStatus("LEAD");
        }
        
        // Update location if not already set
        if ((client.getCity() == null || client.getCity().trim().isEmpty()) && 
            buyer.getPreferredAreas() != null && !buyer.getPreferredAreas().trim().isEmpty()) {
            client.setCity(buyer.getPreferredAreas());
        }
        
        // Append buyer information to existing notes
        StringBuilder updatedNotes = new StringBuilder();
        if (client.getNotes() != null && !client.getNotes().trim().isEmpty()) {
            updatedNotes.append(client.getNotes()).append("\n\n");
        }
        
        updatedNotes.append("Additional Buyer Information (Updated ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))).append("):\n");
        updatedNotes.append("- Budget Range: $").append(buyer.getMinBudget()).append(" - $").append(buyer.getMaxBudget()).append("\n");
        
        if (buyer.getPreferredAreas() != null && !buyer.getPreferredAreas().trim().isEmpty()) {
            updatedNotes.append("- Preferred Areas: ").append(buyer.getPreferredAreas()).append("\n");
        }
        
        if (buyer.getMinBedrooms() != null) {
            updatedNotes.append("- Min Bedrooms: ").append(buyer.getMinBedrooms()).append("\n");
        }
        
        if (buyer.getMinBathrooms() != null) {
            updatedNotes.append("- Min Bathrooms: ").append(buyer.getMinBathrooms()).append("\n");
        }
        
        if (buyer.getPropertyType() != null && !buyer.getPropertyType().trim().isEmpty()) {
            updatedNotes.append("- Property Type: ").append(buyer.getPropertyType()).append("\n");
        }
        
        if (buyer.getPurchasePurpose() != null && !buyer.getPurchasePurpose().trim().isEmpty()) {
            updatedNotes.append("- Purchase Purpose: ").append(buyer.getPurchasePurpose()).append("\n");
        }
        
        if (buyer.getTimeframe() != null && !buyer.getTimeframe().trim().isEmpty()) {
            updatedNotes.append("- Timeframe: ").append(buyer.getTimeframe()).append("\n");
        }
        
        if (buyer.getNeedsFinancing() != null && buyer.getNeedsFinancing()) {
            updatedNotes.append("- Needs Financing: Yes\n");
        }
        
        if (buyer.getOpenToCreativeFinancing() != null && buyer.getOpenToCreativeFinancing()) {
            updatedNotes.append("- Open to Creative Financing: Yes\n");
        }
        
        if (buyer.getAdditionalNotes() != null && !buyer.getAdditionalNotes().trim().isEmpty()) {
            updatedNotes.append("- Additional Notes: ").append(buyer.getAdditionalNotes()).append("\n");
        }
        
        client.setNotes(updatedNotes.toString());
        
        // Ensure client is active and opted in
        client.setIsActive(true);
        client.setEmailOptedIn(true);
    }
    
    /**
     * Updates an existing client with seller information
     */
    private void updateClientWithSellerInfo(Client client, Seller seller) {
        // Update name if not already set
        if (client.getFirstName() == null || client.getFirstName().trim().isEmpty()) {
            String[] nameParts = seller.getName().split(" ", 2);
            client.setFirstName(nameParts[0]);
            client.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        }
        
        // Update phone if not already set
        if (client.getPhoneNumber() == null || client.getPhoneNumber().trim().isEmpty()) {
            client.setPhoneNumber(seller.getPhone());
        }
        
        // Update client type if not already set or if it's different
        if (client.getClientType() == null || client.getClientType().trim().isEmpty()) {
            client.setClientType("SELLER");
        } else if (!client.getClientType().equals("SELLER")) {
            // If client was previously a buyer, update to reflect both
            client.setClientType("BUYER_SELLER");
        }
        
        // Update status to LEAD if currently SUSPECT
        if ("SUSPECT".equals(client.getClientStatus())) {
            client.setClientStatus("LEAD");
        }
        
        // Update location information if not already set
        if (client.getCity() == null || client.getCity().trim().isEmpty()) {
            client.setCity(seller.getCity());
        }
        if (client.getState() == null || client.getState().trim().isEmpty()) {
            client.setState(seller.getState());
        }
        
        // Append seller information to existing notes
        StringBuilder updatedNotes = new StringBuilder();
        if (client.getNotes() != null && !client.getNotes().trim().isEmpty()) {
            updatedNotes.append(client.getNotes()).append("\n\n");
        }
        
        updatedNotes.append("Additional Seller Information (Updated ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))).append("):\n");
        updatedNotes.append("- Property Address: ").append(seller.getPropertyAddress()).append("\n");
        
        if (seller.getAskingPrice() != null) {
            updatedNotes.append("- Asking Price: $").append(seller.getAskingPrice()).append("\n");
        }
        
        if (seller.getPropertyType() != null && !seller.getPropertyType().trim().isEmpty()) {
            updatedNotes.append("- Property Type: ").append(seller.getPropertyType()).append("\n");
        }
        
        if (seller.getBedrooms() != null) {
            updatedNotes.append("- Bedrooms: ").append(seller.getBedrooms()).append("\n");
        }
        
        if (seller.getBathrooms() != null) {
            updatedNotes.append("- Bathrooms: ").append(seller.getBathrooms()).append("\n");
        }
        
        if (seller.getSquareFootage() != null) {
            updatedNotes.append("- Square Footage: ").append(seller.getSquareFootage()).append("\n");
        }
        
        if (seller.getYearBuilt() != null) {
            updatedNotes.append("- Year Built: ").append(seller.getYearBuilt()).append("\n");
        }
        
        if (seller.getCondition() != null && !seller.getCondition().trim().isEmpty()) {
            updatedNotes.append("- Condition: ").append(seller.getCondition()).append("\n");
        }
        
        if (seller.getSellingReason() != null && !seller.getSellingReason().trim().isEmpty()) {
            updatedNotes.append("- Selling Reason: ").append(seller.getSellingReason()).append("\n");
        }
        
        if (seller.getTimeframe() != null && !seller.getTimeframe().trim().isEmpty()) {
            updatedNotes.append("- Timeframe: ").append(seller.getTimeframe()).append("\n");
        }
        
        if (seller.getOwnerFinancing() != null && seller.getOwnerFinancing()) {
            updatedNotes.append("- Owner Financing: Yes\n");
        }
        
        if (seller.getOpenToCreativeFinancing() != null && seller.getOpenToCreativeFinancing()) {
            updatedNotes.append("- Open to Creative Financing: Yes\n");
        }
        
        if (seller.getNeedsRepairs() != null && seller.getNeedsRepairs()) {
            updatedNotes.append("- Needs Repairs: Yes\n");
        }
        
        if (seller.getRepairDetails() != null && !seller.getRepairDetails().trim().isEmpty()) {
            updatedNotes.append("- Repair Details: ").append(seller.getRepairDetails()).append("\n");
        }
        
        if (seller.getAdditionalNotes() != null && !seller.getAdditionalNotes().trim().isEmpty()) {
            updatedNotes.append("- Additional Notes: ").append(seller.getAdditionalNotes()).append("\n");
        }
        
        client.setNotes(updatedNotes.toString());
        
        // Ensure client is active and opted in
        client.setIsActive(true);
        client.setEmailOptedIn(true);
    }
}