package com.example.distributioncentersim.ports

import com.example.distributioncentersim.warehouse.Warehouse
import kotlinx.coroutines.delay
import com.example.distributioncentersim.models.ProductCategory
import com.example.distributioncentersim.models.Truck

/**
 * Класс порта загрузки.
 * @param portId Уникальный идентификатор порта.
 * @param warehouse Ссылка на склад.
 * @param onStateChanged Функция для уведомления об изменении статуса порта.
 */
class LoadingPort(
    private val portId: Int,
    private val warehouse: Warehouse,
    private val onStateChanged: suspend (portId: Int, status: String) -> Unit
) {
    /**
     * Обрабатывает один грузовик: загружает его товарами со склада.
     */
    suspend fun processTruck(truck: Truck) {
        val loadCategory: ProductCategory = truck.loadCategory ?: run {
            onStateChanged(portId, "Ошибка")
            delay(1000) // Показываем ошибку на секунду
            onStateChanged(portId, "Свободен")
            return
        }
        onStateChanged(portId, "Загрузка #${truck.id}")
        val neededCapacity = truck.type.capacity
        var currentLoad = 0
        while (currentLoad < neededCapacity) {
            val extracted = warehouse.removeProducts(loadCategory, 1)
            if (extracted.isEmpty()) {
                onStateChanged(portId, "Ожидает ${loadCategory.displayName}")
                delay(1000L)
                continue
            }
            // Если начали загрузку, показываем это
            onStateChanged(portId, "Загрузка #${truck.id}")
            delay(loadCategory.processingTime)
            currentLoad += loadCategory.weight
        }
        onStateChanged(portId, "Свободен")
    }
}