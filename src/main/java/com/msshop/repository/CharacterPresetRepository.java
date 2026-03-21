package com.msshop.repository;

import com.msshop.domain.CharacterPreset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterPresetRepository extends JpaRepository<CharacterPreset, Long> {

    List<CharacterPreset> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}
