package com.msshop.service;

import com.msshop.domain.CharacterPreset;
import com.msshop.repository.CharacterPresetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterPresetService {

    private final CharacterPresetRepository repo;

    @Transactional(readOnly = true)
    public List<CharacterPreset> findAll() {
        return repo.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<String> findAllNames() {
        return repo.findAllByOrderByNameAsc()
                .stream().map(CharacterPreset::getName).toList();
    }

    @Transactional
    public void create(String name) {
        if (name == null || name.isBlank()) return;
        String trimmed = name.trim();
        if (!repo.existsByName(trimmed)) {
            repo.save(new CharacterPreset(trimmed));
        }
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
