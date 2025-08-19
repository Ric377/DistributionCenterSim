package com.example.distributioncentersim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.distributioncentersim.generators.generateRandomTruckForLoading
import com.example.distributioncentersim.generators.generateRandomTruckForUnloading
import com.example.distributioncentersim.ports.LoadingPort
import com.example.distributioncentersim.ports.UnloadingPort
import com.example.distributioncentersim.warehouse.Warehouse
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.distributioncentersim.models.Truck

/**
 * Управляет логикой и состоянием симуляции распределительного центра.
 */
class SimulationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SimulationState())
    val uiState = _uiState.asStateFlow()

    private var simulationJob: Job? = null
    private val unloadingChannel = Channel<Truck>(Channel.UNLIMITED)
    private val loadingChannel = Channel<Truck>(Channel.UNLIMITED)

    private val NUM_UNLOADING_PORTS = 3
    private val NUM_LOADING_PORTS = 2

    /**
     * Запускает всю симуляцию.
     */
    fun startSimulation() {
        if (simulationJob?.isActive == true) return

        // Сброс состояния перед новым запуском
        _uiState.value = SimulationState(isRunning = true, logMessages = listOf("▶️ Симуляция запущена!"))

        simulationJob = viewModelScope.launch {
            val warehouse = Warehouse { stock ->
                _uiState.update { it.copy(warehouseStock = stock) }
            }

            // Запуск портов разгрузки
            val unloadingPorts = List(NUM_UNLOADING_PORTS) { index ->
                UnloadingPort(index + 1, warehouse) { portId, status ->
                    _uiState.update { state ->
                        val newStatuses = state.unloadingPortsStatus.toMutableList()
                        newStatuses[portId - 1] = status
                        state.copy(unloadingPortsStatus = newStatuses)
                    }
                }
            }
            unloadingPorts.forEach { port ->
                launch {
                    for (truck in unloadingChannel) {
                        port.processTruck(truck)
                    }
                }
            }

            // Запуск портов загрузки
            val loadingPorts = List(NUM_LOADING_PORTS) { index ->
                LoadingPort(index + 1, warehouse) { portId, status ->
                    _uiState.update { state ->
                        val newStatuses = state.loadingPortsStatus.toMutableList()
                        newStatuses[portId - 1] = status
                        state.copy(loadingPortsStatus = newStatuses)
                    }
                }
            }
            loadingPorts.forEach { port ->
                launch {
                    for (truck in loadingChannel) {
                        port.processTruck(truck)
                    }
                }
            }

            // Запуск генератора грузовиков для разгрузки
            launch {
                while (isActive) {
                    val truck = generateRandomTruckForUnloading()
                    addLogMessage("🚚 Создан грузовик #${truck.id} для РАЗГРУЗКИ.")
                    unloadingChannel.send(truck)
                    delay(8000L) // Каждые 8 секунд
                }
            }

            // Запуск генератора грузовиков для загрузки
            launch {
                while (isActive) {
                    val truck = generateRandomTruckForLoading()
                    addLogMessage("🚛 Создан грузовик #${truck.id} для ЗАГРУЗКИ товаром '${truck.loadCategory?.displayName}'.")
                    loadingChannel.send(truck)
                    delay(5000L) // Каждые 5 секунд
                }
            }
        }
    }

    /**
     * Останавливает симуляцию.
     */
    fun stopSimulation() {
        simulationJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
        addLogMessage("⏹️ Симуляция остановлена.")
    }

    private fun addLogMessage(message: String) {
        _uiState.update {
            val newLogs = (listOf(message) + it.logMessages).take(100)
            it.copy(logMessages = newLogs)
        }
    }
}