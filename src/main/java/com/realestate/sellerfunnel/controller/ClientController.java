package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.repository.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.realestate.sellerfunnel.service.ClientImportService;

@Controller
@RequestMapping("/admin/clients")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private ClientImportService clientImportService;

    @GetMapping
    public String listClients(Model model, 
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String clientType,
                             @RequestParam(required = false) String clientStatus,
                             @RequestParam(required = false) String leadSource,
                             @RequestParam(required = false) String city,
                             @RequestParam(required = false) String state) {
        
        List<Client> clients;
        
        if (search != null && !search.trim().isEmpty()) {
            // Advanced search
            clients = clientRepository.findByAdvancedSearch(
                search, search, search, search, clientType, clientStatus, 
                leadSource, city, state, null, true
            );
        } else {
            // Filtered search
            clients = clientRepository.findByFilters(clientType, clientStatus, leadSource, city, state);
        }
        
        model.addAttribute("clients", clients);
        model.addAttribute("search", search);
        model.addAttribute("clientType", clientType);
        model.addAttribute("clientStatus", clientStatus);
        model.addAttribute("leadSource", leadSource);
        model.addAttribute("city", city);
        model.addAttribute("state", state);
        
        // Add statistics
        addClientStatistics(model);
        
        return "admin/clients/list";
    }

    @GetMapping("/new")
    public String newClient(Model model) {
        model.addAttribute("client", new Client());
        addFormData(model);
        return "admin/clients/form";
    }

    @GetMapping("/{id}")
    public String viewClient(@PathVariable Long id, Model model) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Client not found"));
        
        model.addAttribute("client", client);
        return "admin/clients/view";
    }

    @GetMapping("/{id}/edit")
    public String editClient(@PathVariable Long id, Model model) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Client not found"));
        
        model.addAttribute("client", client);
        addFormData(model);
        return "admin/clients/form";
    }

    @PostMapping
    public String saveClient(@Valid @ModelAttribute Client client, 
                            BindingResult result, 
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            addFormData(model);
            return "admin/clients/form";
        }
        
        // Check for duplicate email
        if (client.getId() == null) { // New client
            if (clientRepository.findByEmail(client.getEmail()).isPresent()) {
                result.rejectValue("email", "error.email", "A client with this email already exists");
                addFormData(model);
                return "admin/clients/form";
            }
        } else { // Existing client
            clientRepository.findByEmail(client.getEmail()).ifPresent(existingClient -> {
                if (!existingClient.getId().equals(client.getId())) {
                    result.rejectValue("email", "error.email", "A client with this email already exists");
                }
            });
            if (result.hasErrors()) {
                addFormData(model);
                return "admin/clients/form";
            }
        }
        
        clientRepository.save(client);
        
        String message = (client.getId() == null || client.getId() == 0) ? "Client created successfully!" : "Client updated successfully!";
        redirectAttributes.addFlashAttribute("message", message);
        
        return "redirect:/admin/clients";
    }

    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Client not found"));
        
        client.setIsActive(false);
        clientRepository.save(client);
        
        redirectAttributes.addFlashAttribute("message", "Client deactivated successfully!");
        return "redirect:/admin/clients";
    }

    @PostMapping("/{id}/contact")
    public String recordContact(@PathVariable Long id, 
                               @RequestParam String contactType,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirectAttributes) {
        
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Client not found"));
        
        client.incrementContact(contactType);
        if (notes != null && !notes.trim().isEmpty()) {
            String existingNotes = client.getNotes() != null ? client.getNotes() : "";
            client.setNotes(existingNotes + "\n" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")) + 
                          " - " + contactType.toUpperCase() + ": " + notes);
        }
        
        clientRepository.save(client);
        
        redirectAttributes.addFlashAttribute("message", 
            contactType.toUpperCase() + " contact recorded for " + client.getFullName());
        
        return "redirect:/admin/clients/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, 
                              @RequestParam String clientStatus,
                              RedirectAttributes redirectAttributes) {
        
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Client not found"));
        
        client.setClientStatus(clientStatus);
        clientRepository.save(client);
        
        redirectAttributes.addFlashAttribute("message", 
            "Client status updated to " + clientStatus);
        
        return "redirect:/admin/clients/" + id;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Client statistics
        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("activeClients", clientRepository.findByIsActiveTrueOrderByCreatedAtDesc().size());
        
        // Status breakdown
        List<Object[]> statusCounts = clientRepository.countByClientStatus();
        Map<String, Long> statusMap = statusCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        model.addAttribute("statusCounts", statusMap);
        
        // Type breakdown
        List<Object[]> typeCounts = clientRepository.countByClientType();
        Map<String, Long> typeMap = typeCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        model.addAttribute("typeCounts", typeMap);
        
        // Lead source breakdown
        List<Object[]> sourceCounts = clientRepository.countByLeadSource();
        Map<String, Long> sourceMap = sourceCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        model.addAttribute("sourceCounts", sourceMap);
        
        // Recent clients
        model.addAttribute("recentClients", clientRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        
        // Clients needing contact
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        model.addAttribute("clientsNeedingContact", clientRepository.findClientsNeedingContact(weekAgo));
        
        // Top contacted clients
        model.addAttribute("topContactedClients", clientRepository.findTopContactedClients());
        
        return "admin/clients/dashboard";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<Client> searchClients(@RequestParam String query) {
        return clientRepository.findByNameContainingIgnoreCase(query);
    }

    @GetMapping("/export")
    public String exportClients(Model model) {
        List<Client> allClients = clientRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        model.addAttribute("clients", allClients);
        return "admin/clients/export";
    }
    
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportClientsCSV() {
        List<Client> allClients = clientRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        
        StringBuilder csv = new StringBuilder();
        csv.append("First Name,Last Name,Email,Phone,Company,Job Title,Address,City,State,ZIP Code,Client Type,Status,Lead Source,Email Contact Count,Phone Contact Count,Total Contact Count,Last Contact Date,Notes,Created Date\n");
        
        for (Client client : allClients) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                escapeCsvField(client.getFirstName()),
                escapeCsvField(client.getLastName()),
                escapeCsvField(client.getEmail()),
                escapeCsvField(client.getPhoneNumber()),
                escapeCsvField(client.getCompanyName()),
                escapeCsvField(client.getJobTitle()),
                escapeCsvField(client.getAddress()),
                escapeCsvField(client.getCity()),
                escapeCsvField(client.getState()),
                escapeCsvField(client.getZipCode()),
                escapeCsvField(client.getClientType()),
                escapeCsvField(client.getClientStatus()),
                escapeCsvField(client.getLeadSource()),
                escapeCsvField(client.getEmailContactCount() != null ? client.getEmailContactCount().toString() : "0"),
                escapeCsvField(client.getPhoneContactCount() != null ? client.getPhoneContactCount().toString() : "0"),
                escapeCsvField(client.getTotalContactCount() != null ? client.getTotalContactCount().toString() : "0"),
                escapeCsvField(client.getLastContactDate() != null ? client.getLastContactDate().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")) : ""),
                escapeCsvField(client.getNotes()),
                escapeCsvField(client.getCreatedAt() != null ? client.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")) : "")
            ));
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "clients_export_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        
        return new ResponseEntity<>(csv.toString(), headers, HttpStatus.OK);
    }
    
    @GetMapping("/import")
    public String showImportForm(Model model) {
        return "admin/clients/import";
    }
    
    @PostMapping("/import")
    public String importClients(@RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/admin/clients/import";
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            redirectAttributes.addFlashAttribute("error", "Please upload an Excel (.xlsx) file");
            return "redirect:/admin/clients/import";
        }
        
        try {
            ClientImportService.ImportResult result = clientImportService.importClientsFromExcel(file);
            
            redirectAttributes.addFlashAttribute("importResult", result);
            redirectAttributes.addFlashAttribute("success", 
                String.format("Import completed: %d successful, %d errors, %d skipped", 
                    result.getSuccessCount(), result.getErrorCount(), result.getSkippedCount()));
            
            if (result.getErrorCount() > 0) {
                redirectAttributes.addFlashAttribute("warning", 
                    "Some records had errors. Check the details below.");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }
        
        return "redirect:/admin/clients/import";
    }
    
    private String escapeCsvField(String field) {
        if (field == null) return "";
        return field.replace("\"", "\"\"");
    }

    private void addClientStatistics(Model model) {
        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("activeClients", clientRepository.findByIsActiveTrueOrderByCreatedAtDesc().size());
        
        // Status counts
        List<Object[]> statusCounts = clientRepository.countByClientStatus();
        Map<String, Long> statusMap = statusCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        model.addAttribute("statusCounts", statusMap);
        
        // Type counts
        List<Object[]> typeCounts = clientRepository.countByClientType();
        Map<String, Long> typeMap = typeCounts.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        model.addAttribute("typeCounts", typeMap);
    }

    private void addFormData(Model model) {
        // Add dropdown options
        model.addAttribute("clientTypes", List.of("SELLER", "BUYER", "INVESTOR", "AGENT", "VENDOR"));
        model.addAttribute("clientStatuses", List.of("SUSPECT", "PROSPECT", "LEAD", "CONTRACT", "DEAL"));
        model.addAttribute("leadSources", List.of("WEBSITE", "REFERRAL", "SOCIAL_MEDIA", "COLD_CALL", "OPEN_HOUSE", "SIGN", "OTHER"));
        model.addAttribute("states", List.of("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"));
    }
} 