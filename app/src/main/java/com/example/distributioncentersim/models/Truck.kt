package com.example.distributioncentersim.models

/**
 * Класс грузовика.
 *
 * При разгрузке truck.products содержит список товаров,
 * а при загрузке используется параметр loadCategory – тип товара, которым должен быть загружен грузовик.
 */
data class Truck(
    val id: Int,
    val type: TruckType,
    val products: List<Product> = emptyList(),
    val loadCategory: ProductCategory? = null
)
