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
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinel.ops.data.SampleData
import com.sentinel.ops.data.Severity
import com.sentinel.ops.ui.SentinelViewModel
import com.sentinel.ops.ui.components.InfoRow
import com.sentinel.ops.ui.components.MetricBar
import com.sentinel.ops.ui.components.SectionHeader
import com.sentinel.ops.ui.components.StatusBadge
import com.sentinel.ops.ui.components.severityColor
import com.sentinel.ops.ui.theme.CriticalRed
import com.sentinel.ops.ui.theme.HealthyGreen
import com.sentinel.ops.ui.theme.SentinelTeal
import com.sentinel.ops.ui.theme.WarningAmber

@Composable
fun DssScreen(padding: PaddingValues, vm: SentinelViewModel) {
    LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Dukungan Keputusan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Rekomendasi otomatis berbasis risiko, SLA, dan hasil pemindaian.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                com.sentinel.ops.ui.components.KpiCard("Skor risiko", SampleData.risks.sumOf { vm.riskScore(it) }.toString(), "Total tertimbang", CriticalRed, Modifier.weight(1f))
                com.sentinel.ops.ui.components.KpiCard("Kepercayaan", "${vm.decisions.first().confidence}%", "Rekomendasi utama", SentinelTeal, Modifier.weight(1f))
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Rekomendasi")
                vm.decisions.forEach { decision ->
                    Card {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(decision.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(decision.recommendation, style = MaterialTheme.typography.bodyMedium)
                            Text("Dampak: ${decision.impact}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            MetricBar("Kepercayaan", decision.confidence)
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Matriks risiko")
                SampleData.risks.forEach { risk ->
                    val score = vm.riskScore(risk)
                    val level = when {
                        score >= 20 -> "Kritis"
                        score >= 12 -> "Tinggi"
                        score >= 6 -> "Sedang"
                        else -> "Rendah"
                    }
                    val expanded = vm.expandedRiskId == risk.id
                    Card(modifier = Modifier.fillMaxWidth().clickable { vm.expandedRiskId = if (expanded) null else risk.id }) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row {
                                Text(risk.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                                StatusBadge(level, if (level == "Kritis") CriticalRed else if (level == "Tinggi") WarningAmber else HealthyGreen)
                            }
                            Text(risk.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${risk.category} • ${risk.owner}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Skor $score = kemungkinan ${risk.likelihood} × dampak ${risk.impact}", style = MaterialTheme.typography.labelMedium)
                            if (expanded) {
                                HorizontalDivider()
                                Text(risk.mitigation, style = MaterialTheme.typography.bodyMedium)
                                Text("Status: ${risk.status}", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Riwayat keputusan")
                SampleData.dssHistory.forEach { history ->
                    Card {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(history.id, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                                Text(history.decidedAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(history.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Keputusan: ${history.decision}", style = MaterialTheme.typography.bodySmall)
                            Text("Hasil: ${history.outcome}", style = MaterialTheme.typography.labelMedium, color = HealthyGreen)
                        }
                    }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("Target SLA")
                    vm.slaTargets.forEach { target ->
                        InfoRow(target.severity.label, "Respons ${target.responseMinutes}m • Selesai ${target.resolveMinutes}m")
                    }
                }
            }
        }
    }
}
