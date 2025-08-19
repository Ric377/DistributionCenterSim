package com.example.distributioncentersim.generators

import com.example.distributioncentersim.models.Product
import com.example.distributioncentersim.models.ProductCategory
import com.example.distributioncentersim.models.Truck
import com.example.distributioncentersim.models.TruckType
import kotlin.random.Random

/**
 * Функции для генерации случайных грузовиков.
 */

/**
 * Генерирует грузовик для разгрузки.
 */
fun generateRandomTruckForUnloading(): Truck {
    val type = TruckType.values().random()
    val isFoodOnly = Random.nextBoolean()
    val availableCategories = if (isFoodOnly) listOf(ProductCategory.FOOD)
    else ProductCategory.values().filter { it != ProductCategory.FOOD }
    val numProducts = Random.nextInt(1, 6)
    val products = List(numProducts) { Product(availableCategories.random()) }
    return Truck(Random.nextInt(1000, 9999), type, products)
}

/**
 * Генерирует грузовик для загрузки.
 */
fun generateRandomTruckForLoading(): Truck {
    val type = TruckType.values().filter { it.canLoad }.random()
    val category = ProductCategory.values().random()
    return Truck(Random.nextInt(1000, 9999), type, loadCategory = category)
}