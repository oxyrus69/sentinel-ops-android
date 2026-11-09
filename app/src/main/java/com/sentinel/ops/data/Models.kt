package com.sentinel.ops.data

enum class UserRole(val label: String, val home: String) {
    ADMIN("Admin", "dashboard"),
    SECOPS("Security Ops", "dashboard"),
    SUPPORT("Support", "portal"),
    CLIENT_OWNER("Pemilik Klien", "portal"),
}

enum class Severity(val label: String) { CRITICAL("Kritis"), HIGH("Tinggi"), MEDIUM("Sedang"), LOW("Rendah") }

enum class IncidentStatus(val label: String) {
    OPEN("Terbuka"),
    INVESTIGATING("Investigasi"),
    CONTAINED("Terkendali"),
    RESOLVED("Selesai"),
    CLOSED("Ditutup"),
}

enum class AssetHealth(val label: String) { HEALTHY("Sehat"), WARNING("Peringatan"), COMPROMISED("Terkompromi"), DOWN("Mati"), MAINTENANCE("Pemeliharaan") }

enum class IncidentCategory(val label: String) {
    CONFIGURATION_ERROR("Kesalahan Konfigurasi"),
    SERVER_DOWN("Server Down"),
    SSL_EXPIRED("Sertifikat TLS Kedaluwarsa"),
    BRUTE_FORCE("Brute Force"),
    SQL_INJECTION("SQL Injection"),
    MALWARE_BACKDOOR("Malware / Backdoor"),
    PERFORMANCE("Kinerja"),
}

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val lastLoginAt: String,
)

data class TimelineEntry(val time: String, val actor: String, val action: String)

data class Incident(
    val id: String,
    val ticketNumber: String,
    val title: String,
    val severity: Severity,
    val status: IncidentStatus,
    val slaMinutesLeft: Int,
    val slaResponseTarget: Int,
    val slaResolveTarget: Int,
    val owner: String,
    val assignee: String,
    val assetId: String,
    val category: IncidentCategory,
    val createdAt: String,
    val description: String,
    val version: Int,
    val timeline: List<TimelineEntry>,
)

data class Asset(
    val id: String,
    val name: String,
    val category: String,
    val health: AssetHealth,
    val ipAddress: String,
    val location: String,
    val lastScan: String,
    val openFindings: Int,
    val uptimePercent: Double,
    val cpuPercent: Int,
    val memoryPercent: Int,
    val version: Int,
)

data class ScanFinding(
    val id: String,
    val title: String,
    val severity: Severity,
    val category: String,
    val target: String,
    val detectedAt: String,
    val detail: String,
    val remediation: String,
)

data class ScanRule(
    val code: String,
    val name: String,
    val category: String,
    val severity: Severity,
    val enabled: Boolean,
)

data class ScanScenario(
    val id: String,
    val label: String,
    val target: String,
    val preview: String,
    val latencyMs: Int,
)

data class ScanResult(
    val target: String,
    val httpStatus: Int,
    val findingCount: Int,
    val summary: String,
    val health: AssetHealth,
    val incidentId: String?,
)

data class Runbook(
    val code: String,
    val title: String,
    val category: String,
    val owner: String,
    val updatedAt: String,
    val steps: List<String>,
)

data class AuditLog(
    val id: String,
    val time: String,
    val actor: String,
    val action: String,
    val detail: String,
    val sensitive: Boolean,
)

data class DssRisk(
    val id: String,
    val title: String,
    val category: String,
    val likelihood: Int,
    val impact: Int,
    val owner: String,
    val mitigation: String,
    val status: String,
)

data class DssDecision(
    val id: String,
    val title: String,
    val recommendation: String,
    val confidence: Int,
    val impact: String,
)

data class DssHistory(
    val id: String,
    val title: String,
    val decision: String,
    val decidedAt: String,
    val outcome: String,
)

data class SlaTarget(
    val severity: Severity,
    val responseMinutes: Int,
    val resolveMinutes: Int,
)

data class PortalTicket(
    val id: String,
    val ticketNumber: String,
    val title: String,
    val detail: String,
    val category: IncidentCategory,
    val severity: Severity,
    val status: IncidentStatus,
    val assetId: String,
    val createdAt: String,
)

data class StatusService(
    val name: String,
    val slug: String,
    val status: String,
    val uptime: Double,
    val note: String,
)

data class VaultEntry(
    val service: String,
    val account: String,
    val lastRotated: String,
    val accessCount: Int,
)

data class OpsNotification(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val severity: Severity,
    val read: Boolean,
)

fun IncidentStatus.nextStatuses(): List<IncidentStatus> = when (this) {
    IncidentStatus.OPEN -> listOf(IncidentStatus.INVESTIGATING, IncidentStatus.CONTAINED, IncidentStatus.RESOLVED)
    IncidentStatus.INVESTIGATING -> listOf(IncidentStatus.CONTAINED, IncidentStatus.RESOLVED)
    IncidentStatus.CONTAINED -> listOf(IncidentStatus.RESOLVED)
    IncidentStatus.RESOLVED -> listOf(IncidentStatus.CLOSED)
    IncidentStatus.CLOSED -> emptyList()
}
