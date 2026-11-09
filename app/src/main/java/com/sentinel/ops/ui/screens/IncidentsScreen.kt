package com.sentinel.ops.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import com.sentinel.ops.data.nextStatuses
import com.sentinel.ops.ui.SentinelViewModel
import com.sentinel.ops.ui.components.EmptyState
import com.sentinel.ops.ui.components.InfoRow
import com.sentinel.ops.ui.components.SearchField
import com.sentinel.ops.ui.components.SectionHeader
import com.sentinel.ops.ui.components.SlaIndicator
import com.sentinel.ops.ui.components.StatusBadge
import com.sentinel.ops.ui.components.severityColor
import com.sentinel.ops.ui.components.statusColor

@Composable
fun IncidentsScreen(padding: PaddingValues, vm: SentinelViewModel, onOpen: (String) -> Unit) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Insiden & SLA", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${vm.activeCount} aktif • ${vm.slaRiskCount} berisiko melanggar SLA", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        item { SearchField(vm.incidentQuery, { vm.incidentQuery = it }, "Cari ID, tiket, judul, petugas, atau aset") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Aktif", "Semua", "Kritis", "SLA", "Selesai", "Ditutup")) { filter ->
                    FilterChip(selected = vm.incidentFilter == filter, onClick = { vm.incidentFilter = filter }, label = { Text(filter) })
                }
            }
        }
        if (vm.filteredIncidents.isEmpty()) item { EmptyState("Tidak ada insiden", "Ubah kata pencarian atau filter status.") }
        items(vm.filteredIncidents, key = { it.id }) { incident ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(incident.id) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row {
                        Text("${incident.id} / ${incident.ticketNumber}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        StatusBadge(incident.status.label, statusColor(incident.status))
                    }
                    Row {
                        Text(incident.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        StatusBadge(incident.severity.label, severityColor(incident.severity))
                    }
                    SlaIndicator(incident.slaMinutesLeft, incident.slaResponseTarget, incident.status == IncidentStatus.RESOLVED || incident.status == IncidentStatus.CLOSED)
                    HorizontalDivider()
                    Text("${incident.category.label} • ${incident.assetId} • ${incident.assignee}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Dibuat ${incident.createdAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun IncidentDetailScreen(padding: PaddingValues, vm: SentinelViewModel, incidentId: String, onAsset: (String) -> Unit) {
    val incident = vm.incidentById(incidentId)
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (incident == null) {
            item { EmptyState("Insiden tidak ditemukan", "Data mungkin sudah disegarkan.") }
            return@LazyColumn
        }
        item {
            Row {
                Text("${incident.id} / ${incident.ticketNumber}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                StatusBadge(incident.severity.label, severityColor(incident.severity))
            }
            Text(incident.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            StatusBadge(incident.status.label, statusColor(incident.status))
        }
        item { Card { Column(Modifier.padding(16.dp)) { SlaIndicator(incident.slaMinutesLeft, incident.slaResponseTarget, incident.status == IncidentStatus.RESOLVED || incident.status == IncidentStatus.CLOSED) } } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.escalate(incident.id) }, modifier = Modifier.weight(1f)) { Text("Eskalasi") }
                incident.status.nextStatuses().take(2).forEach { status ->
                    Button(onClick = { vm.changeIncidentStatus(incident.id, status) }, modifier = Modifier.weight(1f)) { Text(status.label) }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Ringkasan kejadian")
                    Text(incident.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("Detail operasional")
                    InfoRow("Kategori", incident.category.label)
                    InfoRow("Aset", incident.assetId, mono = true)
                    InfoRow("Tim", incident.owner)
                    InfoRow("Petugas", incident.assignee)
                    InfoRow("Dibuat", incident.createdAt)
                    InfoRow("Versi", incident.version.toString(), mono = true)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Linimasa aktivitas")
                incident.timeline.asReversed().forEach { entry ->
                    Card {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("${entry.time} • ${entry.actor}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(entry.action, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        item { OutlinedButton(onClick = { onAsset(incident.assetId) }, modifier = Modifier.fillMaxWidth()) { Text("Buka aset terkait ${incident.assetId}") } }
    }
}
