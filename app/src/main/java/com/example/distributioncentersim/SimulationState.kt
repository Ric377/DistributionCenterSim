package com.example.distributioncentersim

import com.example.distributioncentersim.models.ProductCategory

/**
 * Хранит полное состояние UI для экрана симуляции.
 */
data class SimulationState(
    val warehouseStock: Map<ProductCategory, Int> = emptyMap(),
    val unloadingPortsStatus: List<String> = List(3) { "Свободен" },
    val loadingPortsStatus: List<String> = List(2) { "Свободен" },
    val logMessages: List<String> = emptyList(),
    val isRunning: Boolean = false
)