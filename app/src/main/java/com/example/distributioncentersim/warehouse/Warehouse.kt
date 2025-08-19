package com.example.distributioncentersim.warehouse

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.distributioncentersim.models.Product
import com.example.distributioncentersim.models.ProductCategory

/**
 * Класс склада распределительного центра.
 * Использует Mutex для синхронизации доступа к хранилищу товаров.
 * @param onStateChanged Функция обратного вызова для уведомления об изменениях на складе.
 */
class Warehouse(
    private val onStateChanged: suspend (stock: Map<ProductCategory, Int>) -> Unit
) {
    private val storage = mutableMapOf<ProductCategory, MutableList<Product>>()
    private val mutex = Mutex()

    /**
     * Инициализирует склад и отправляет начальное состояние.
     */
    init {
        // Заполняем storage всеми категориями, чтобы они отображались в UI с самого начала
        ProductCategory.values().forEach { category ->
            storage[category] = mutableListOf()
        }
    }

    /**
     * Добавляет товар на склад.
     */
    suspend fun addProduct(product: Product) {
        mutex.withLock {
            storage.getOrPut(product.category) { mutableListOf() }.add(product)
            notifyStateChange()
        }
    }

    /**
     * Забирает указанное количество товаров со склада.
     * @return Список изъятых товаров.
     */
    suspend fun removeProducts(category: ProductCategory, neededCount: Int): List<Product> {
        return mutex.withLock {
            val available = storage.getOrPut(category) { mutableListOf() }
            val extracted = available.take(neededCount)
            if (extracted.isNotEmpty()) {
                repeat(extracted.size) { available.removeAt(0) }
                notifyStateChange()
            }
            extracted
        }
    }

    /**
     * Уведомляет подписчика об изменении состояния склада.
     */
    private suspend fun notifyStateChange() {
        val currentStock = storage.mapValues { it.value.size }
        onStateChanged(currentStock)
    }
}