package com.example.distributioncentersim.ports

import com.example.distributioncentersim.warehouse.Warehouse
import kotlinx.coroutines.delay
import com.example.distributioncentersim.models.Truck

/**
 * Класс порта разгрузки.
 * @param portId Уникальный идентификатор порта.
 * @param warehouse Ссылка на склад.
 * @param onStateChanged Функция для уведомления об изменении статуса порта.
 */
class UnloadingPort(
    private val portId: Int,
    private val warehouse: Warehouse,
    private val onStateChanged: suspend (portId: Int, status: String) -> Unit
) {
    /**
     * Обрабатывает один грузовик: разгружает его товары на склад.
     */
    suspend fun processTruck(truck: Truck) {
        onStateChanged(portId, "Разгрузка #${truck.id}")
        truck.products.forEach { product ->
            delay(product.category.processingTime)
            warehouse.addProduct(product)
        }
        onStateChanged(portId, "Свободен")
    }
}