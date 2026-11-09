package com.sentinel.ops.data

import com.sentinel.ops.data.IncidentCategory.BRUTE_FORCE
import com.sentinel.ops.data.IncidentCategory.CONFIGURATION_ERROR
import com.sentinel.ops.data.IncidentCategory.PERFORMANCE
import com.sentinel.ops.data.IncidentCategory.SERVER_DOWN
import com.sentinel.ops.data.IncidentCategory.SSL_EXPIRED

object SampleData {
    const val DEMO_PASSWORD = "sentinel123"

    val users = listOf(
        User("USR-01", "admin@sentinel.id", "Raka Pratama", UserRole.ADMIN, "Belum login"),
        User("USR-02", "secops@sentinel.id", "Nadia Putri", UserRole.SECOPS, "Belum login"),
        User("USR-03", "support@sentinel.id", "Ayu Larasati", UserRole.SUPPORT, "Belum login"),
        User("USR-04", "rektorat@kampus-nusantara.ac.id", "Dr. Sari Wijaya", UserRole.CLIENT_OWNER, "Belum login"),
    )

    val incidents = listOf(
        Incident(
            "INC-2026-0142", "SEN-2026-0912", "Lonjakan latensi database transaksi", Severity.CRITICAL,
            IncidentStatus.OPEN, 12, 15, 240, "NOC", "Raka Pratama", "AST-DB-01", PERFORMANCE,
            "09:42 WIB", "Latensi P95 melewati ambang 850 ms selama tiga interval. Replika baca masih sehat.",
            1,
            listOf(TimelineEntry("09:42", "Sentinel Monitor", "Insiden dibuat dari anomali metrik."), TimelineEntry("09:44", "DSS Engine", "Prioritas dinaikkan menjadi P1.")),
        ),
        Incident(
            "INC-2026-0138", "SEN-2026-0907", "Indikasi brute force pada portal admin", Severity.HIGH,
            IncidentStatus.INVESTIGATING, 45, 60, 720, "SecOps", "Nadia Putri", "AST-WEB-02", BRUTE_FORCE,
            "08:17 WIB", "147 percobaan autentikasi gagal dari 6 alamat IP. Sebagian sumber telah diblokir WAF.",
            2,
            listOf(TimelineEntry("08:17", "Scanner", "Percobaan brute force terdeteksi."), TimelineEntry("08:20", "Nadia Putri", "Insiden diambil alih.")),
        ),
        Incident(
            "INC-2026-0129", "SEN-2026-0888", "Sertifikat TLS akan kedaluwarsa", Severity.MEDIUM,
            IncidentStatus.CONTAINED, 238, 240, 1440, "Infra", "Dimas Aryo", "AST-API-03", SSL_EXPIRED,
            "Kemarin, 21:10", "Sertifikat gateway API tersisa kurang dari 72 jam. Proses rotasi dijadwalkan ulang.",
            3,
            listOf(TimelineEntry("21:10", "Certificate Watch", "Peringatan masa berlaku dibuat.")),
        ),
        Incident(
            "INC-2026-0124", "SEN-2026-0870", "Tunnel VPN cabang tidak stabil", Severity.HIGH,
            IncidentStatus.OPEN, 76, 60, 720, "Network", "Belum ditugaskan", "AST-RTR-07", CONFIGURATION_ERROR,
            "Kemarin, 18:05", "Packet loss pada tunnel utama mencapai 18%. Trafik dialihkan sebagian ke koneksi cadangan.",
            1,
            listOf(TimelineEntry("18:05", "Network Probe", "Paket hilang melewati ambang 10%.")),
        ),
        Incident(
            "INC-2026-0118", "SEN-2026-0812", "Kapasitas volume cadangan di atas ambang", Severity.LOW,
            IncidentStatus.RESOLVED, 0, 720, 4320, "Infra", "Dimas Aryo", "AST-NAS-04", CONFIGURATION_ERROR,
            "2 hari lalu", "Volume cadangan dibersihkan dan kebijakan retensi disesuaikan.",
            4,
            listOf(TimelineEntry("16:20", "Dimas Aryo", "Retensi dipulihkan dan tiket ditutup.")),
        ),
        Incident(
            "INC-2026-0110", "SEN-2026-0788", "Server portal sempat tidak responsif", Severity.CRITICAL,
            IncidentStatus.CLOSED, 0, 15, 240, "NOC", "Nadia Putri", "AST-WEB-02", SERVER_DOWN,
            "3 hari lalu", "Layanan dipulihkan setelah restart terkendali dan permintaan dialihkan ke node cadangan.",
            5,
            listOf(TimelineEntry("02:14", "Sentinel Monitor", "Auto-incident dibuat dari status HTTP 503.")),
        ),
    )

