package org.skypro.skyshop;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.skypro.skyshop.model.product.Product;
import org.skypro.skyshop.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SkyshopApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageService storageService;

    @Test
    void contextLoads() {
    }

    @Test
    void productsEndpointReturnsProductsWithoutSearchFields() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].searchTerm").doesNotExist())
                .andExpect(jsonPath("$[0].contentType").doesNotExist());
    }

    @Test
    void articlesEndpointReturnsArticlesWithoutSearchFields() throws Exception {
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].searchTerm").doesNotExist())
                .andExpect(jsonPath("$[0].contentType").doesNotExist());
    }

    @Test
    void searchEndpointReturnsFilteredSearchResults() throws Exception {
        mockMvc.perform(get("/search").param("pattern", "Молоко"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Молоко", "Молоко и кальций")))
                .andExpect(jsonPath("$[*].contentType", containsInAnyOrder("PRODUCT", "ARTICLE")));
    }

    @Test
    void basketStoresQuantitiesWithinOneSession() throws Exception {
        Product product = storageService.getAllProducts().iterator().next();
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/basket/{id}", product.getId()).session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/basket/{id}", product.getId()).session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/basket").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.total").value(product.getPrice() * 2));
    }

    @Test
    void basketIsIsolatedBetweenSessions() throws Exception {
        Product product = storageService.getAllProducts().iterator().next();
        MockHttpSession firstSession = new MockHttpSession();
        MockHttpSession secondSession = new MockHttpSession();

        mockMvc.perform(get("/basket/{id}", product.getId()).session(firstSession))
                .andExpect(status().isOk());

        mockMvc.perform(get("/basket").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void missingProductReturnsStructuredNotFoundError() throws Exception {
        mockMvc.perform(get("/basket/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Товар не найден"));
    }
}
