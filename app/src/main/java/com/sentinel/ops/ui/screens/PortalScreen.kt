package com.sentinel.ops.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinel.ops.data.IncidentCategory
import com.sentinel.ops.ui.theme.HealthyGreen
import com.sentinel.ops.ui.SentinelViewModel
import com.sentinel.ops.ui.components.InfoRow
import com.sentinel.ops.ui.components.SectionHeader
import com.sentinel.ops.ui.components.StatusBadge
import com.sentinel.ops.ui.components.severityColor
import com.sentinel.ops.ui.components.statusColor

@Composable
fun PortalScreen(padding: PaddingValues, vm: SentinelViewModel, onOpenIncident: (String) -> Unit) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Portal Klien", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Lapor masalah dan pantau tiket bantuan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Lapor masalah")
                    Text("Aset", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(vm.assets, key = { it.id }) { asset ->
                            FilterChip(selected = vm.portalAssetId == asset.id, onClick = { vm.portalAssetId = asset.id }, label = { Text(asset.name) })
                        }
                    }
                    Text("Jenis masalah", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(IncidentCategory.CONFIGURATION_ERROR, IncidentCategory.SERVER_DOWN, IncidentCategory.SSL_EXPIRED, IncidentCategory.BRUTE_FORCE), key = { it.name }) { category ->
                            FilterChip(selected = vm.portalCategory == category, onClick = { vm.portalCategory = category }, label = { Text(category.label) })
                        }
                    }
                    OutlinedTextField(vm.portalTitle, { vm.portalTitle = it }, label = { Text("Judul singkat") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(vm.portalDetail, { vm.portalDetail = it }, label = { Text("Penjelasan") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    Text("${vm.portalDetail.length}/30 karakter minimal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (vm.portalFormError != null) Text(vm.portalFormError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { vm.submitPortalTicket() }, modifier = Modifier.fillMaxWidth()) { Text("Kirim laporan") }
                    if (vm.lastTicketNumber != null) {
                        StatusBadge("Tiket ${vm.lastTicketNumber} dibuat", HealthyGreen)
                        Text("Severitas otomatis MEDIUM sesuai aturan portal.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("Tiket klien")
                vm.incidents.filter { it.owner == vm.currentUser?.role?.label || vm.currentUser?.role?.label == "Admin" || vm.currentUser?.role?.label == "Support" }.take(8).forEach { ticket ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(ticket.ticketNumber, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                                StatusBadge(ticket.status.label, statusColor(ticket.status))
                            }
                            Text(ticket.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${ticket.category.label} • ${ticket.assetId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { onOpenIncident(ticket.id) }) { Text("Buka detail") }
                        }
                    }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("Ringkasan akun")
                    InfoRow("Nama", vm.currentUser?.fullName ?: "-")
                    InfoRow("Peran", vm.currentUser?.role?.label ?: "-")
                    InfoRow("Tiket terbuka", vm.activeCount.toString())
                }
            }
        }
    }
}
