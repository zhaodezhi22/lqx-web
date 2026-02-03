package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.dto.SearchDTO;
import com.lqx.opera.entity.HeritageResource;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.entity.Product;
import com.lqx.opera.service.HeritageResourceService;
import com.lqx.opera.service.PerformanceEventService;
import com.lqx.opera.service.ProductService;
import com.lqx.opera.service.SearchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private final ProductService productService;
    private final PerformanceEventService performanceEventService;
    private final HeritageResourceService heritageResourceService;

    public SearchServiceImpl(ProductService productService,
                             PerformanceEventService performanceEventService,
                             HeritageResourceService heritageResourceService) {
        this.productService = productService;
        this.performanceEventService = performanceEventService;
        this.heritageResourceService = heritageResourceService;
    }

    @Override
    public List<SearchDTO> globalSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Search Products
        CompletableFuture<List<SearchDTO>> productFuture = CompletableFuture.supplyAsync(() -> {
            List<Product> products = productService.list(new LambdaQueryWrapper<Product>()
                    .like(Product::getName, keyword)
                    .or()
                    .like(Product::getDetail, keyword)
                    .last("LIMIT 20")); // Limit to avoid too many results
            return products.stream().map(p -> {
                SearchDTO dto = new SearchDTO();
                dto.setId(String.valueOf(p.getProductId()));
                dto.setOriginalId(p.getProductId());
                dto.setTitle(p.getName());
                dto.setType("product");
                dto.setCover(p.getMainImage());
                dto.setSubTitle(p.getSubTitle());
                dto.setPrice(p.getPrice());
                return dto;
            }).collect(Collectors.toList());
        });

        // 2. Search Events
        CompletableFuture<List<SearchDTO>> eventFuture = CompletableFuture.supplyAsync(() -> {
            List<PerformanceEvent> events = performanceEventService.list(new LambdaQueryWrapper<PerformanceEvent>()
                    .like(PerformanceEvent::getTitle, keyword)
                    .or()
                    .like(PerformanceEvent::getVenue, keyword)
                    .last("LIMIT 20"));
            return events.stream().map(e -> {
                SearchDTO dto = new SearchDTO();
                dto.setId(String.valueOf(e.getEventId()));
                dto.setOriginalId(e.getEventId());
                dto.setTitle(e.getTitle());
                dto.setType("event");
                dto.setCover(""); // No cover image in PerformanceEvent entity
                dto.setSubTitle(e.getVenue());
                dto.setPrice(e.getTicketPrice());
                return dto;
            }).collect(Collectors.toList());
        });

        // 3. Search Resources
        CompletableFuture<List<SearchDTO>> resourceFuture = CompletableFuture.supplyAsync(() -> {
            List<HeritageResource> resources = heritageResourceService.list(new LambdaQueryWrapper<HeritageResource>()
                    .like(HeritageResource::getTitle, keyword)
                    .or()
                    .like(HeritageResource::getDescription, keyword)
                    .last("LIMIT 20"));
            return resources.stream().map(r -> {
                SearchDTO dto = new SearchDTO();
                dto.setId(String.valueOf(r.getResourceId()));
                dto.setOriginalId(r.getResourceId());
                dto.setTitle(r.getTitle());
                dto.setType("resource");
                dto.setCover(r.getCoverImg());
                dto.setSubTitle(r.getDescription());
                dto.setPrice(null);
                return dto;
            }).collect(Collectors.toList());
        });

        List<SearchDTO> result = new ArrayList<>();
        try {
            // Wait for all to complete
            CompletableFuture.allOf(productFuture, eventFuture, resourceFuture).join();
            
            result.addAll(productFuture.get());
            result.addAll(eventFuture.get());
            result.addAll(resourceFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
            // In case of error, return what we have or empty
        }

        // Simple sorting: put exact matches or startsWith matches first
        result.sort((o1, o2) -> {
            boolean o1Start = o1.getTitle().startsWith(keyword);
            boolean o2Start = o2.getTitle().startsWith(keyword);
            if (o1Start && !o2Start) return -1;
            if (!o1Start && o2Start) return 1;
            return 0;
        });

        return result;
    }
}
