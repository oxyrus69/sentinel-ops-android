package com.sentinel.ops.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.ops.data.Asset
import com.sentinel.ops.data.AssetHealth
import com.sentinel.ops.data.AuditLog
import com.sentinel.ops.data.DssDecision
import com.sentinel.ops.data.DssRisk
import com.sentinel.ops.data.Incident
import com.sentinel.ops.data.IncidentCategory
import com.sentinel.ops.data.IncidentStatus
import com.sentinel.ops.data.OpsNotification
import com.sentinel.ops.data.PortalTicket
import com.sentinel.ops.data.SampleData
import com.sentinel.ops.data.ScanFinding
import com.sentinel.ops.data.ScanResult
import com.sentinel.ops.data.ScanScenario
import com.sentinel.ops.data.Severity
import com.sentinel.ops.data.StatusService
import com.sentinel.ops.data.TimelineEntry
import com.sentinel.ops.data.User
import com.sentinel.ops.data.VaultEntry
import com.sentinel.ops.data.nextStatuses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SentinelViewModel : ViewModel() {
    private val scanScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCleared() {
        scanScope.cancel()
        super.onCleared()
    }

    // Auth and audit
    var currentUser by mutableStateOf<User?>(null); private set
    var loginError by mutableStateOf<String?>(null); private set
    var auditLogs by mutableStateOf(SampleData.auditLogs); private set
    var auditQuery by mutableStateOf("")

    // Core data
    var incidents by mutableStateOf(SampleData.incidents); private set
    var assets by mutableStateOf(SampleData.assets); private set
    var findings by mutableStateOf(SampleData.findings); private set
    var rules by mutableStateOf(SampleData.rules); private set
    val runbooks = SampleData.runbooks
    val decisions = SampleData.decisions
    val statusServices = SampleData.statusServices
    val slaTargets = SampleData.slaTargets
    var notifications by mutableStateOf(SampleData.notifications); private set
    var vault by mutableStateOf(SampleData.vault); private set

    // Incidents state
    var incidentQuery by mutableStateOf("")
    var incidentFilter by mutableStateOf("Aktif")

    // Assets state
    var assetQuery by mutableStateOf("")
    var assetFilter by mutableStateOf("Semua")

    // Asset form
    var assetName by mutableStateOf("")
    var assetCategory by mutableStateOf("Server")
    var assetIp by mutableStateOf("")
    var assetLocation by mutableStateOf("")
    var assetHealth by mutableStateOf(AssetHealth.HEALTHY)
    var assetFormError by mutableStateOf<String?>(null)

    // Portal form
    var portalAssetId by mutableStateOf("AST-WEB-02")
    var portalCategory by mutableStateOf(IncidentCategory.SERVER_DOWN)
    var portalTitle by mutableStateOf("")
    var portalDetail by mutableStateOf("")
    var portalFormError by mutableStateOf<String?>(null)
    var lastTicketNumber by mutableStateOf<String?>(null)

    // Scanner state
    var scanScenarioId by mutableStateOf(SampleData.scenarios.first().id)
    var scanRunning by mutableStateOf(false); private set
    var scanProgress by mutableStateOf(0f); private set
    var scanMessage by mutableStateOf("Pilih skenario untuk menguji deteksi."); private set
    var scanResult by mutableStateOf<ScanResult?>(null); private set
    var expandedFindingId by mutableStateOf<String?>(null)
    var selectedRunbook by mutableStateOf<String?>(null)
    var expandedRiskId by mutableStateOf<String?>(null)

    // Vault and settings
    var vaultUnlocked by mutableStateOf(false); private set
    var autoSync by mutableStateOf(true)
    var pushEnabled by mutableStateOf(true)

    // Snackbar / shared feedback
    var lastAction by mutableStateOf<String?>(null); private set

    val unreadCount get() = notifications.count { !it.read }
    val activeCount get() = incidents.count { it.status != IncidentStatus.RESOLVED && it.status != IncidentStatus.CLOSED }
    val criticalCount get() = incidents.count { it.severity == Severity.CRITICAL && it.status != IncidentStatus.CLOSED && it.status != IncidentStatus.RESOLVED }
    val slaRiskCount get() = incidents.count { it.status != IncidentStatus.RESOLVED && it.status != IncidentStatus.CLOSED && it.slaMinutesLeft in 1..30 }
    val healthyAssetCount get() = assets.count { it.health == AssetHealth.HEALTHY }
    val sensitiveAuditCount get() = auditLogs.count { it.sensitive }

    val filteredIncidents get() = incidents.filter { incident ->
        val queryOk = incidentQuery.isBlank() || listOf(incident.id, incident.ticketNumber, incident.title, incident.assignee, incident.assetId)
            .any { it.contains(incidentQuery, ignoreCase = true) }
        val filterOk = when (incidentFilter) {
            "Aktif" -> incident.status != IncidentStatus.RESOLVED && incident.status != IncidentStatus.CLOSED
            "Kritis" -> incident.severity == Severity.CRITICAL && incident.status != IncidentStatus.CLOSED && incident.status != IncidentStatus.RESOLVED
            "SLA" -> incident.slaMinutesLeft in 1..30
            "Selesai" -> incident.status == IncidentStatus.RESOLVED
            "Ditutup" -> incident.status == IncidentStatus.CLOSED
            else -> true
        }
        queryOk && filterOk
    }

    val filteredAssets get() = assets.filter { asset ->
        val queryOk = assetQuery.isBlank() || listOf(asset.id, asset.name, asset.category, asset.ipAddress)
            .any { it.contains(assetQuery, ignoreCase = true) }
        val filterOk = when (assetFilter) {
            "Perlu atensi" -> asset.health != AssetHealth.HEALTHY
            "Sehat" -> asset.health == AssetHealth.HEALTHY
            else -> true
        }
        queryOk && filterOk
    }

    val filteredAudit get() = auditLogs.filter { audit -> auditQuery.isBlank() || listOf(audit.action, audit.actor, audit.detail).any { it.contains(auditQuery, ignoreCase = true) } }

    fun incidentById(id: String) = incidents.firstOrNull { it.id == id }
    fun assetById(id: String) = assets.firstOrNull { it.id == id }
    fun statusBySlug(slug: String) = statusServices.firstOrNull { it.slug == slug }
    fun riskScore(risk: DssRisk) = risk.likelihood * risk.impact

    // Auth
    fun login(email: String, password: String) {
        val user = SampleData.users.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
        loginError = when {
            user == null -> "Email tidak terdaftar pada mode demo."
            password != SampleData.DEMO_PASSWORD -> "Kata sandi salah. Gunakan sentinel123."
            else -> null
        }
        user?.let {
            if (loginError == null) {
                currentUser = it.copy(lastLoginAt = "Baru saja")
                addAudit(it.fullName, "auth.login", "Login ${it.email}")
                lastAction = "Selamat datang, ${it.fullName}"
            } else addAudit(email.ifBlank { "tidak diketahui" }, "auth.login_failed", "Percobaan login gagal", true)
        }
    }

    fun logout() {
        currentUser?.let { addAudit(it.fullName, "auth.logout", "Logout dari perangkat mobile") }
        currentUser = null
        lastAction = "Sesi berakhir"
    }

    private fun addAudit(actor: String, action: String, detail: String, sensitive: Boolean = false) {
        auditLogs = (auditLogs + AuditLog("AUD-${auditLogs.size + 1}", "Baru saja", actor, action, detail, sensitive))
    }

    // Incidents
    fun changeIncidentStatus(id: String, status: IncidentStatus) {
        val incident = incidentById(id) ?: return
        if (status !in incident.status.nextStatuses()) {
            lastAction = "Transisi ${incident.status.label} → ${status.label} tidak diizinkan"
            return
        }
        incidents = incidents.map {
            if (it.id != id) it else it.copy(
                status = status,
                slaMinutesLeft = if (status == IncidentStatus.RESOLVED || status == IncidentStatus.CLOSED) 0 else it.slaMinutesLeft,
                version = it.version + 1,
                timeline = it.timeline + TimelineEntry("Baru saja", currentUser?.fullName ?: "Operator", "Status menjadi ${status.label}"),
            )
        }
        addAudit(currentUser?.fullName ?: "Operator", "incident.status", "$id → ${status.label}")
        lastAction = "$id sekarang ${status.label}"
    }

    fun acknowledge(id: String) = changeIncidentStatus(id, IncidentStatus.INVESTIGATING)
    fun contain(id: String) = changeIncidentStatus(id, IncidentStatus.CONTAINED)
    fun resolve(id: String) = changeIncidentStatus(id, IncidentStatus.RESOLVED)
    fun close(id: String) = changeIncidentStatus(id, IncidentStatus.CLOSED)
    fun escalate(id: String) {
        val incident = incidentById(id) ?: return
        incidents = incidents.map { if (it.id == id) it.copy(assignee = "On-call Lead", slaMinutesLeft = (it.slaMinutesLeft - 5).coerceAtLeast(1)) else it }
        addAudit(currentUser?.fullName ?: "Operator", "incident.escalate", "$id dieskalasikan")
        lastAction = "$id dieskalasikan ke On-call Lead"
    }

    // Assets
    fun createAsset(): Boolean {
        val name = assetName.trim(); val ip = assetIp.trim(); val location = assetLocation.trim()
        assetFormError = when {
            name.length < 3 -> "Nama aset minimal 3 karakter."
            ip.isBlank() -> "Alamat IP wajib diisi."
            location.isBlank() -> "Lokasi wajib diisi."
            assets.any { it.name.equals(name, true) } -> "Nama aset sudah terdaftar."
            else -> null
        }
        if (assetFormError != null) return false
        val id = "AST-${assets.size + 1}".padEnd(3, '0')
        assets = assets + Asset(id, name, assetCategory, assetHealth, ip, location, "Belum dipindai", 0, 100.0, 0, 0, 1)
        addAudit(currentUser?.fullName ?: "Operator", "asset.create", "$id $name")
        lastAction = "Aset $name dibuat"
        assetName = ""; assetIp = ""; assetLocation = ""; assetHealth = AssetHealth.HEALTHY
        return true
    }

    fun updateAssetHealth(id: String, health: AssetHealth) {
        assets = assets.map { if (it.id == id) it.copy(health = health, version = it.version + 1) else it }
        addAudit(currentUser?.fullName ?: "Operator", "asset.health", "$id → ${health.label}")
        lastAction = "$id diperbarui menjadi ${health.label}"
    }

    // Portal
    fun submitPortalTicket(): Boolean {
        val title = portalTitle.trim(); val detail = portalDetail.trim()
        portalFormError = when {
            assetById(portalAssetId) == null -> "Aset wajib dipilih."
            title.length < 10 -> "Judul singkat minimal 10 karakter."
            title.length > 160 -> "Judul maksimal 160 karakter."
            detail.length < 30 -> "Penjelasan minimal 30 karakter."
            else -> null
        }
        if (portalFormError != null) return false
        val ticketNumber = "SEN-2026-${1000 + incidents.size + portalTickets.size + 1}"
        val ticket = PortalTicket("POR-${portalTickets.size + 1}", ticketNumber, title, detail, portalCategory, Severity.MEDIUM, IncidentStatus.OPEN, portalAssetId, "Baru saja")
        incidents = incidents + Incident(
            id = "INC-2026-${100 + incidents.size + 1}",
            ticketNumber = ticketNumber,
            title = title,
            severity = Severity.MEDIUM,
            status = IncidentStatus.OPEN,
            slaMinutesLeft = 240,
            slaResponseTarget = 240,
            slaResolveTarget = 1440,
            owner = currentUser?.role?.label ?: "Portal",
            assignee = "Belum ditugaskan",
            assetId = portalAssetId,
            category = portalCategory,
            createdAt = "Baru saja",
            description = detail,
            version = 1,
            timeline = listOf(TimelineEntry("Baru saja", currentUser?.fullName ?: "Klien", "Tiket dibuat dari portal klien")),
        )
        addAudit(currentUser?.fullName ?: "Klien", "portal.create", ticketNumber)
        lastTicketNumber = ticketNumber
        lastAction = "Tiket $ticketNumber berhasil dibuat"
        portalTitle = ""; portalDetail = ""
        return true
    }

    private val portalTickets = mutableListOf<PortalTicket>().apply { addAll(SampleData.portalTickets) }

    // Scanner
    val activeRuleCount get() = rules.count { it.enabled }

    fun toggleRule(code: String) {
        rules = rules.map { if (it.code == code) it.copy(enabled = !it.enabled) else it }
    }

    fun startScan() {
        if (scanRunning) return
        val scenario = SampleData.scenarios.firstOrNull { it.id == scanScenarioId } ?: return
        scanScope.launch {
            scanRunning = true
            scanProgress = 0f
            scanMessage = "Memindai ${scenario.target} • ${scenario.latencyMs}ms"
            while (scanProgress < 1f) {
                delay(if (scenario.latencyMs > 1000) 500 else 220)
                scanProgress = (scanProgress + 0.12f).coerceAtMost(1f)
            }
            scanRunning = false
            scanMessage = "Pemindaian ${scenario.label} selesai • ${activeRuleCount} aturan aktif"
            applyScenario(scenario)
        }
    }

    private fun applyScenario(scenario: ScanScenario) {
        val asset = assets.firstOrNull { it.name.contains("Portal", true) } ?: assets.first()
        val status = if (scenario.id == "SERVER_DOWN") 503 else 200
        val severity = when (scenario.id) {
            "SLOT_DEFACEMENT", "MALWARE_BACKDOOR", "SERVER_DOWN" -> Severity.CRITICAL
            "SQLI_ERROR" -> Severity.HIGH
            "SSL_EXPIRED" -> Severity.MEDIUM
            else -> Severity.LOW
        }
        val category = when (scenario.id) {
            "SLOT_DEFACEMENT" -> IncidentCategory.CONFIGURATION_ERROR
            "SQLI_ERROR" -> IncidentCategory.SQL_INJECTION
            "MALWARE_BACKDOOR" -> IncidentCategory.MALWARE_BACKDOOR
            "SERVER_DOWN" -> IncidentCategory.SERVER_DOWN
            "SSL_EXPIRED" -> IncidentCategory.SSL_EXPIRED
            else -> IncidentCategory.CONFIGURATION_ERROR
        }
        val health = when (scenario.id) {
            "SERVER_DOWN" -> AssetHealth.DOWN
            "SLOT_DEFACEMENT", "MALWARE_BACKDOOR" -> AssetHealth.COMPROMISED
            "SQLI_ERROR", "SSL_EXPIRED" -> AssetHealth.WARNING
            else -> AssetHealth.HEALTHY
        }
        val finding = if (scenario.id == "CLEAN") null else ScanFinding(
            id = "FND-${1000 + findings.size + 1}",
            title = when (scenario.id) {
                "SLOT_DEFACEMENT" -> "Indikasi defacement pada halaman utama"
                "SQLI_ERROR" -> "Pesan error SQL terekspos ke publik"
                "MALWARE_BACKDOOR" -> "Pola backdoor PHP terdeteksi"
                "SERVER_DOWN" -> "Layanan mengembalikan HTTP 503"
                "SSL_EXPIRED" -> "Sertifikat TLS tidak valid"
                else -> "Temuan konfigurasi"
            },
            severity = severity,
            category = category.name,
            target = scenario.target,
            detectedAt = "Baru saja",
            detail = scenario.preview.take(120),
            remediation = when (severity) {
                Severity.CRITICAL -> "Aktifkan respons insiden, isolasi layanan, dan verifikasi integritas file."
                Severity.HIGH -> "Batasi error detail ke log internal dan terapkan validasi input."
                Severity.MEDIUM -> "Rotasi sertifikat dan tambahkan pemantauan masa berlaku."
                Severity.LOW -> "Pantau ulang pada siklus berikutnya."
            },
        )
        if (finding != null) findings = listOf(finding) + findings
        assets = assets.map { if (it.id == asset.id) it.copy(health = health, lastScan = "Baru saja", openFindings = if (finding == null) 0 else it.openFindings + 1, version = it.version + 1) else it }

        val existing = incidents.firstOrNull { it.assetId == asset.id && it.category == category && it.status in listOf(IncidentStatus.OPEN, IncidentStatus.INVESTIGATING, IncidentStatus.CONTAINED) }
        val autoIncident = when {
            scenario.id == "CLEAN" -> null
            existing != null -> existing
            severity == Severity.LOW -> null
            else -> incidents.let { list ->
                val newIncident = Incident(
                    id = "INC-2026-${200 + list.size + 1}",
                    ticketNumber = "SEN-2026-${2000 + list.size + 1}",
                    title = "${category.label} pada ${asset.name}",
                    severity = severity,
                    status = IncidentStatus.OPEN,
                    slaMinutesLeft = slaTargets.first { it.severity == severity }.responseMinutes,
                    slaResponseTarget = slaTargets.first { it.severity == severity }.responseMinutes,
                    slaResolveTarget = slaTargets.first { it.severity == severity }.resolveMinutes,
                    owner = "SecOps",
                    assignee = "Belum ditugaskan",
                    assetId = asset.id,
                    category = category,
                    createdAt = "Baru saja",
                    description = "Auto-incident dari pemindaian ${scenario.label}: ${finding?.title.orEmpty()}",
                    version = 1,
                    timeline = listOf(TimelineEntry("Baru saja", "Scanner", "Insiden otomatis dibuat dari hasil pemindaian.")),
                )
                incidents = list + newIncident
                newIncident
            }
        }
        scanResult = ScanResult(scenario.target, status, findings.size, "${scenario.label} • ${finding?.title ?: "Tidak ada temuan"}", health, autoIncident?.id)
        addAudit(currentUser?.fullName ?: "Scanner", "scan.run", "${scenario.id} pada ${scenario.target}")
        lastAction = if (autoIncident != null) "Pemindaian selesai • ${autoIncident.id}" else "Pemindaian selesai"
    }

    // Runbooks
    fun selectRunbook(code: String?) { selectedRunbook = code }

    // Audit, notifications, vault
    fun markAllRead() {
        notifications = notifications.map { it.copy(read = true) }
        lastAction = "Semua notifikasi ditandai dibaca"
    }

    fun readNotification(item: OpsNotification) {
        notifications = notifications.map { if (it.id == item.id) it.copy(read = true) else it }
    }

    fun toggleVault() {
        vaultUnlocked = !vaultUnlocked
        if (vaultUnlocked) addAudit(currentUser?.fullName ?: "Operator", "vault.unlock", "Brankas dibuka dari perangkat", true)
    }

    fun accessVaultEntry(service: String) {
        vault = vault.map { if (it.service == service) it.copy(accessCount = it.accessCount + 1) else it }
        addAudit(currentUser?.fullName ?: "Operator", "vault.secret_access", "Akses $service", true)
        lastAction = "Akses $service tercatat di audit"
    }

    fun exportCsv(type: String) {
        val csv = when (type) {
            "incidents" -> buildString {
                appendLine("id,ticket,title,severity,status,asset")
                incidents.forEach { appendLine("${it.id},${it.ticketNumber},${it.title},${it.severity.label},${it.status.label},${it.assetId}") }
            }
            "assets" -> buildString {
                appendLine("id,name,category,health,ip,findings")
                assets.forEach { appendLine("${it.id},${it.name},${it.category},${it.health.label},${it.ipAddress},${it.openFindings}") }
            }
            else -> buildString {
                appendLine("time,actor,action,detail,sensitive")
                auditLogs.forEach { appendLine("${it.time},${it.actor},${it.action},${it.detail},${it.sensitive}") }
            }
        }
        addAudit(currentUser?.fullName ?: "Operator", "export.$type", "Ekspor ${type} dijalankan")
        lastAction = "Ekspor $type disiapkan (${csv.lines().size - 1} baris)"
    }

    fun consumeAction() { lastAction = null }
}
