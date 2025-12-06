package com.roomsy.backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.News;
import com.roomsy.backend.repository.NewsRepository;

import jakarta.transaction.Transactional;

@Service
public class NewsService {
    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public News getNews(@NonNull UUID newsId, @NonNull UUID groupId) throws ResourceNotFoundException {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("News item not found with id: " + newsId));
        if (!news.getGroup().getId().equals(groupId)) {
            throw new ResourceNotFoundException("News item not found in the specified group");
        }
        return news;
    }

    public Page<News> getGroupNews(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        return newsRepository.findByGroupId(groupId, pageable);
    }

    @Transactional
    public void deleteUser(@NonNull UUID userId) {
        newsRepository.findByActorId(userId).forEach(news -> {
            newsRepository.delete(news);
        });
    }
    
}
