package org.skypro.skyshop.service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skypro.skyshop.exception.NoSuchProductException;
import org.skypro.skyshop.model.basket.ProductBasket;
import org.skypro.skyshop.model.basket.UserBasket;
import org.skypro.skyshop.model.product.Product;
import org.skypro.skyshop.model.product.SimpleProduct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {
    @Mock
    private ProductBasket productBasket;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private BasketService basketService;

    @Test
    void addingMissingProductThrowsException() {
        UUID invalidId = UUID.randomUUID();
        when(storageService.getProductById(invalidId)).thenReturn(Optional.empty());

        assertThrows(NoSuchProductException.class, () -> basketService.addProduct(invalidId));
        verify(productBasket, never()).addProduct(invalidId);
    }

    @Test
    void addingExistingProductDelegatesToProductBasket() {
        UUID validId = UUID.randomUUID();
        Product product = new SimpleProduct(validId, "TestProduct", 100);
        when(storageService.getProductById(validId)).thenReturn(Optional.of(product));

        basketService.addProduct(validId);

        verify(productBasket, times(1)).addProduct(validId);
    }

    @Test
    void getUserBasketReturnsEmptyBasket() {
        when(productBasket.getProducts()).thenReturn(Collections.emptyMap());

        UserBasket userBasket = basketService.getUserBasket();

        assertThat(userBasket.getItems()).isEmpty();
        assertThat(userBasket.getTotal()).isZero();
    }

    @Test
    void getUserBasketReturnsProductsAndCalculatedTotal() {
        UUID productId = UUID.randomUUID();
        int quantity = 3;
        Product product = new SimpleProduct(productId, "TestProduct", 150);
        when(productBasket.getProducts()).thenReturn(Map.of(productId, quantity));
        when(storageService.getProductById(productId)).thenReturn(Optional.of(product));

        UserBasket userBasket = basketService.getUserBasket();

        assertThat(userBasket.getItems()).hasSize(1);
        assertThat(userBasket.getItems().get(0).getProduct()).isSameAs(product);
        assertThat(userBasket.getItems().get(0).getQuantity()).isEqualTo(quantity);
        assertThat(userBasket.getTotal()).isEqualTo(product.getPrice() * quantity);
    }
}
