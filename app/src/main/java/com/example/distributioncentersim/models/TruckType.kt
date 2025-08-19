package com.example.distributioncentersim.models

/**
 * Определяет типы грузовиков.
 * - SMALL и MEDIUM могут участвовать в загрузке (canLoad = true).
 * - LARGE используется только для разгрузки.
 */
enum class TruckType(val capacity: Int, val canLoad: Boolean) {
    SMALL(100, true),
    MEDIUM(200, true),
    LARGE(300, false)
}
