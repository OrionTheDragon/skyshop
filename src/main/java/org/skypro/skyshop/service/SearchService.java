package org.skypro.skyshop.service;

import java.util.Collection;
import java.util.stream.Collectors;
import org.skypro.skyshop.model.search.SearchResult;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private final StorageService storageService;

    public SearchService(StorageService storageService) {
        this.storageService = storageService;
    }

    public Collection<SearchResult> search(String pattern) {
        return storageService.getAllSearchables().stream()
                .filter(searchable -> searchable.getSearchTerm().contains(pattern))
                .map(SearchResult::fromSearchable)
                .collect(Collectors.toList());
    }
}