    val assets = listOf(
        Asset("AST-DB-01", "PostgreSQL Primary", "Database", AssetHealth.WARNING, "10.8.0.11", "DC-1 / Rack A04", "09:36 WIB", 3, 99.82, 78, 83, 1),
        Asset("AST-WEB-02", "Portal Public Web", "Web Server", AssetHealth.COMPROMISED, "10.8.0.22", "DC-1 / Rack B02", "09:30 WIB", 7, 99.15, 62, 71, 2),
        Asset("AST-API-03", "API Gateway", "Gateway", AssetHealth.WARNING, "10.8.0.31", "DC-2 / Rack C01", "09:28 WIB", 2, 99.97, 41, 54, 1),
        Asset("AST-RTR-07", "Branch Core Router", "Network", AssetHealth.DOWN, "172.16.7.1", "Cabang Bandung", "08:58 WIB", 1, 97.44, 88, 66, 1),
        Asset("AST-NAS-04", "Backup Repository", "Storage", AssetHealth.HEALTHY, "10.8.4.14", "DC-2 / Rack D08", "09:12 WIB", 0, 99.99, 23, 48, 1),
        Asset("AST-EDR-18", "Finance Endpoint 18", "Endpoint", AssetHealth.MAINTENANCE, "10.20.1.18", "Kantor Pusat", "07:45 WIB", 0, 98.70, 12, 39, 1),
    )

    val findings = listOf(
        ScanFinding("FND-0091", "Login admin tanpa pembatasan percobaan", Severity.HIGH, "BRUTE_FORCE", "portal.sentinel.local", "09:30 WIB", "Endpoint autentikasi merespons seluruh percobaan tanpa backoff memadai.", "Aktifkan rate limit per akun dan per alamat IP, lalu hubungkan kebijakan blokir ke WAF."),
        ScanFinding("FND-0088", "Content-Security-Policy belum tersedia", Severity.MEDIUM, "CONFIGURATION_ERROR", "portal.sentinel.local", "09:29 WIB", "Respons HTML tidak memuat kebijakan pembatasan sumber skrip.", "Terapkan CSP report-only, evaluasi pelanggaran, lalu enforce."),
        ScanFinding("FND-0082", "Sertifikat TLS tersisa 68 jam", Severity.MEDIUM, "SSL_EXPIRED", "api.sentinel.local", "09:28 WIB", "Sertifikat leaf akan kedaluwarsa akhir pekan.", "Rotasi sertifikat dan validasi rantai kepercayaan."),
        ScanFinding("FND-0077", "Banner SSH mengekspos versi", Severity.LOW, "CONFIGURATION_ERROR", "10.8.0.11", "09:26 WIB", "Banner layanan mempermudah fingerprinting.", "Sembunyikan versi dan batasi akses melalui daftar tepercaya."),
    )

    val rules = listOf(
        ScanRule("SQLI-001", "Deteksi SQL Injection", "Aplikasi Web", Severity.CRITICAL, true),
        ScanRule("AUTH-004", "Brute Force & Credential Stuffing", "Autentikasi", Severity.HIGH, true),
        ScanRule("TLS-002", "Masa Berlaku Sertifikat", "Transport", Severity.MEDIUM, true),
        ScanRule("CFG-007", "Security Header Baseline", "Konfigurasi", Severity.MEDIUM, true),
        ScanRule("MAL-003", "Indikasi Backdoor", "File & Proses", Severity.CRITICAL, false),
    )

    val scenarios = listOf(
        ScanScenario("CLEAN", "Bersih", "portal.sentinel.local", "<!doctype html><html><body>Situs aman</body></html>", 320),
        ScanScenario("SLOT_DEFACEMENT", "Defacement Slot", "portal.sentinel.local", "<html><body><h1>PATCHED BY GHOSTSEC</h1></body></html>", 380),
        ScanScenario("SQLI_ERROR", "Error SQL Injection", "portal.sentinel.local", "You have an error in your SQL syntax; check the manual", 1850),
        ScanScenario("MALWARE_BACKDOOR", "Backdoor Malware", "portal.sentinel.local", "<?php eval(\$_POST['cmd']); ?>", 420),
        ScanScenario("SERVER_DOWN", "Server Down", "portal.sentinel.local", "HTTP 503 Service Unavailable", 8000),
        ScanScenario("SSL_EXPIRED", "TLS Kedaluwarsa", "api.sentinel.local", "NET::ERR_CERT_DATE_INVALID", 300),
    )

    val runbooks = listOf(
        Runbook("RB-SEC-01", "Respon Brute Force", "Keamanan", "SecOps", "6 Nov 2026", listOf("Validasi pola sumber dan akun terdampak.", "Terapkan blokir sementara pada WAF.", "Periksa keberhasilan autentikasi mencurigakan.", "Rotasi kredensial dan dokumentasikan bukti.")),
        Runbook("RB-DB-03", "Mitigasi Latensi Database", "Database", "NOC", "4 Nov 2026", listOf("Bandingkan P95 dengan baseline 24 jam.", "Identifikasi query dan lock teratas.", "Alihkan bacaan ke replika sehat.", "Evaluasi restart terkendali.")),
        Runbook("RB-NET-02", "Failover Tunnel VPN", "Jaringan", "Network", "1 Nov 2026", listOf("Uji kualitas link cadangan.", "Alihkan rute secara bertahap.", "Pantau packet loss selama 15 menit.", "Buat tiket ke penyedia koneksi utama.")),
    )

