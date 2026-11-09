package com.sentinel.ops.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinel.ops.data.IncidentStatus
import com.sentinel.ops.ui.SentinelViewModel
import com.sentinel.ops.ui.components.KpiCard
import com.sentinel.ops.ui.components.MiniBarChart
import com.sentinel.ops.ui.components.SectionHeader
import com.sentinel.ops.ui.components.SlaIndicator
import com.sentinel.ops.ui.components.StatusBadge
import com.sentinel.ops.ui.components.healthColor
import com.sentinel.ops.ui.components.severityColor
import com.sentinel.ops.ui.components.statusColor
import com.sentinel.ops.ui.theme.CriticalRed
import com.sentinel.ops.ui.theme.HealthyGreen
import com.sentinel.ops.ui.theme.SentinelBlue
import com.sentinel.ops.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    vm: SentinelViewModel,
    onIncident: (String) -> Unit,
    onIncidents: () -> Unit,
    onScanner: () -> Unit,
    onDss: () -> Unit,
) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Dasbor Komando", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Minggu, 8 Nov • Shift Pagi • ${vm.currentUser?.fullName ?: "Operator"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                StatusBadge("Sinkron lokal aktif • audit ${vm.auditLogs.size}", HealthyGreen)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard("Insiden aktif", vm.activeCount.toString(), "${vm.slaRiskCount} mendekati SLA", CriticalRed, Modifier.weight(1f))
                    KpiCard("Prioritas kritis", vm.criticalCount.toString(), "P1 perlu respons", WarningAmber, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard("Aset sehat", "${vm.healthyAssetCount}/${vm.assets.size}", "Inventaris termonitor", HealthyGreen, Modifier.weight(1f))
                    KpiCard("Temuan", vm.findings.size.toString(), "${vm.activeRuleCount} aturan aktif", SentinelBlue, Modifier.weight(1f))
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Tren insiden 7 hari")
                    MiniBarChart(listOf(3, 5, 2, 6, 4, 7, 4), listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"))
                    Text("28 total • −18% dibanding pekan lalu", style = MaterialTheme.typography.labelMedium, color = HealthyGreen)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Antrean prioritas", "Semua", onIncidents)
                vm.incidents.filter { it.status != IncidentStatus.RESOLVED && it.status != IncidentStatus.CLOSED }.take(3).forEach { incident ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onIncident(incident.id) }) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Row {
                                Text(incident.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                                StatusBadge(incident.severity.label, severityColor(incident.severity))
                            }
                            Text(incident.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            SlaIndicator(incident.slaMinutesLeft, incident.slaResponseTarget)
                            Text("${incident.assetId} • ${incident.assignee}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.clickable(onClick = onDss)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Rekomendasi DSS")
                    vm.decisions.take(2).forEach {
                        Text("${it.title}: ${it.recommendation}", style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = onDss, modifier = Modifier.fillMaxWidth()) { Text("Buka dukungan keputusan") }
                }
            }
        }
        item {
            Card(modifier = Modifier.clickable(onClick = onScanner)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Postur keamanan", "Pindai", onScanner)
                    Text("Skor keamanan 82/100", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("1 kritis • 2 tinggi • rotasi TLS perlu dijadwalkan", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("Status aset")
                vm.assets.take(4).forEach { asset ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        StatusBadge(asset.health.label, healthColor(asset.health))
                        Text(asset.name, modifier = Modifier.padding(start = 10.dp).weight(1f), style = MaterialTheme.typography.bodySmall)
                        Text("${asset.uptimePercent}%", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        item {
            HorizontalDivider()
            Text("Telemetry diperbarui 20 detik lalu • Sentinel Ops Mobile 1.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
