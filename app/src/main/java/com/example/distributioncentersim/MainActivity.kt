package com.example.distributioncentersim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.distributioncentersim.ui.theme.DistributionCenterSimTheme
import com.example.distributioncentersim.models.ProductCategory

class MainActivity : ComponentActivity() {

    private val viewModel: SimulationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DistributionCenterSimTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                SimulationScreen(
                    state = state,
                    onStartClick = viewModel::startSimulation,
                    onStopClick = viewModel::stopSimulation
                )
            }
        }
    }
}

/**
 * Главный экран приложения, отображающий состояние симуляции.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(
    state: SimulationState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Симулятор склада") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Панель управления
            ControlPanel(
                isRunning = state.isRunning,
                onStartClick = onStartClick,
                onStopClick = onStopClick
            )
            Spacer(Modifier.height(16.dp))
            // Статус склада и портов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WarehouseStatus(modifier = Modifier.weight(1f), stock = state.warehouseStock)
                PortsStatus(
                    modifier = Modifier.weight(1f),
                    unloadingPorts = state.unloadingPortsStatus,
                    loadingPorts = state.loadingPortsStatus
                )
            }
            Spacer(Modifier.height(16.dp))
            // Логи
            LogPanel(logs = state.logMessages, modifier = Modifier.fillMaxHeight())
        }
    }
}

/**
 * Кнопки "Старт" и "Стоп".
 */
@Composable
fun ControlPanel(isRunning: Boolean, onStartClick: () -> Unit, onStopClick: () -> Unit) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onStartClick, enabled = !isRunning, modifier = Modifier.weight(1f)) {
                Text("Старт")
            }
            Button(onClick = onStopClick, enabled = isRunning, modifier = Modifier.weight(1f)) {
                Text("Стоп")
            }
        }
    }
}

/**
 * Карточка с состоянием товаров на складе.
 */
@Composable
fun WarehouseStatus(modifier: Modifier = Modifier, stock: Map<ProductCategory, Int>) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Склад", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (stock.isEmpty()) {
                Text("Пусто")
            } else {
                ProductCategory.values().forEach { category ->
                    val count = stock.getOrDefault(category, 0)
                    Text("${category.displayName}: $count шт.")
                }
            }
        }
    }
}

/**
 * Карточка с состоянием портов.
 */
@Composable
fun PortsStatus(
    modifier: Modifier = Modifier,
    unloadingPorts: List<String>,
    loadingPorts: List<String>
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Порты", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Разгрузка:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            unloadingPorts.forEachIndexed { index, status ->
                Text("  #${index + 1}: $status")
            }
            Spacer(Modifier.height(8.dp))
            Text("Загрузка:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            loadingPorts.forEachIndexed { index, status ->
                Text("  #${index + 1}: $status")
            }
        }
    }
}

/**
 * Панель с логами симуляции.
 */
@Composable
fun LogPanel(logs: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Лог событий", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), reverseLayout = true) {
                items(logs) { log ->
                    Text(log, fontSize = 12.sp)
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}