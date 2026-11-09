package com.sentinel.ops.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sentinel.ops.data.AssetHealth
import com.sentinel.ops.data.IncidentStatus
import com.sentinel.ops.data.Severity
import com.sentinel.ops.ui.theme.CriticalRed
import com.sentinel.ops.ui.theme.HealthyGreen
import com.sentinel.ops.ui.theme.SentinelBlue
import com.sentinel.ops.ui.theme.SentinelTeal
import com.sentinel.ops.ui.theme.WarningAmber

@Composable
fun severityColor(severity: Severity) = when (severity) {
    Severity.CRITICAL -> CriticalRed
    Severity.HIGH -> Color(0xFFFF9466)
    Severity.MEDIUM -> WarningAmber
    Severity.LOW -> SentinelBlue
}

@Composable
fun healthColor(health: AssetHealth) = when (health) {
    AssetHealth.HEALTHY -> HealthyGreen
    AssetHealth.WARNING, AssetHealth.MAINTENANCE -> WarningAmber
    AssetHealth.COMPROMISED, AssetHealth.DOWN -> CriticalRed
}

@Composable
fun statusColor(status: IncidentStatus) = when (status) {
    IncidentStatus.OPEN -> CriticalRed
    IncidentStatus.INVESTIGATING -> WarningAmber
    IncidentStatus.CONTAINED -> SentinelBlue
    IncidentStatus.RESOLVED -> HealthyGreen
    IncidentStatus.CLOSED -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun StatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun KpiCard(label: String, value: String, note: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(action, modifier = Modifier.clickable(onClick = onAction), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, mono: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.width(124.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default)
    }
}

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
fun SlaIndicator(minutesLeft: Int, totalMinutes: Int, resolved: Boolean = false) {
    val color = when {
        resolved -> HealthyGreen
        minutesLeft <= 15 -> CriticalRed
        minutesLeft <= 60 -> WarningAmber
        else -> SentinelTeal
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SLA respons", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            Text(if (resolved) "Selesai" else "${minutesLeft}m tersisa", color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))) {
            val fraction = if (resolved || totalMinutes == 0) 1f else (minutesLeft.toFloat() / totalMinutes).coerceIn(0f, 1f)
            Box(modifier = Modifier.fillMaxWidth(fraction).height(6.dp).background(color, RoundedCornerShape(3.dp)))
        }
    }
}

@Composable
fun MetricBar(label: String, value: Int, max: Int = 100) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            Text("$value%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { value / max.toFloat() }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun MiniBarChart(values: List<Int>, labels: List<String>) {
    val barColor = SentinelTeal
    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
            val slot = size.width / values.size
            values.forEachIndexed { index, value ->
                val barHeight = (size.height - 18f) * value / maxValue
                drawRect(
                    color = barColor,
                    topLeft = Offset(index * slot + slot * 0.25f, size.height - barHeight),
                    size = Size(slot * 0.5f, barHeight),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            labels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
fun EmptyState(title: String, detail: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.size(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
