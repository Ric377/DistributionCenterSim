package com.example.distributioncentersim.models

/**
 * Определяет типы товаров.
 * Для каждого товара указаны:
 * - displayName – наглядное имя для отображения,
 * - processingTime – время загрузки/разгрузки одной единицы товара,
 * - weight – вес одной единицы товара.
 */
enum class ProductCategory(val displayName: String, val processingTime: Long, val weight: Int) {
    LARGE("Крупногабаритный товар", 800L, 20),
    MEDIUM("Среднегабаритный товар", 500L, 10),
    SMALL("Малогабаритный товар", 300L, 5),
    FOOD("Пищевой товар", 250L, 3)
}
