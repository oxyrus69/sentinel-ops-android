package com.sentinel.ops

import com.sentinel.ops.data.AssetHealth
import com.sentinel.ops.data.IncidentStatus
import com.sentinel.ops.data.SampleData
import com.sentinel.ops.data.Severity
import com.sentinel.ops.ui.SentinelViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SentinelViewModelTest {

    @Test
    fun login_success_records_audit_and_sets_user() {
        val vm = SentinelViewModel()
        vm.login("admin@sentinel.id", SampleData.DEMO_PASSWORD)
        assertNotNull(vm.currentUser)
        assertEquals("Raka Pratama", vm.currentUser?.fullName)
        assertTrue(vm.auditLogs.any { it.action == "auth.login" })
    }

    @Test
    fun login_wrong_password_sets_error_and_sensitive_audit() {
        val vm = SentinelViewModel()
        vm.login("admin@sentinel.id", "salah")
        assertEquals(null, vm.currentUser)
        assertNotNull(vm.loginError)
        assertTrue(vm.auditLogs.any { it.action == "auth.login_failed" && it.sensitive })
    }

    @Test
    fun incident_status_transition_follows_flow() {
        val vm = SentinelViewModel()
        vm.login("admin@sentinel.id", SampleData.DEMO_PASSWORD)
        val id = SampleData.incidents.first { it.status == IncidentStatus.OPEN }.id
        vm.acknowledge(id)
        assertEquals(IncidentStatus.INVESTIGATING, vm.incidentById(id)?.status)
        vm.acknowledge(id)
        assertEquals(IncidentStatus.INVESTIGATING, vm.incidentById(id)?.status)
        vm.contain(id)
        assertEquals(IncidentStatus.CONTAINED, vm.incidentById(id)?.status)
        vm.resolve(id)
        assertEquals(IncidentStatus.RESOLVED, vm.incidentById(id)?.status)
        vm.close(id)
        assertEquals(IncidentStatus.CLOSED, vm.incidentById(id)?.status)
    }

    @Test
    fun portal_ticket_validation_and_medium_severity() {
        val vm = SentinelViewModel()
        vm.login("rektorat@kampus-nusantara.ac.id", SampleData.DEMO_PASSWORD)
        vm.portalTitle = "pendek"
        vm.portalDetail = "Penjelasan yang cukup panjang untuk laporan klien."
        assertFalse(vm.submitPortalTicket())
        assertNotNull(vm.portalFormError)

        vm.portalTitle = "Portal rektorat tidak bisa diakses"
        vm.portalDetail = "Halaman portal mengembalikan error 503 sejak pagi dan tidak dapat dipakai."
        assertTrue(vm.submitPortalTicket())
        val created = vm.incidents.last()
        assertEquals(Severity.MEDIUM, created.severity)
        assertEquals(IncidentStatus.OPEN, created.status)
        assertNotNull(vm.lastTicketNumber)
    }

    @Test
    fun asset_creation_requires_valid_fields() {
        val vm = SentinelViewModel()
        vm.assetName = "As"
        vm.assetIp = ""
        vm.assetLocation = ""
        assertFalse(vm.createAsset())
        assertNotNull(vm.assetFormError)

        vm.assetName = "Switch Core Uji"
        vm.assetIp = "10.8.9.10"
        vm.assetLocation = "DC-1 Rack A10"
        val before = vm.assets.size
        assertTrue(vm.createAsset())
        assertEquals(before + 1, vm.assets.size)
        assertEquals(AssetHealth.HEALTHY, vm.assets.last().health)
    }

    @Test
    fun vault_access_is_counted_and_audited() {
        val vm = SentinelViewModel()
        vm.login("admin@sentinel.id", SampleData.DEMO_PASSWORD)
        vm.toggleVault()
        val service = SampleData.vault.first().service
        val before = vm.vault.first { it.service == service }.accessCount
        vm.accessVaultEntry(service)
        assertEquals(before + 1, vm.vault.first { it.service == service }.accessCount)
        assertTrue(vm.auditLogs.any { it.action == "vault.secret_access" && it.sensitive })
    }

    @Test
    fun filters_and_export_work() {
        val vm = SentinelViewModel()
        vm.incidentFilter = "Kritis"
        assertTrue(vm.filteredIncidents.all { it.severity == Severity.CRITICAL })

        vm.incidentFilter = "Semua"
        vm.incidentQuery = "TLS"
        assertTrue(vm.filteredIncidents.any { it.title.contains("TLS", true) })

        vm.exportCsv("incidents")
        assertTrue(vm.auditLogs.any { it.action == "export.incidents" })
    }
}
