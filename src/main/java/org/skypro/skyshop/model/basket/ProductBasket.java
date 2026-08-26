package org.skypro.skyshop.model.basket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class ProductBasket {
    private final Map<UUID, Integer> products = new HashMap<>();

    public void addProduct(UUID id) {
        products.computeIfAbsent(id, key -> 0);
        products.put(id, products.get(id) + 1);
    }

    public Map<UUID, Integer> getProducts() {
        return Collections.unmodifiableMap(products);
    }
}
