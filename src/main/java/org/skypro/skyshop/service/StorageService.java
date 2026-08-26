package org.skypro.skyshop.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.skypro.skyshop.model.article.Article;
import org.skypro.skyshop.model.product.DiscountedProduct;
import org.skypro.skyshop.model.product.FixPriceProduct;
import org.skypro.skyshop.model.product.Product;
import org.skypro.skyshop.model.product.SimpleProduct;
import org.skypro.skyshop.model.search.Searchable;
import org.springframework.stereotype.Service;

@Service
public class StorageService {
    private final Map<UUID, Product> products;
    private final Map<UUID, Article> articles;

    public StorageService() {
        products = new HashMap<>();
        articles = new HashMap<>();
        initializeTestData();
    }

    public Collection<Product> getAllProducts() {
        return products.values();
    }

    public Collection<Article> getAllArticles() {
        return articles.values();
    }

    public Collection<Searchable> getAllSearchables() {
        Collection<Searchable> searchables = new ArrayList<>(products.values());
        searchables.addAll(articles.values());
        return searchables;
    }

    private void initializeTestData() {
        addProduct(new SimpleProduct(UUID.randomUUID(), "Хлеб", 50));
        addProduct(new SimpleProduct(UUID.randomUUID(), "Молоко", 80));
        addProduct(new DiscountedProduct(UUID.randomUUID(), "Яблоки", 120, 20));
        addProduct(new FixPriceProduct(UUID.randomUUID(), "Сыр"));
        addProduct(new DiscountedProduct(UUID.randomUUID(), "Колбаса", 300, 10));
        addProduct(new SimpleProduct(UUID.randomUUID(), "Сок", 150));

        addArticle(new Article(
                UUID.randomUUID(),
                "Польза яблок",
                "Яблоки очень полезны для здоровья."
        ));
        addArticle(new Article(
                UUID.randomUUID(),
                "Как выбрать хлеб",
                "Хлеб должен быть свежим и ароматным."
        ));
        addArticle(new Article(
                UUID.randomUUID(),
                "Молоко и кальций",
                "Молоко - отличный источник кальция."
        ));
    }

    private void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    private void addArticle(Article article) {
        articles.put(article.getId(), article);
    }
}
