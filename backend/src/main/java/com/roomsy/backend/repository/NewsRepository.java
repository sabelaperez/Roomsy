package com.roomsy.backend.repository;

import com.roomsy.backend.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
}
