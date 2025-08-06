package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Buyer;
import com.realestate.sellerfunnel.model.Seller;
import com.realestate.sellerfunnel.model.Client;
import com.realestate.sellerfunnel.repository.BuyerRepository;
import com.realestate.sellerfunnel.repository.SellerRepository;
import com.realestate.sellerfunnel.repository.ClientRepository;
import com.realestate.sellerfunnel.service.FileUploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/buyer")
    public String buyerForm(Model model) {
        model.addAttribute("buyer", new Buyer());
        return "buyer-form";
    }

    @PostMapping("/buyer")
    public String submitBuyer(@Valid @ModelAttribute("buyer") Buyer buyer, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "buyer-form";
        }
        
        // Save the buyer
        Buyer savedBuyer = buyerRepository.save(buyer);
        
        // Create or update client record
        createOrUpdateClientFromBuyer(savedBuyer);
        
        redirectAttributes.addFlashAttribute("message", "Thank you! Your buyer information has been submitted successfully.");
        return "redirect:/buyer/success";
    }

    @GetMapping("/buyer/success")
    public String buyerSuccess() {
        return "buyer-success";
    }

    @GetMapping("/seller")
    public String sellerForm(Model model) {
        model.addAttribute("seller", new Seller());
        return "seller-form";
    }

    @PostMapping("/seller")
    public String submitSeller(@Valid @ModelAttribute("seller") Seller seller, 
                              BindingResult result,
                              @RequestParam("photos") List<MultipartFile> photos,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "seller-form";
        }
        
        try {
            // Save seller first
            Seller savedSeller = sellerRepository.save(seller);
            
            // Create or update client record
            createOrUpdateClientFromSeller(savedSeller);
            
            // Then save photos
            if (photos != null && !photos.isEmpty()) {
                fileUploadService.savePropertyPhotos(photos, savedSeller);
            }
            
            redirectAttributes.addFlashAttribute("message", "Thank you! Your property information has been submitted successfully.");
            return "redirect:/seller/success";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "There was an error uploading your photos. Please try again.");
            return "seller-form";
        }
    }

    @GetMapping("/seller/success")
    public String sellerSuccess() {
        return "seller-success";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("buyers", buyerRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("sellers", sellerRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("clients", clientRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        return "admin";
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
}