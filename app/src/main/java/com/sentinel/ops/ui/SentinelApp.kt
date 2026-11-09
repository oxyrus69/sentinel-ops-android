package com.sentinel.ops.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sentinel.ops.ui.screens.AssetDetailScreen
import com.sentinel.ops.ui.screens.AssetsScreen
import com.sentinel.ops.ui.screens.AuditScreen
import com.sentinel.ops.ui.screens.DashboardScreen
import com.sentinel.ops.ui.screens.DssScreen
import com.sentinel.ops.ui.screens.IncidentDetailScreen
import com.sentinel.ops.ui.screens.IncidentsScreen
import com.sentinel.ops.ui.screens.LoginScreen
import com.sentinel.ops.ui.screens.MoreScreen
import com.sentinel.ops.ui.screens.NotificationsScreen
import com.sentinel.ops.ui.screens.PortalScreen
import com.sentinel.ops.ui.screens.ReportScreen
import com.sentinel.ops.ui.screens.RunbookScreen
import com.sentinel.ops.ui.screens.ScannerScreen
import com.sentinel.ops.ui.screens.SettingsScreen
import com.sentinel.ops.ui.screens.StatusDetailScreen
import com.sentinel.ops.ui.screens.StatusScreen
import com.sentinel.ops.ui.screens.VaultScreen

private data class TopLevelRoute(val route: String, val label: String, val icon: ImageVector)

private val routes = listOf(
    TopLevelRoute("dashboard", "Dasbor", Icons.Default.Dashboard),
    TopLevelRoute("incidents", "Insiden", Icons.Default.Warning),
    TopLevelRoute("assets", "Aset", Icons.Default.Inventory2),
    TopLevelRoute("scanner", "Pindai", Icons.Default.QrCodeScanner),
    TopLevelRoute("more", "Lainnya", Icons.Default.Menu),
)

private fun routesFor(role: com.sentinel.ops.data.UserRole): List<TopLevelRoute> = when (role) {
    com.sentinel.ops.data.UserRole.ADMIN, com.sentinel.ops.data.UserRole.SECOPS -> routes
    com.sentinel.ops.data.UserRole.SUPPORT -> listOf(
        TopLevelRoute("portal", "Portal", Icons.Default.Dashboard),
        TopLevelRoute("incidents", "Insiden", Icons.Default.Warning),
        TopLevelRoute("scanner", "Pindai", Icons.Default.QrCodeScanner),
        TopLevelRoute("more", "Lainnya", Icons.Default.Menu),
    )
    com.sentinel.ops.data.UserRole.CLIENT_OWNER -> listOf(
        TopLevelRoute("portal", "Portal", Icons.Default.Dashboard),
        TopLevelRoute("incidents", "Insiden", Icons.Default.Warning),
        TopLevelRoute("status", "Status", Icons.Default.Notifications),
        TopLevelRoute("more", "Lainnya", Icons.Default.Menu),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentinelApp() {
    val navController = rememberNavController()
    val vm: SentinelViewModel = viewModel()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: "dashboard"
    val snackbar = remember { SnackbarHostState() }
    val user = vm.currentUser
    val detailRoute = current.startsWith("incident/") || current.startsWith("asset/") || current.startsWith("status/") ||
        current in setOf("notifications", "runbooks", "vault", "settings", "audit", "dss", "portal", "report")
    val title = when {
        current.startsWith("incident/") -> "Detail Insiden"
        current.startsWith("asset/") -> "Detail Aset"
        current.startsWith("status/") -> "Status Layanan"
        current == "notifications" -> "Pusat Notifikasi"
        current == "runbooks" -> "Runbook"
        current == "vault" -> "Brankas"
        current == "settings" -> "Pengaturan"
        current == "audit" -> "Jejak Audit"
        current == "dss" -> "Dukungan Keputusan"
        current == "portal" -> "Portal Klien"
        current == "report" -> "Laporan"
        current == "asset-form" -> "Tambah Aset"
        else -> "Sentinel Ops"
    }

    LaunchedEffect(vm.lastAction) {
        vm.lastAction?.let {
            snackbar.showSnackbar(it)
            vm.consumeAction()
        }
    }

    if (user == null) {
        LoginScreen(vm)
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (detailRoute) IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        BadgedBox(badge = { if (vm.unreadCount > 0) Badge { Text(vm.unreadCount.toString()) } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifikasi")
                        }
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
                    }
                    IconButton(onClick = { vm.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Keluar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            NavigationBar {
                routesFor(user.role).forEach { route ->
                    NavigationBarItem(
                        selected = current == route.route,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(route.icon, contentDescription = route.label) },
                        label = { Text(route.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = user.role.home) {
            composable("dashboard") {
                DashboardScreen(
                    padding, vm,
                    onIncident = { navController.navigate("incident/$it") },
                    onIncidents = { navController.navigate("incidents") },
                    onScanner = { navController.navigate("scanner") },
                    onDss = { navController.navigate("dss") },
                )
            }
            composable("incidents") { IncidentsScreen(padding, vm) { navController.navigate("incident/$it") } }
            composable("incident/{incidentId}") { entry ->
                IncidentDetailScreen(padding, vm, entry.arguments?.getString("incidentId").orEmpty()) { navController.navigate("asset/$it") }
            }
            composable("assets") {
                AssetsScreen(padding, vm, onOpen = { navController.navigate("asset/$it") }, onScan = { navController.navigate("scanner") }, onCreate = { navController.navigate("asset-form") })
            }
            composable("asset-form") { com.sentinel.ops.ui.screens.AssetFormScreen(padding, vm) { navController.popBackStack() } }
            composable("asset/{assetId}") { entry ->
                AssetDetailScreen(padding, vm, entry.arguments?.getString("assetId").orEmpty()) { navController.navigate("scanner") }
            }
            composable("scanner") { ScannerScreen(padding, vm) }
            composable("more") {
                MoreScreen(
                    padding, vm,
                    onDss = { navController.navigate("dss") },
                    onPortal = { navController.navigate("portal") },
                    onAudit = { navController.navigate("audit") },
                    onRunbooks = { navController.navigate("runbooks") },
                    onVault = { navController.navigate("vault") },
                    onStatus = { navController.navigate("status") },
                    onReport = { navController.navigate("report") },
                    onSettings = { navController.navigate("settings") },
                )
            }
            composable("notifications") { NotificationsScreen(padding, vm) }
            composable("dss") { DssScreen(padding, vm) }
            composable("portal") { PortalScreen(padding, vm) { navController.navigate("incident/$it") } }
            composable("audit") { AuditScreen(padding, vm) }
            composable("runbooks") { RunbookScreen(padding, vm) }
            composable("vault") { VaultScreen(padding, vm) }
            composable("status") { StatusScreen(padding, vm) { navController.navigate("status/$it") } }
            composable("status/{slug}") { entry ->
                StatusDetailScreen(padding, vm, entry.arguments?.getString("slug").orEmpty())
            }
            composable("report") { ReportScreen(padding, vm) }
            composable("settings") { SettingsScreen(padding, vm) }
        }
    }
}
