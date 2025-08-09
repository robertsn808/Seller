package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.service.BatchImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/clients/batch-import")
public class BatchImportController {

    @Autowired
    private BatchImportService batchImportService;

    @GetMapping
    public String showBatchImportForm() {
        return "admin/clients/batch-import";
    }

    @PostMapping
    public String handleBatchImport(@RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/admin/clients/batch-import";
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !(originalFilename.endsWith(".xlsx") || originalFilename.endsWith(".csv"))) {
            redirectAttributes.addFlashAttribute("error", "Please upload an Excel (.xlsx) or CSV (.csv) file");
            return "redirect:/admin/clients/batch-import";
        }

        try {
            String importId = batchImportService.processLargeImport(file).get();
            redirectAttributes.addFlashAttribute("importId", importId);
            redirectAttributes.addFlashAttribute("message", 
                "File upload successful. Import processing has started. Use the progress tracker to monitor the import.");
            return "redirect:/admin/clients/batch-import/progress/" + importId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
            return "redirect:/admin/clients/batch-import";
        }
    }

    @GetMapping("/progress/{importId}")
    public String showImportProgress(@PathVariable String importId, Model model) {
        BatchImportService.ImportProgress progress = batchImportService.getImportProgress(importId);
        if (progress == null) {
            model.addAttribute("error", "Import not found");
            return "admin/clients/batch-import";
        }
        
        model.addAttribute("importId", importId);
        model.addAttribute("progress", progress);
        return "admin/clients/import-progress";
    }

    @GetMapping("/progress/{importId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getImportStatus(@PathVariable String importId) {
        BatchImportService.ImportProgress progress = batchImportService.getImportProgress(importId);
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", progress.getStatus());
        response.put("progress", progress.getProgress());
        response.put("processedRecords", progress.getProcessedRecords());
        response.put("totalRecords", progress.getTotalRecords());
        response.put("successCount", progress.getSuccessCount());
        response.put("errorCount", progress.getErrorCount());
        response.put("skippedCount", progress.getSkippedCount());
        response.put("recentErrors", progress.getRecentErrors());
        
        if (progress.getErrorMessage() != null) {
            response.put("errorMessage", progress.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }
}