    val auditLogs = listOf(
        AuditLog("AUD-01", "08:17 WIB", "Scanner", "scan.run", "Pemindaian portal.sentinel.local", false),
        AuditLog("AUD-02", "08:20 WIB", "Nadia Putri", "incident.acknowledge", "INC-2026-0138 diambil alih", false),
        AuditLog("AUD-03", "07:52 WIB", "Raka Pratama", "auth.login", "Login admin@sentinel.id", false),
        AuditLog("AUD-04", "07:41 WIB", "Sistem", "asset.deleted", "Data sensitif dihapus dari indeks", true),
    )

    val risks = listOf(
        DssRisk("RSK-01", "Brute force portal admin", "Keamanan", 4, 5, "SecOps", "Rate limit + blokir adaptif WAF", "Aktif"),
        DssRisk("RSK-02", "TLS kedaluwarsa", "Kepatuhan", 3, 4, "Infra", "Rotasi otomatis 7 hari sebelum kedaluwarsa", "Aktif"),
        DssRisk("RSK-03", "Latensi database", "Ketersediaan", 3, 5, "NOC", "Connection pooling dan replika baca", "Aktif"),
        DssRisk("RSK-04", "Akses vault tidak dipantau", "Akses", 2, 5, "SecOps", "Audit akses dan notifikasi real-time", "Terbuka"),
    )

    val decisions = listOf(
        DssDecision("DSS-01", "Eskalasi insiden P1", "Aktifkan war room dan libatkan on-call lead", 94, "Mengurangi downtime 30-45 menit"),
        DssDecision("DSS-02", "Rotasi kredensial darurat", "Rotasi kunci vault dan paksa logout sesi", 88, "Mengurangi risiko akses lanjutan"),
        DssDecision("DSS-03", "Perluasan monitoring endpoint", "Agen EDR ke 18 endpoint kantor", 79, "Cakupan deteksi naik 22 poin"),
    )

    val dssHistory = listOf(
        DssHistory("DSS-H01", "Pemeliharaan router cabang", "Failover tunnel VPN", "5 Nov 2026", "Downtime turun 22 menit"),
        DssHistory("DSS-H02", "Rotasi kredensial vault", "Disetujui dengan pemantauan akses", "3 Nov 2026", "Akses mencuri tidak terdeteksi"),
        DssHistory("DSS-H03", "Peningkatan WAF portal", "Blokir adaptif diaktifkan", "1 Nov 2026", "Percobaan brute force turun 61%"),
    )

    val portalTickets = listOf(
        PortalTicket("POR-01", "SEN-2026-0910", "Portal rektorat lambat dibuka", "Halaman beranda membutuhkan waktu lebih dari 10 detik sejak pagi.", SERVER_DOWN, Severity.HIGH, IncidentStatus.OPEN, "AST-WEB-02", "08:55 WIB"),
        PortalTicket("POR-02", "SEN-2026-0903", "Laporan CSV gagal diunduh", "Unduhan laporan bulanan berhenti di 80%.", CONFIGURATION_ERROR, Severity.MEDIUM, IncidentStatus.INVESTIGATING, "AST-API-03", "Kemarin, 14:20"),
    )

    val statusServices = listOf(
        StatusService("Portal Akademik", "portal-akademik", "Operasional", 99.98, "Semua layanan inti berjalan normal."),
        StatusService("API Integrasi", "api-integrasi", "Terganggu Sebagian", 99.21, "Latensi meningkat, kapasitas sedang ditambah."),
        StatusService("Layanan Sertifikat", "sertifikat", "Perhatian", 99.80, "Sertifikat gateway akan dirotasi."),
    )

    val vault = listOf(
        VaultEntry("Production Database", "ops-readonly", "2 hari lalu", 6),
        VaultEntry("WAF Administrator", "secops-breakglass", "18 hari lalu", 12),
        VaultEntry("Branch Router", "netops-admin", "31 hari lalu", 3),
    )

    val notifications = listOf(
        OpsNotification("NTF-01", "SLA kritis tersisa 12 menit", "INC-2026-0142 memerlukan pengakuan operator.", "Baru saja", Severity.CRITICAL, false),
        OpsNotification("NTF-02", "Finding keamanan baru", "Login admin tanpa pembatasan percobaan ditemukan.", "6 menit lalu", Severity.HIGH, false),
        OpsNotification("NTF-03", "Sinkronisasi inventaris selesai", "6 aset diperbarui dari sumber CMDB.", "21 menit lalu", Severity.LOW, true),
    )

    val slaTargets = listOf(
        SlaTarget(Severity.CRITICAL, 15, 240),
        SlaTarget(Severity.HIGH, 60, 720),
        SlaTarget(Severity.MEDIUM, 240, 1440),
        SlaTarget(Severity.LOW, 720, 4320),
    )
}
