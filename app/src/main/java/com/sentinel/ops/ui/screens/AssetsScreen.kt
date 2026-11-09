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
import com.sentinel.ops.data.AssetHealth
import com.sentinel.ops.data.Severity
import com.sentinel.ops.ui.SentinelViewModel
import com.sentinel.ops.ui.components.EmptyState
import com.sentinel.ops.ui.components.InfoRow
import com.sentinel.ops.ui.components.KpiCard
import com.sentinel.ops.ui.components.MetricBar
import com.sentinel.ops.ui.components.SearchField
import com.sentinel.ops.ui.components.SectionHeader
import com.sentinel.ops.ui.components.StatusBadge
import com.sentinel.ops.ui.components.healthColor
import com.sentinel.ops.ui.components.severityColor
import com.sentinel.ops.ui.theme.HealthyGreen
import com.sentinel.ops.ui.theme.WarningAmber

@Composable
fun AssetsScreen(padding: PaddingValues, vm: SentinelViewModel, onOpen: (String) -> Unit, onScan: () -> Unit, onCreate: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Inventaris Aset", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${vm.assets.size} aset • ${vm.healthyAssetCount} sehat • CMDB tersinkron", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onCreate) { Text("Tambah") }
            }
        }
        item { SearchField(vm.assetQuery, { vm.assetQuery = it }, "Cari nama, ID, kategori, atau alamat IP") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Semua", "Perlu atensi", "Sehat")) { filter ->
                    FilterChip(selected = vm.assetFilter == filter, onClick = { vm.assetFilter = filter }, label = { Text(filter) })
                }
            }
        }
        if (vm.filteredAssets.isEmpty()) item { EmptyState("Aset tidak ditemukan", "Coba istilah atau filter lain.") }
        items(vm.filteredAssets, key = { it.id }) { asset ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(asset.id) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row {
                        Text(asset.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                        StatusBadge(asset.health.label, healthColor(asset.health))
                    }
                    Row {
                        Text(asset.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("${asset.openFindings} temuan", style = MaterialTheme.typography.labelMedium, color = if (asset.openFindings > 0) WarningAmber else HealthyGreen)
                    }
                    Text("${asset.category} • ${asset.ipAddress} • ${asset.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider()
                    Text("Uptime ${asset.uptimePercent}% • CPU ${asset.cpuPercent}% • RAM ${asset.memoryPercent}%", style = MaterialTheme.typography.labelSmall)
                    Text("Pemindaian terakhir ${asset.lastScan}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { OutlinedButton(onClick = onScan, modifier = Modifier.fillMaxWidth()) { Text("Jalankan pemindaian inventaris") } }
    }
}

@Composable
fun AssetDetailScreen(padding: PaddingValues, vm: SentinelViewModel, assetId: String, onScan: () -> Unit) {
    val asset = vm.assetById(assetId)
    val relatedIncidents = vm.incidents.filter { it.assetId == assetId }
    val relatedFindings = vm.findings.filter { it.target.contains(asset?.name.orEmpty(), true) || asset?.ipAddress?.let { ip -> it.target.contains(ip) } == true }
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (asset == null) {
            item { EmptyState("Aset tidak ditemukan", "ID tidak tersedia dalam inventaris lokal.") }
            return@LazyColumn
        }
        item {
            Row {
                Text(asset.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                StatusBadge(asset.health.label, healthColor(asset.health))
            }
            Text(asset.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${asset.category} • ${asset.location}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard("Uptime", "${asset.uptimePercent}%", "30 hari terakhir", HealthyGreen, Modifier.weight(1f))
                KpiCard("Temuan", asset.openFindings.toString(), "Belum ditutup", WarningAmber, Modifier.weight(1f))
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("Identitas & konektivitas")
                    InfoRow("Alamat IP", asset.ipAddress, mono = true)
                    InfoRow("Lokasi", asset.location)
                    InfoRow("Kategori", asset.category)
                    InfoRow("Scan terakhir", asset.lastScan)
                    InfoRow("Versi", asset.version.toString(), mono = true)
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Telemetri sumber daya")
                    MetricBar("CPU", asset.cpuPercent)
                    MetricBar("Memori", asset.memoryPercent)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Aksi kesehatan")
                listOf(AssetHealth.HEALTHY, AssetHealth.WARNING, AssetHealth.MAINTENANCE, AssetHealth.DOWN).forEach { health ->
                    FilterChip(selected = asset.health == health, onClick = { vm.updateAssetHealth(asset.id, health) }, label = { Text(health.label) })
                }
            }
        }
        if (relatedFindings.isNotEmpty()) item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("Temuan terkait")
                relatedFindings.take(4).forEach { finding ->
                    Card {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row {
                                Text(finding.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                                StatusBadge(finding.severity.label, severityColor(finding.severity))
                            }
                            Text(finding.title, style = MaterialTheme.typography.bodyMedium)
                            Text(finding.remediation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        if (relatedIncidents.isNotEmpty()) item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("Insiden terkait")
                relatedIncidents.forEach { Text("${it.id} • ${it.status.label} • ${it.severity.label}", style = MaterialTheme.typography.bodySmall) }
            }
        }
        item { OutlinedButton(onClick = onScan, modifier = Modifier.fillMaxWidth()) { Text("Pindai ${asset.name}") } }
    }
}

@Composable
fun AssetFormScreen(padding: PaddingValues, vm: SentinelViewModel, onDone: () -> Unit) {
    Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tambah Aset", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        androidx.compose.material3.OutlinedTextField(vm.assetName, { vm.assetName = it }, label = { Text("Nama aset") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        androidx.compose.material3.OutlinedTextField(vm.assetIp, { vm.assetIp = it }, label = { Text("Alamat IP") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        androidx.compose.material3.OutlinedTextField(vm.assetLocation, { vm.assetLocation = it }, label = { Text("Lokasi") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        androidx.compose.material3.OutlinedTextField(vm.assetCategory, { vm.assetCategory = it }, label = { Text("Kategori") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Text("Kesehatan awal", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AssetHealth.entries.toList()) { health ->
                FilterChip(selected = vm.assetHealth == health, onClick = { vm.assetHealth = health }, label = { Text(health.label) })
            }
        }
        if (vm.assetFormError != null) Text(vm.assetFormError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (vm.createAsset()) onDone() }, modifier = Modifier.weight(1f)) { Text("Simpan aset") }
            OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) { Text("Batal") }
        }
        Text("Validasi mengikuti aturan web: nama unik, IP dan lokasi wajib, serta versi aset bertambah saat perubahan.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
