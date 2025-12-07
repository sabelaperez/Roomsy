package com.roomsy.backend.repository;

import com.roomsy.backend.model.News;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
    Page<News> findByGroupId(UUID groupId, Pageable pageable);
    void deleteByGroupId(UUID groupId);
    List<News> findByActorId(UUID actorId);
}
