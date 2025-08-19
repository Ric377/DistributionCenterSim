package com.example.distributioncentersim

/**
 * Хранит состояние одного порта (статус и прогресс выполнения).
 */
data class PortState(
    val status: String = "Свободен",
    val progress: Float = 0f
)