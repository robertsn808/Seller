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
import java.util.Optional;

@Controller
@RequestMapping("/admin/clients")
public class ClientController {
    
    @Autowired
    private ClientRepository clientRepository;
    
    @GetMapping
    public String listClients(Model model, 
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String clientType,
                             @RequestParam(required = false) String leadSource) {
        
        List<Client> clients;
        
        if (search != null && !search.trim().isEmpty()) {
            clients = clientRepository.findByNameContainingIgnoreCase(search.trim());
        } else if (clientType != null && !clientType.isEmpty()) {
            clients = clientRepository.findByClientTypeOrderByCreatedAtDesc(clientType);
        } else if (leadSource != null && !leadSource.isEmpty()) {
            clients = clientRepository.findByLeadSourceOrderByCreatedAtDesc(leadSource);
        } else {
            clients = clientRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        }
        
        model.addAttribute("clients", clients);
        model.addAttribute("search", search);
        model.addAttribute("clientType", clientType);
        model.addAttribute("leadSource", leadSource);
        
        // Add statistics
        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("activeClients", clientRepository.findByIsActiveTrueOrderByCreatedAtDesc().size());
        model.addAttribute("emailOptedIn", clientRepository.findByEmailOptedInTrueAndIsActiveTrueOrderByCreatedAtDesc().size());
        
        return "admin/clients/list";
    }
    
    @GetMapping("/new")
    public String newClient(Model model) {
        model.addAttribute("client", new Client());
        return "admin/clients/form";
    }
    
    @GetMapping("/{id}")
    public String viewClient(@PathVariable Long id, Model model) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            model.addAttribute("client", clientOpt.get());
            return "admin/clients/view";
        }
        return "redirect:/admin/clients";
    }
    
    @GetMapping("/{id}/edit")
    public String editClient(@PathVariable Long id, Model model) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            model.addAttribute("client", clientOpt.get());
            return "admin/clients/form";
        }
        return "redirect:/admin/clients";
    }
    
    @PostMapping
    public String saveClient(@Valid @ModelAttribute Client client, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/clients/form";
        }
        
        // Check for duplicate email
        Optional<Client> existingClient = clientRepository.findByEmail(client.getEmail());
        if (existingClient.isPresent() && !existingClient.get().getId().equals(client.getId())) {
            result.rejectValue("email", "error.email", "Email address already exists");
            return "admin/clients/form";
        }
        
        clientRepository.save(client);
        redirectAttributes.addFlashAttribute("message", "Client saved successfully!");
        
        return "redirect:/admin/clients";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            client.setIsActive(false);
            clientRepository.save(client);
            redirectAttributes.addFlashAttribute("message", "Client deactivated successfully!");
        }
        return "redirect:/admin/clients";
    }
    
    @PostMapping("/{id}/toggle-email-opt-in")
    public String toggleEmailOptIn(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            client.setEmailOptedIn(!client.getEmailOptedIn());
            clientRepository.save(client);
            
            String message = client.getEmailOptedIn() ? "Email opt-in enabled" : "Email opt-in disabled";
            redirectAttributes.addFlashAttribute("message", message + " for " + client.getFullName());
        }
        return "redirect:/admin/clients/" + id;
    }
    
    @PostMapping("/{id}/update-contact-date")
    public String updateContactDate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            client.setLastContactDate(LocalDateTime.now());
            clientRepository.save(client);
            redirectAttributes.addFlashAttribute("message", "Contact date updated for " + client.getFullName());
        }
        return "redirect:/admin/clients/" + id;
    }
    
    @GetMapping("/import")
    public String importClients() {
        return "admin/clients/import";
    }
    
    @PostMapping("/import")
    public String processImport(@RequestParam("csvFile") String csvContent, 
                               RedirectAttributes redirectAttributes) {
        // TODO: Implement CSV import functionality
        redirectAttributes.addFlashAttribute("message", "Import functionality coming soon!");
        return "redirect:/admin/clients";
    }
    
    @GetMapping("/export")
    public String exportClients() {
        // TODO: Implement CSV export functionality
        return "redirect:/admin/clients";
    }
    
    @GetMapping("/analytics")
    public String analytics(Model model) {
        // Client type statistics
        List<Object[]> clientTypeStats = clientRepository.countByClientType();
        model.addAttribute("clientTypeStats", clientTypeStats);
        
        // Lead source statistics
        List<Object[]> leadSourceStats = clientRepository.countByLeadSource();
        model.addAttribute("leadSourceStats", leadSourceStats);
        
        // Recent clients (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Client> recentClients = clientRepository.findByDateRange(thirtyDaysAgo, LocalDateTime.now());
        model.addAttribute("recentClients", recentClients);
        
        // Clients needing follow-up
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Client> clientsNeedingContact = clientRepository.findClientsNeedingContact(sevenDaysAgo);
        model.addAttribute("clientsNeedingContact", clientsNeedingContact);
        
        return "admin/clients/analytics";
    }
} 