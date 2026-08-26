package org.skypro.skyshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

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
}
