package org.skypro.skyshop.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skypro.skyshop.model.product.SimpleProduct;
import org.skypro.skyshop.model.search.SearchResult;
import org.skypro.skyshop.model.search.Searchable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock
    private StorageService storageService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void searchReturnsEmptyCollectionWhenStorageIsEmpty() {
        when(storageService.getAllSearchables()).thenReturn(Collections.emptyList());

        Collection<SearchResult> results = searchService.search("Test");

        assertThat(results).isEmpty();
    }

    @Test
    void searchReturnsEmptyCollectionWhenNothingMatches() {
        Searchable product = new SimpleProduct(UUID.randomUUID(), "TestProduct", 100);
        when(storageService.getAllSearchables()).thenReturn(List.of(product));

        Collection<SearchResult> results = searchService.search("Missing");

        assertThat(results).isEmpty();
    }

    @Test
    void searchReturnsMatchingObject() {
        UUID productId = UUID.randomUUID();
        Searchable product = new SimpleProduct(productId, "TestProduct", 100);
        when(storageService.getAllSearchables()).thenReturn(List.of(product));

        Collection<SearchResult> results = searchService.search("Test");

        assertThat(results).hasSize(1);
        SearchResult result = results.iterator().next();
        assertThat(result.getId()).isEqualTo(productId.toString());
        assertThat(result.getName()).isEqualTo("TestProduct");
        assertThat(result.getContentType()).isEqualTo("PRODUCT");
    }
}
