package com.sentinel.ops.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinel.ops.ui.SentinelViewModel
import com.sentinel.ops.ui.components.EmptyState
import com.sentinel.ops.ui.components.InfoRow
import com.sentinel.ops.ui.components.MetricBar
import com.sentinel.ops.ui.components.SectionHeader
import com.sentinel.ops.ui.components.StatusBadge
import com.sentinel.ops.ui.components.severityColor
import com.sentinel.ops.ui.components.statusColor
import com.sentinel.ops.ui.theme.HealthyGreen
import com.sentinel.ops.ui.theme.WarningAmber

@Composable
fun StatusScreen(padding: PaddingValues, vm: SentinelViewModel, onOpen: (String) -> Unit) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Status Layanan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Ringkasan publik untuk klien dan pemangku kepentingan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(vm.statusServices, key = { it.slug }) { service ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(service.slug) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Text(service.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        StatusBadge(service.status, if (service.status == "Operasional") HealthyGreen else WarningAmber)
                    }
                    Text("/status/${service.slug}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Uptime ${service.uptime}%", style = MaterialTheme.typography.labelMedium)
                    Text(service.note, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Bagikan tautan status") }
                }
            }
        }
    }
}

@Composable
fun StatusDetailScreen(padding: PaddingValues, vm: SentinelViewModel, slug: String) {
    val service = vm.statusBySlug(slug)
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (service == null) {
            item { EmptyState("Halaman status tidak ditemukan", "Slug tidak terdaftar pada mode demo.") }
            return@LazyColumn
        }
        item {
            Text(service.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("/status/${service.slug}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StatusBadge(service.status, if (service.status == "Operasional") HealthyGreen else WarningAmber)
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Ringkasan ketersediaan")
                    InfoRow("Uptime", "${service.uptime}%")
                    InfoRow("Status", service.status)
                    InfoRow("Catatan", service.note)
                    MetricBar("Uptime 30 hari", service.uptime.toInt())
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Insiden berkaitan")
                    vm.incidents.filter { it.title.contains(service.name, true) || it.category.label.contains("Server", true) }.take(5).forEach { incident ->
                        Text("${incident.id} • ${incident.status.label} • ${incident.severity.label}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (vm.incidents.none { it.title.contains(service.name, true) }) {
                        Text("Tidak ada insiden terbuka pada 30 hari terakhir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportScreen(padding: PaddingValues, vm: SentinelViewModel) {
    val incident = vm.incidents.firstOrNull()
    val asset = vm.assets.firstOrNull()
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Laporan & Ekspor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        if (incident != null) item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Laporan insiden ${incident.id}")
                    InfoRow("Tiket", incident.ticketNumber, mono = true)
                    InfoRow("Severitas", incident.severity.label)
                    InfoRow("Status", incident.status.label)
                    InfoRow("Aset", incident.assetId, mono = true)
                    InfoRow("Dibuat", incident.createdAt)
                    Text(incident.description, style = MaterialTheme.typography.bodySmall)
                    Row {
                        Button(onClick = { vm.exportCsv("incidents") }, modifier = Modifier.weight(1f)) { Text("Ekspor CSV") }
                    }
                }
            }
        }
        if (asset != null) item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Laporan aset ${asset.id}")
                    InfoRow("Nama", asset.name)
                    InfoRow("Kesehatan", asset.health.label)
                    InfoRow("IP", asset.ipAddress, mono = true)
                    InfoRow("Temuan", asset.openFindings.toString(), mono = true)
                    Row {
                        Button(onClick = { vm.exportCsv("assets") }, modifier = Modifier.weight(1f)) { Text("Ekspor CSV") }
                    }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Riwayat audit")
                    InfoRow("Entri", vm.auditLogs.size.toString(), mono = true)
                    InfoRow("Sensitif", vm.sensitiveAuditCount.toString(), mono = true)
                    Button(onClick = { vm.exportCsv("audit") }, modifier = Modifier.fillMaxWidth()) { Text("Ekspor audit") }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Snapshot operasional")
                    vm.incidents.take(3).forEach {
                        Row {
                            Text(it.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                            StatusBadge(it.severity.label, severityColor(it.severity))
                            Text(it.status.label, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelSmall, color = statusColor(it.status))
                        }
                    }
                }
            }
        }
    }
}
