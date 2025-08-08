package com.realestate.sellerfunnel.service;

import com.realestate.sellerfunnel.model.Settings;
import com.realestate.sellerfunnel.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;

    public Settings getSettingsOrDefault() {
        return settingsRepository.findAll().stream().findFirst().orElseGet(Settings::new);
    }

    @Transactional
    public Settings save(Settings settings) {
        // Ensure only one settings row
        Optional<Settings> existing = settingsRepository.findAll().stream().findFirst();
        if (existing.isPresent()) {
            settings.setId(existing.get().getId());
        }
        return settingsRepository.save(settings);
    }
}
