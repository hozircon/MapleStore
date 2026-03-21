package com.msshop.service;

import com.msshop.domain.Announcement;
import com.msshop.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository repo;

    @Transactional(readOnly = true)
    public List<Announcement> findAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    /** Returns the latest announcement, or null if none exist. */
    @Transactional(readOnly = true)
    public Announcement findLatest() {
        return repo.findAllByOrderByCreatedAtDesc().stream().findFirst().orElse(null);
    }

    @Transactional
    public void create(String message) {
        Announcement a = new Announcement();
        a.setMessage(message);
        repo.save(a);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
