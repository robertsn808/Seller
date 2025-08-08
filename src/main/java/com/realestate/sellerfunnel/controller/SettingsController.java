package com.realestate.sellerfunnel.controller;

import com.realestate.sellerfunnel.model.Settings;
import com.realestate.sellerfunnel.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public String showSettings(Model model) {
        Settings settings = settingsService.getSettingsOrDefault();
        model.addAttribute("settings", settings);
        return "admin/settings/form";
    }

    @PostMapping
    public String saveSettings(@ModelAttribute Settings settings, RedirectAttributes redirectAttributes) {
        settingsService.save(settings);
        redirectAttributes.addFlashAttribute("message", "Settings updated successfully");
        return "redirect:/admin/settings";
    }
}
