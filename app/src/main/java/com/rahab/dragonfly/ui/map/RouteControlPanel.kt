package com.rahab.dragonfly.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rahab.dragonfly.ui.theme.Coral500
import com.rahab.dragonfly.ui.theme.Ink600
import com.rahab.dragonfly.ui.theme.Ink900
import com.rahab.dragonfly.ui.theme.Mist100
import com.rahab.dragonfly.ui.theme.SuccessGreen
import com.rahab.dragonfly.ui.theme.Teal700
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 底部轨迹控制面板。
 *
 * 包含：轨迹统计（里程/配速/预估用时）、日期选择、配速滑杆、撤销/清空/保存/路线、
 * 以及跑步打卡按钮。
 *
 * @author Rahab
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteControlPanel(
    state: MapUiState,
    viewModel: MapViewModel,
) {
    val savedRoutes by viewModel.savedRoutes.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showRoutesDialog by remember { mutableStateOf(false) }

    val distanceKm = viewModel.totalDistanceMeters / 1000.0
    val estimatedSeconds = viewModel.estimatedSeconds

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White,
                shadowElevation = 16.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    // 轨迹统计
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatItem(label = "里程", value = "%.2f km".format(distanceKm))
                        StatItem(label = "配速", value = "%.0f'00\"/km".format(state.paceMinPerKm))
                        StatItem(label = "预估用时", value = formatDuration(estimatedSeconds))
                    }

                    // 里程不足提示。
                    val minKm = state.minDistanceM?.toDouble()?.div(1000.0)
                    AnimatedVisibility(
                        visible = minKm != null && distanceKm < minKm,
                    ) {
                        Text(
                            text = "当前里程低于规则最低要求 %.1f km，成绩可能无效".format(minKm ?: 0.0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Coral500,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // 日期选择
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Mist100, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, null, tint = Teal700)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "跑步日期",
                                style = MaterialTheme.typography.labelSmall,
                                color = Ink600,
                            )
                            Text(
                                text = formatDate(state.dateMillis),
                                style = MaterialTheme.typography.titleMedium,
                                color = Ink900,
                            )
                        }
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("选择", color = Teal700)
                        }
                    }

                    // 配速滑杆
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "配速设置",
                                style = MaterialTheme.typography.labelLarge,
                                color = Ink900,
                            )
                            Text(
                                text = "%.0f min/km".format(state.paceMinPerKm),
                                style = MaterialTheme.typography.labelLarge,
                                color = Teal700,
                            )
                        }
                        Slider(
                            value = state.paceMinPerKm,
                            onValueChange = viewModel::onPaceChange,
                            valueRange = 2f..10f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = Teal700,
                                activeTrackColor = Teal700,
                                inactiveTrackColor = Mist100,
                            ),
                        )
                    }

                    // 操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ActionButton(
                            icon = Icons.Outlined.Undo,
                            label = "撤销",
                            enabled = state.points.isNotEmpty(),
                            onClick = viewModel::undoLastPoint,
                            modifier = Modifier.weight(1f),
                        )
                        ActionButton(
                            icon = Icons.Outlined.Refresh,
                            label = "清空",
                            enabled = state.points.isNotEmpty(),
                            onClick = viewModel::clearPoints,
                            modifier = Modifier.weight(1f),
                        )
                        ActionButton(
                            icon = Icons.Outlined.Save,
                            label = "保存",
                            enabled = state.points.size >= 2,
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.weight(1f),
                        )
                        ActionButton(
                            icon = Icons.Outlined.FolderOpen,
                            label = "路线",
                            enabled = true,
                            onClick = { showRoutesDialog = true },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Button(
                        onClick = viewModel::upload,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = state.points.size >= 2 && !state.isUploading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Teal700,
                            contentColor = Color.White,
                        ),
                    ) {
                        if (state.isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("上传中…", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Outlined.DirectionsRun, null)
                            Spacer(Modifier.width(8.dp))
                            Text("跑步打卡", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // 上传结果提示。
                    AnimatedVisibility(visible = state.uploadMessage != null) {
                        Text(
                            text = state.uploadMessage.orEmpty(),
                            color = if (state.uploadSuccess == true) SuccessGreen
                            else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    // 日期选择对话框。
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(viewModel::onDateChange)
                        showDatePicker = false
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 保存路线对话框。
    if (showSaveDialog) {
        var routeName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("保存路线") },
            text = {
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("路线名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveRoute(routeName)
                        showSaveDialog = false
                    },
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("取消") }
            },
        )
    }

    // 已保存路线对话框。
    if (showRoutesDialog) {
        AlertDialog(
            onDismissRequest = { showRoutesDialog = false },
            title = { Text("已保存路线") },
            text = {
                if (savedRoutes.isEmpty()) {
                    Text("暂无保存的路线", color = Ink600)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        savedRoutes.forEach { route ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = route.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = {
                                    viewModel.loadRoute(route)
                                    showRoutesDialog = false
                                }) { Text("应用", color = Teal700) }
                                TextButton(onClick = { viewModel.deleteRoute(route.name) }) {
                                    Text("删除", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoutesDialog = false }) { Text("关闭") }
            },
        )
    }
}

/** 小操作按钮（图标 + 文字）。 */
@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Icon(icon, null, Modifier.width(16.dp))
        Spacer(Modifier.width(2.dp))
        Text(label, fontSize = 12.sp)
    }
}

/** 统计项。 */
@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Teal700,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Ink600)
    }
}

/** 格式化日期为 yyyy-MM-dd。 */
private fun formatDate(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))

/** 格式化秒数为 Xh Ym 或 Ym Zs。 */
private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return when {
        h > 0 -> "%dh %02dm".format(h, m)
        m > 0 -> "%d'%02d\"".format(m, s)
        else -> "%ds".format(s)
    }
}
