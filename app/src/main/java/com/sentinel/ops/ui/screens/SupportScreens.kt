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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinel.ops.data.UserRole
import com.sentinel.ops.ui.SentinelViewModel
import com.sentinel.ops.ui.components.InfoRow
import com.sentinel.ops.ui.components.SearchField
import com.sentinel.ops.ui.components.SectionHeader
import com.sentinel.ops.ui.components.StatusBadge
import com.sentinel.ops.ui.components.severityColor
import com.sentinel.ops.ui.theme.HealthyGreen

@Composable
fun MoreScreen(
    padding: PaddingValues,
    vm: SentinelViewModel,
    onDss: () -> Unit,
    onPortal: () -> Unit,
    onAudit: () -> Unit,
    onRunbooks: () -> Unit,
    onVault: () -> Unit,
    onStatus: () -> Unit,
    onReport: () -> Unit,
    onSettings: () -> Unit,
) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Ruang Kerja", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Semua modul pendukung operasi dan akun operator.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("Operator", vm.currentUser?.fullName ?: "-")
                    InfoRow("Peran", vm.currentUser?.role?.label ?: "-")
                    InfoRow("Shift", "Pagi • 07:00–15:00")
                    InfoRow("Koneksi", "Terenkripsi • tersinkron")
                    InfoRow("Audit sensitif", vm.sensitiveAuditCount.toString(), mono = true)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("Modul")
                MoreMenuItem("Portal klien", "Lapor masalah dan pantau tiket", onPortal)
                MoreMenuItem("Runbook respons", "${vm.runbooks.size} prosedur operasional", onRunbooks)
                MoreMenuItem("Status layanan", "Halaman publik untuk klien", onStatus)
                if (vm.currentUser?.role in listOf(UserRole.ADMIN, UserRole.SECOPS)) {
                    MoreMenuItem("Dukungan keputusan", "Rekomendasi & matriks risiko", onDss)
                    MoreMenuItem("Jejak audit", "Riwayat aksi operator dan sistem", onAudit)
                    MoreMenuItem("Brankas kredensial", "TOTP dan akses darurat", onVault)
                    MoreMenuItem("Laporan & ekspor", "Pratinjau dan ekspor CSV", onReport)
                }
                MoreMenuItem("Pengaturan", "Server, sinkronisasi, dan keamanan", onSettings)
            }
        }
    }
}

@Composable
private fun MoreMenuItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun NotificationsScreen(padding: PaddingValues, vm: SentinelViewModel) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Notifikasi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${vm.unreadCount} belum dibaca", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = { vm.markAllRead() }) { Text("Tandai semua") }
            }
        }
        items(vm.notifications, key = { it.id }) { notification ->
            Card(modifier = Modifier.fillMaxWidth().clickable { vm.readNotification(notification) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        StatusBadge(if (notification.read) "Dibaca" else "Baru", if (notification.read) MaterialTheme.colorScheme.onSurfaceVariant else severityColor(notification.severity))
                        Text(notification.time, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(notification.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(notification.body, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun RunbookScreen(padding: PaddingValues, vm: SentinelViewModel) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Runbook Respons", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Prosedur terstandardisasi untuk triage dan pemulihan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(vm.runbooks, key = { it.code }) { runbook ->
            val expanded = vm.selectedRunbook == runbook.code
            Card(modifier = Modifier.fillMaxWidth().clickable { vm.selectRunbook(if (expanded) null else runbook.code) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(runbook.code, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(runbook.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${runbook.category} • ${runbook.owner} • Diperbarui ${runbook.updatedAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (expanded) {
                        HorizontalDivider()
                        runbook.steps.forEachIndexed { index, step -> Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }
    }
}

@Composable
fun VaultScreen(padding: PaddingValues, vm: SentinelViewModel) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Brankas Kredensial", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Akses darurat dan TOTP tercatat pada jejak audit.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = if (vm.vaultUnlocked) HealthyGreen else MaterialTheme.colorScheme.primary)
                    Text(if (vm.vaultUnlocked) "Brankas terbuka untuk sesi ini" else "Brankas terkunci", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { vm.toggleVault() }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (vm.vaultUnlocked) "Kunci kembali" else "Buka brankas")
                    }
                }
            }
        }
        if (vm.vaultUnlocked) items(vm.vault, key = { it.service }) { entry ->
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(entry.service, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(entry.account, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    Row {
                        Text("482 913", style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        Text("24 detik", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Rotasi ${entry.lastRotated} • diakses ${entry.accessCount}x", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { vm.accessVaultEntry(entry.service) }, modifier = Modifier.fillMaxWidth()) { Text("Catat akses") }
                }
            }
        }
    }
}

@Composable
fun AuditScreen(padding: PaddingValues, vm: SentinelViewModel) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Jejak Audit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${vm.auditLogs.size} entri • ${vm.sensitiveAuditCount} sensitif", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { SearchField(vm.auditQuery, { vm.auditQuery = it }, "Cari aktor, aksi, atau detail") }
        items(vm.filteredAudit, key = { it.id }) { audit ->
            Card {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        Text(audit.action, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                        StatusBadge(if (audit.sensitive) "Sensitif" else "Normal", if (audit.sensitive) MaterialTheme.colorScheme.error else HealthyGreen)
                    }
                    Text(audit.detail, style = MaterialTheme.typography.bodyMedium)
                    Text("${audit.time} • ${audit.actor}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(padding: PaddingValues, vm: SentinelViewModel) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Pengaturan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("Koneksi Sentinel Ops")
                    InfoRow("Server", "https://ops.sentinel.local", mono = true)
                    InfoRow("Organisasi", "Sentinel Command")
                    InfoRow("Mode data", "Offline seed / demo")
                    InfoRow("Versi", "1.0.0 (1)")
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Operasional")
                    SettingToggle("Sinkronisasi otomatis", "Segarkan data setiap 5 menit", vm.autoSync) { vm.autoSync = it }
                    SettingToggle("Notifikasi push", "SLA kritis dan insiden baru", vm.pushEnabled) { vm.pushEnabled = it }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Target SLA")
                    vm.slaTargets.forEach { InfoRow(it.severity.label, "Respons ${it.responseMinutes}m • Selesai ${it.resolveMinutes}m") }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Ekspor & laporan")
                    Button(onClick = { vm.exportCsv("incidents") }, modifier = Modifier.fillMaxWidth()) { Text("Ekspor insiden CSV") }
                    OutlinedButton(onClick = { vm.exportCsv("assets") }, modifier = Modifier.fillMaxWidth()) { Text("Ekspor aset CSV") }
                    OutlinedButton(onClick = { vm.exportCsv("audit") }, modifier = Modifier.fillMaxWidth()) { Text("Ekspor audit CSV") }
                }
            }
        }
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
