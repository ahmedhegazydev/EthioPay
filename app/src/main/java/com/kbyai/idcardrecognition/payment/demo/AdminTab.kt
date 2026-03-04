package com.kbyai.idcardrecognition.payment.demo

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AdminTab(
    onStatus: (String) -> Unit,
    bumpRefresh: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var users by remember { mutableStateOf<List<AdminUserDto>>(emptyList()) }
    var events by remember { mutableStateOf<List<AdminEventDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) } // 0 users, 1 events

    fun refresh() {
        scope.launch {
            try {
                loading = true
                onStatus("Loading admin…")
                users = DemoApi.adminGetUsers()
                events = DemoApi.adminGetEvents()
                onStatus("Admin OK")
            } catch (e: Exception) {
                onStatus("Admin error: ${e.message}")
                Toast.makeText(ctx, e.message ?: "Admin error", Toast.LENGTH_LONG).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    // 🎨 Light palette
    val pageBg = Brush.verticalGradient(
        colors = listOf(Color(0xFFF7F8FC), Color(0xFFF2F4FA), Color(0xFFF7F8FC))
    )
    val cardBg = Color.White
    val textMain = Color(0xFF111827)
    val textSub = Color(0xFF6B7280)
    val border = Color(0xFFE5E7EB)
    val accent = Color(0xFF4F46E5)     // Indigo
    val accentSoft = Color(0xFFEEF2FF)


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(14.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(accentSoft, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = accent
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                "Admin Console",
                                color = textMain,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Manage users & view system events",
                                color = textSub,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        FilledTonalIconButton(onClick = { refresh() }, enabled = !loading) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = accent)
                        }
                    }

                    if (loading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = accent,
                            trackColor = accentSoft
                        )
                    }

                    // Tabs
                    TabRow(
                        selectedTabIndex = tab,
                        containerColor = Color.Transparent,
                        contentColor = accent
                    ) {
                        Tab(
                            selected = tab == 0,
                            onClick = { tab = 0 },
                            text = { Text("Users") },
                            icon = {
                                Icon(
                                    Icons.Default.SupervisorAccount,
                                    contentDescription = null
                                )
                            }
                        )
                        Tab(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            text = { Text("Events") },
                            icon = { Icon(Icons.Default.EventNote, contentDescription = null) }
                        )
                    }
                }
            }
        }

        // Content: Users
        if (tab == 0) {
            item {
                SectionTitle(
                    title = "Users",
                    subtitle = "Up to 50 users",
                    textMain = textMain,
                    textSub = textSub
                )
            }

            if (!loading && users.isEmpty()) {
                item { EmptyCard("No users found", "Try refresh again.", cardBg, textMain, textSub, border) }
            } else {
                items(users.take(50), key = { it.id }) { u ->
                    UserAdminCardLight(
                        u = u,
                        cardBg = cardBg,
                        textMain = textMain,
                        textSub = textSub,
                        border = border,
                        accent = accent,
                        accentSoft = accentSoft,
                        loading = loading,
                        onApprove = {
                            scope.launch {
                                try {
                                    DemoApi.adminApproveKyc(u.id)
                                    Toast.makeText(ctx, "KYC Approved ✅", Toast.LENGTH_SHORT).show()
                                    refresh()
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, e.message ?: "Approve failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onToggleFreeze = {
                            scope.launch {
                                try {
                                    if (u.isFrozen) DemoApi.adminUnfreeze(u.id) else DemoApi.adminFreeze(u.id)
                                    Toast.makeText(ctx, if (u.isFrozen) "Unfrozen ✅" else "Frozen ✅", Toast.LENGTH_SHORT).show()
                                    refresh()
                                    bumpRefresh()
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, e.message ?: "Freeze action failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }

        // Content: Events
        if (tab == 1) {
            item {
                SectionTitle(
                    title = "Events",
                    subtitle = "Up to 50 events",
                    textMain = textMain,
                    textSub = textSub
                )
            }

            if (!loading && events.isEmpty()) {
                item { EmptyCard("No events yet", "Once actions happen, they’ll appear here.", cardBg, textMain, textSub, border) }
            } else {
                items(events.take(50)) { e ->
                    EventCardLight(
                        e = e,
                        cardBg = cardBg,
                        textMain = textMain,
                        textSub = textSub,
                        border = border,
                        accent = accent,
                        accentSoft = accentSoft
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String, textMain: Color, textSub: Color) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = textMain, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = textSub, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EmptyCard(
    title: String,
    subtitle: String,
    cardBg: Color,
    textMain: Color,
    textSub: Color,
    border: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, border),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = textMain, fontWeight = FontWeight.Bold)
            Text(subtitle, color = textSub)
        }
    }
}

@Composable
private fun UserAdminCardLight(
    u: AdminUserDto,
    cardBg: Color,
    textMain: Color,
    textSub: Color,
    border: Color,
    accent: Color,
    accentSoft: Color,
    loading: Boolean,
    onApprove: () -> Unit,
    onToggleFreeze: () -> Unit
) {
    val kycBg = if (u.kycApproved) Color(0xFFECFDF5) else Color(0xFFFFF7ED)
    val kycFg = if (u.kycApproved) Color(0xFF047857) else Color(0xFFB45309)

    val freezeBg = if (u.isFrozen) Color(0xFFFEF2F2) else Color(0xFFEEF2FF)
    val freezeFg = if (u.isFrozen) Color(0xFFB91C1C) else accent

    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, border),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accentSoft, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = accent)
                }
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "User: ${u.id.take(14)}…",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Phone: ${u.phone ?: "-"}",
                        color = textSub,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Surface(color = kycBg, shape = RoundedCornerShape(999.dp)) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = kycFg)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (u.kycApproved) "KYC Approved" else "KYC Pending",
                            color = kycFg,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onApprove,
                    enabled = !loading && !u.kycApproved,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, border),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accent
                    )
                ) {
                    Text("Approve KYC", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onToggleFreeze,
                    enabled = !loading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = freezeBg, contentColor = freezeFg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(if (u.isFrozen) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (u.isFrozen) "Unfreeze" else "Freeze", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EventCardLight(
    e: AdminEventDto,
    cardBg: Color,
    textMain: Color,
    textSub: Color,
    border: Color,
    accent: Color,
    accentSoft: Color
) {
    Card(
        shape = RoundedCornerShape(18.dp),
//        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, border),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentSoft, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EventNote, contentDescription = null, tint = accent)
                }
                Spacer(Modifier.width(10.dp))
                Text(e.action, color = textMain, fontWeight = FontWeight.SemiBold)
            }

            e.targetId?.let { Text("Target: ${it.take(18)}…", color = textSub) }
            e.meta?.let { Text("Meta: $it", color = textSub, style = MaterialTheme.typography.bodySmall) }
            Text(e.createdAt, color = textSub, style = MaterialTheme.typography.bodySmall)
        }
    }
}
