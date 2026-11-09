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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinel.ops.data.SampleData
import com.sentinel.ops.ui.SentinelViewModel
import com.sentinel.ops.ui.components.InfoRow
import com.sentinel.ops.ui.components.SectionHeader
import com.sentinel.ops.ui.components.StatusBadge
import com.sentinel.ops.ui.components.healthColor
import com.sentinel.ops.ui.components.severityColor

@Composable
fun ScannerScreen(padding: PaddingValues, vm: SentinelViewModel) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Pemindai Keamanan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Analisis manual dan simulasi skenario seperti versi web.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Jalankan pemindaian")
                    InfoRow("Target", SampleData.scenarios.first { it.id == vm.scanScenarioId }.target, mono = true)
                    InfoRow("Aturan aktif", "${vm.activeRuleCount} dari ${SampleData.rules.size}")
                    Text("Skenario", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SampleData.scenarios, key = { it.id }) { scenario ->
                            FilterChip(selected = vm.scanScenarioId == scenario.id, onClick = { vm.scanScenarioId = scenario.id }, label = { Text(scenario.label) })
                        }
                    }
                    Text("Preview payload", style = MaterialTheme.typography.labelLarge)
                    Text(SampleData.scenarios.first { it.id == vm.scanScenarioId }.preview, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    if (vm.scanRunning) LinearProgressIndicator(progress = { vm.scanProgress }, modifier = Modifier.fillMaxWidth())
                    Text(vm.scanMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { vm.startScan() }, enabled = !vm.scanRunning, modifier = Modifier.fillMaxWidth()) {
                        Text(if (vm.scanRunning) "Memindai ${(vm.scanProgress * 100).toInt()}%" else "Mulai pemindaian")
                    }
                    if (vm.scanResult != null) {
                        val result = vm.scanResult!!
                        HorizontalDivider()
                        Row {
                            Text("HTTP ${result.httpStatus}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            StatusBadge(result.health.label, healthColor(result.health))
                        }
                        Text(result.summary, style = MaterialTheme.typography.bodyMedium)
                        Text("Temuan tercatat: ${result.findingCount}", style = MaterialTheme.typography.labelMedium)
                        if (result.incidentId != null) OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Auto-incident ${result.incidentId}") }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Kamus deteksi")
                vm.rules.forEach { rule ->
                    Card {
                        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(rule.code, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium)
                                Text(rule.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(rule.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(rule.severity.label, severityColor(rule.severity))
                            Switch(checked = rule.enabled, onCheckedChange = { vm.toggleRule(rule.code) })
                        }
                    }
                }
            }
        }
        item { HorizontalDivider(); SectionHeader("Hasil pemindaian terbaru", "${vm.findings.size} temuan") {} }
        items(vm.findings, key = { it.id }) { finding ->
            val expanded = vm.expandedFindingId == finding.id
            Card(modifier = Modifier.fillMaxWidth().clickable { vm.expandedFindingId = if (expanded) null else finding.id }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Text(finding.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                        StatusBadge(finding.severity.label, severityColor(finding.severity))
                    }
                    Text(finding.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("${finding.category} • ${finding.target} • ${finding.detectedAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (expanded) {
                        HorizontalDivider()
                        Text(finding.detail, style = MaterialTheme.typography.bodyMedium)
                        Text("Rekomendasi perbaikan", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(finding.remediation, style = MaterialTheme.typography.bodySmall)
                        FilterChip(selected = false, onClick = { }, label = { Text("Buat tiket insiden") })
                    }
                }
            }
        }
    }
}
