package com.example.distributioncentersim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.distributioncentersim.models.ProductCategory
import com.example.distributioncentersim.ui.theme.DistributionCenterSimTheme

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

            // Приветственное сообщение, если симуляция не запущена
            AnimatedVisibility(visible = !state.isRunning) {
                WelcomeMessage()
            }

            // Основной дашборд, видимый только во время симуляции
            AnimatedVisibility(visible = state.isRunning) {
                Column {
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
    }
}

/**
 * Карточка с приветствием и инструкцией для пользователя.
 */
@Composable
fun WelcomeMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Info, contentDescription = "Информация", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text("Добро пожаловать!", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Это симулятор работы логистического центра. Нажмите 'Старт', чтобы запустить процесс разгрузки и загрузки грузовиков.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
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
            Button(
                onClick = onStopClick,
                enabled = isRunning,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
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
    Card(modifier = modifier.fillMaxHeight()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warehouse, contentDescription = "Склад", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Склад", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider() // <-- ИЗМЕНЕНИЕ №1
            Spacer(Modifier.height(8.dp))

            if (stock.values.sum() == 0) {
                Text("Пусто", style = MaterialTheme.typography.bodyMedium)
            } else {
                ProductCategory.values().forEach { category ->
                    val count = stock.getOrDefault(category, 0)
                    Text("${category.displayName}: $count шт.")
                }
            }
        }
    }
}

// Вспомогательный класс для информации о статусе порта
private data class PortStatusInfo(val icon: ImageVector, val color: Color)

// Функция для получения иконки и цвета по текстовому статусу
@Composable
private fun getStatusInfo(status: String): PortStatusInfo {
    return when {
        status.startsWith("Свободен") -> PortStatusInfo(Icons.Rounded.CheckCircle, MaterialTheme.colorScheme.primary)
        status.startsWith("Ожидает") -> PortStatusInfo(Icons.Rounded.HourglassTop, Color(0xFFFFA000)) // Amber
        status.startsWith("Ошибка") -> PortStatusInfo(Icons.Filled.Warning, MaterialTheme.colorScheme.error)
        else -> PortStatusInfo(Icons.Rounded.Download, MaterialTheme.colorScheme.secondary) // In progress
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
    Card(modifier = modifier.fillMaxHeight()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок для портов разгрузки
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Download, contentDescription = "Разгрузка", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Порты разгрузки", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            unloadingPorts.forEachIndexed { index, status ->
                val statusInfo = getStatusInfo(status)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusInfo.icon, contentDescription = status, tint = statusInfo.color, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "#${index + 1}: $status", fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Заголовок для портов загрузки
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Upload, contentDescription = "Загрузка", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Порты загрузки", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            loadingPorts.forEachIndexed { index, status ->
                val statusInfo = getStatusInfo(status)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusInfo.icon, contentDescription = status, tint = statusInfo.color, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "#${index + 1}: $status", fontSize = 14.sp)
                }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Лог", tint = MaterialTheme.colorScheme.primary) // <-- ИЗМЕНЕНИЕ №2
            Spacer(Modifier.width(8.dp))
            Text("Лог событий", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxSize()) {
            if (logs.isEmpty()){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text("Лог событий пуст.")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), reverseLayout = true) {
                    items(logs) { log ->
                        Text(log, fontSize = 12.sp, lineHeight = 16.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) // <-- ИЗМЕНЕНИЕ №3
                    }
                }
            }
        }
    }
}