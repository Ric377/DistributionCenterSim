package com.example.distributioncentersim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.distributioncentersim.models.ProductCategory
import com.example.distributioncentersim.ui.theme.DistributionCenterSimTheme
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(
    state: SimulationState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Scaffold(
        topBar = { /* ... */ }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ControlPanel(
                isRunning = state.isRunning,
                onStartClick = onStartClick,
                onStopClick = onStopClick
            )
            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = !state.isRunning) {
                WelcomeMessage()
            }

            AnimatedVisibility(visible = state.isRunning) {
                Column(modifier = Modifier.fillMaxSize()) {
                    WarehouseStatus(stock = state.warehouseStock, modifier = Modifier.weight(0.3f))
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(0.3f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PortsColumn(
                            modifier = Modifier.weight(2f),
                            title = "Порты разгрузки",
                            icon = Icons.Rounded.Download,
                            ports = state.unloadingPortsStatus
                        )
                        PortsColumn(
                            modifier = Modifier.weight(3f),
                            title = "Порты загрузки",
                            icon = Icons.Rounded.Upload,
                            ports = state.loadingPortsStatus
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    LogPanel(logs = state.logMessages, modifier = Modifier.weight(0.4f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WarehouseStatus(modifier: Modifier = Modifier, stock: Map<ProductCategory, Int>) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warehouse, contentDescription = "Склад", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Склад", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (stock.values.sum() == 0) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Пусто", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProductCategory.values().forEach { category ->
                            val count = stock.getOrDefault(category, 0)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${category.displayName}:",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "$count шт.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LogPanel(logs: List<String>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(logs) {
        // Проверяем, находится ли пользователь в самом низу списка
        // (для reverseLayout низ - это первый видимый элемент, то есть индекс 0)
        val isAtBottom = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0

        // Если пользователь внизу, плавно скроллим к новому элементу
        if (isAtBottom) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Лог", tint = MaterialTheme.colorScheme.primary)
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
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    reverseLayout = true,
                    state = listState
                ) {
                    items(logs) { log ->
                        Text(log, fontSize = 12.sp, lineHeight = 16.sp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PortsColumn(modifier: Modifier = Modifier, title: String, icon: ImageVector, ports: List<String>) {
    Card(modifier = modifier.fillMaxHeight()) {
        Column(modifier = Modifier.padding(12.dp).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ports.forEachIndexed { index, status ->
                    PortStatusRow(portIndex = index + 1, status = status)
                }
            }
        }
    }
}
@Composable
private fun PortStatusRow(portIndex: Int, status: String) {
    val statusInfo = getStatusInfo(status)
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(statusInfo.isProcessing) {
        if (statusInfo.isProcessing) { alpha.animateTo(0.3f, animationSpec = repeatable(Int.MAX_VALUE, tween(700, easing = LinearEasing), initialStartOffset = androidx.compose.animation.core.StartOffset(portIndex * 100))) }
        else { alpha.snapTo(1f) }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = statusInfo.icon, contentDescription = status, tint = statusInfo.color,
            modifier = Modifier.size(14.dp).alpha(if (statusInfo.isProcessing) alpha.value else 1f)
        )
        Spacer(Modifier.width(4.dp))
        Text(text = "#$portIndex: $status", fontSize = 12.sp)
    }
}
@Composable
fun WelcomeMessage() {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Info, contentDescription = "Информация", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text("Добро пожаловать!", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(text = "Это симулятор работы логистического центра. Нажмите 'Старт', чтобы запустить процесс разгрузки и загрузки грузовиков.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
@Composable
fun ControlPanel(isRunning: Boolean, onStartClick: () -> Unit, onStopClick: () -> Unit) {
    Card {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onStartClick, enabled = !isRunning, modifier = Modifier.weight(1f)) { Text("Старт") }
            Button(onClick = onStopClick, enabled = isRunning, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Стоп") }
        }
    }
}
private data class PortStatusInfo(val icon: ImageVector, val color: Color, val isProcessing: Boolean)
@Composable
private fun getStatusInfo(status: String): PortStatusInfo {
    return when {
        status.startsWith("Свободен") -> PortStatusInfo(Icons.Rounded.CheckCircle, MaterialTheme.colorScheme.primary, false)
        status.startsWith("Ожидает") -> PortStatusInfo(Icons.Rounded.HourglassTop, Color(0xFFFFA000), false)
        status.startsWith("Ошибка") -> PortStatusInfo(Icons.Filled.Warning, MaterialTheme.colorScheme.error, false)
        else -> PortStatusInfo(Icons.Rounded.Sync, MaterialTheme.colorScheme.secondary, true)
    }
}