package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CageEntity
import com.example.data.TaskEntity
import com.example.data.VaccinationEntity
import com.example.data.WarehouseEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppScreen(viewModel: FarmViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val isCloudSynced by viewModel.isCloudSynced.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val loggedInUserDisplayName by viewModel.loggedInUserDisplayName.collectAsStateWithLifecycle()

    // Notifications System
    val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Dialog trigger states
    var showCageDialog by remember { mutableStateOf(false) }
    var selectedCageForEdit by remember { mutableStateOf<CageEntity?>(null) }
    var selectedCageForWorkerStatus by remember { mutableStateOf<CageEntity?>(null) }
    var defaultWardNameForAdd by remember { mutableStateOf("") }

    var showVaccinationDialog by remember { mutableStateOf(false) }
    var selectedVaccinationForEdit by remember { mutableStateOf<VaccinationEntity?>(null) }

    var showWarehouseDialog by remember { mutableStateOf(false) }
    var selectedWarehouseForEdit by remember { mutableStateOf<WarehouseEntity?>(null) }

    var showTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskForEdit by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                // Top row with app icon, name and role badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Round premium farm logo avatar
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_farm_logo),
                            contentDescription = "Farm Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(20.dp))
                        )
                        Text(
                            text = "مزارع العمدة",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NaturalDeepText
                        )
                    }

                    // Role Switcher & Cloud Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Notifications Bell Button with dynamic badge count
                        Box(
                            modifier = Modifier
                                .clickable { showNotificationDialog = true }
                                .padding(4.dp)
                                .testTag("notification_bell")
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "الإشعارات والتنبيهات",
                                tint = NaturalDeepText,
                                modifier = Modifier.size(24.dp)
                            )
                            if (activeAlerts.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ErrorRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activeAlerts.size.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Cloud Sync Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSyncing) NaturalTertiary.copy(alpha = 0.15f)
                                    else if (isCloudSynced) Color(0xFFE2E4D8)
                                    else AlertOrange.copy(alpha = 0.15f)
                                )
                                .clickable { viewModel.forceManualSync() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = NaturalTertiary
                                    )
                                } else if (isCloudSynced) {
                                    Icon(
                                        Icons.Default.CloudDone, 
                                        contentDescription = "سحابي", 
                                        tint = NaturalPrimary, 
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.CloudQueue, 
                                        contentDescription = "غير متزامن", 
                                        tint = AlertOrange, 
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Profile display & Logout button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE2E4D8))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (currentRole == "MANAGER") Icons.Default.AdminPanelSettings else Icons.Default.Engineering,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = loggedInUserDisplayName.ifEmpty { if (currentRole == "MANAGER") "المدير" else "العامل" },
                                color = NaturalDeepText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // Elegant Logout Button with a red icon
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("logout_button")
                            ) {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "تسجيل الخروج",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Production House Subheader styled card: bg-white/40
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "عنبر الإنتاج",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTertiary.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "المبنى A - قفص #24",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalDeepText
                        )
                    }
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "إعدادات",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.drawBehind {
                    val strokeWidthPx = 1.dp.toPx()
                    drawLine(
                        color = NaturalBorder,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeWidthPx
                    )
                }
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.HomeWork, contentDescription = "العنابر") },
                    label = { Text("العنابر", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_cages")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Medication, contentDescription = "التطعيمات") },
                    label = { Text("التطعيمات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_vaccinations")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "المستودع") },
                    label = { Text("المستودع", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_warehouse")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "المهام") },
                    label = { Text("المهام", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_tasks")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "التقارير") },
                    label = { Text("التقارير", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_reports")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Conspicuous Alert Banner at the top of the tab content if alerts are present
                if (activeAlerts.isNotEmpty()) {
                    var isBannerDismissed by remember { mutableStateOf(false) }
                    // Re-enable banner if alerts count changes
                    var lastAlertsCount by remember { mutableStateOf(activeAlerts.size) }
                    if (activeAlerts.size != lastAlertsCount) {
                        isBannerDismissed = false
                        lastAlertsCount = activeAlerts.size
                    }
                    
                    if (!isBannerDismissed) {
                        val firstAlert = activeAlerts.first()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp)
                                .testTag("active_alerts_banner"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (firstAlert.isUrgent) Color(0xFFFDF2F2) else Color(0xFFFFFBEB)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (firstAlert.isUrgent) Color(0xFFF8B4B4) else Color(0xFFFDE68A)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (firstAlert.isUrgent) Icons.Default.ErrorOutline else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (firstAlert.isUrgent) Color(0xFF9B1C1C) else Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "تنبيه هام للعمل: ${firstAlert.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (firstAlert.isUrgent) Color(0xFF9B1C1C) else Color(0xFFB45309),
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = firstAlert.description,
                                        fontSize = 11.sp,
                                        color = if (firstAlert.isUrgent) Color(0xFF771D1D) else Color(0xFF78350F),
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                TextButton(
                                    onClick = { showNotificationDialog = true },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (firstAlert.isUrgent) Color(0xFF9B1C1C) else Color(0xFFB45309)
                                    )
                                ) {
                                    Text("عرض (${activeAlerts.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { isBannerDismissed = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "إغلاق",
                                        tint = if (firstAlert.isUrgent) Color(0xFF9B1C1C) else Color(0xFFB45309),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                0 -> CagesTab(
                    viewModel = viewModel,
                    currentRole = currentRole,
                    onAddCage = { wardName ->
                        defaultWardNameForAdd = wardName
                        selectedCageForEdit = null
                        showCageDialog = true
                    },
                    onEditCage = { cage ->
                        selectedCageForEdit = cage
                        defaultWardNameForAdd = cage.cageNumber
                        showCageDialog = true
                    },
                    onChangeStatus = { cage ->
                        selectedCageForWorkerStatus = cage
                    }
                )
                1 -> VaccinationsTab(
                    viewModel = viewModel,
                    currentRole = currentRole,
                    onAddVaccination = {
                        selectedVaccinationForEdit = null
                        showVaccinationDialog = true
                    },
                    onEditVaccination = { vac ->
                        selectedVaccinationForEdit = vac
                        showVaccinationDialog = true
                    }
                )
                2 -> WarehouseTab(
                    viewModel = viewModel,
                    currentRole = currentRole,
                    onAddItem = {
                        selectedWarehouseForEdit = null
                        showWarehouseDialog = true
                    },
                    onEditItem = { item ->
                        selectedWarehouseForEdit = item
                        showWarehouseDialog = true
                    }
                )
                3 -> TasksTab(
                    viewModel = viewModel,
                    currentRole = currentRole,
                    onAddTask = {
                        selectedTaskForEdit = null
                        showTaskDialog = true
                    },
                    onEditTask = { task ->
                        selectedTaskForEdit = task
                        showTaskDialog = true
                    }
                )
                4 -> ReportsTab(
                    viewModel = viewModel
                )
            }
        }
    }
}
}

    // ==========================================
    // Dialogs & Modals
    // ==========================================

    // Cage Save/Edit Dialog (Manager Only)
    if (showCageDialog && currentRole == "MANAGER") {
        CageFormDialog(
            cage = selectedCageForEdit,
            defaultWard = defaultWardNameForAdd,
            onDismiss = { showCageDialog = false },
            onSave = { cNum, bNum, breed, status, eggs, laidDate, cond, squabs, weights, reason ->
                viewModel.saveCage(
                    id = selectedCageForEdit?.id ?: 0,
                    cageNumber = cNum,
                    nestBoxNumber = bNum,
                    breed = breed,
                    pigeonStatus = status,
                    eggCount = eggs,
                    eggLaidDate = laidDate,
                    eggCondition = cond,
                    squabsCount = squabs,
                    squabsWeights = weights,
                    stoppedReason = reason
                )
                showCageDialog = false
            }
        )
    }

    // Cage Status Dialog (Worker & Manager)
    if (selectedCageForWorkerStatus != null) {
        CageStatusUpdateDialog(
            cage = selectedCageForWorkerStatus!!,
            onDismiss = { selectedCageForWorkerStatus = null },
            onSave = { status, eggs, laidDate, cond, squabs, weights, reason ->
                viewModel.saveCage(
                    id = selectedCageForWorkerStatus!!.id,
                    cageNumber = selectedCageForWorkerStatus!!.cageNumber,
                    nestBoxNumber = selectedCageForWorkerStatus!!.nestBoxNumber,
                    breed = selectedCageForWorkerStatus!!.breed,
                    pigeonStatus = status,
                    eggCount = eggs,
                    eggLaidDate = laidDate,
                    eggCondition = cond,
                    squabsCount = squabs,
                    squabsWeights = weights,
                    stoppedReason = reason
                )
                selectedCageForWorkerStatus = null
            }
        )
    }

    // Vaccination Dialog
    if (showVaccinationDialog && currentRole == "MANAGER") {
        VaccinationFormDialog(
            vac = selectedVaccinationForEdit,
            onDismiss = { showVaccinationDialog = false },
            onSave = { title, date, status, instructions ->
                viewModel.saveVaccination(
                    id = selectedVaccinationForEdit?.id ?: 0,
                    title = title,
                    scheduledDate = date,
                    status = status,
                    instructions = instructions
                )
                showVaccinationDialog = false
            }
        )
    }

    // Warehouse Dialog
    if (showWarehouseDialog && currentRole == "MANAGER") {
        WarehouseFormDialog(
            item = selectedWarehouseForEdit,
            onDismiss = { showWarehouseDialog = false },
            onSave = { name, category, qty, unit, consumption, threshold ->
                viewModel.saveWarehouseItem(
                    id = selectedWarehouseForEdit?.id ?: 0,
                    itemName = name,
                    category = category,
                    currentQuantity = qty,
                    unit = unit,
                    dailyConsumptionRate = consumption,
                    lowStockThreshold = threshold
                )
                showWarehouseDialog = false
            }
        )
    }

    // Task Dialog
    if (showTaskDialog && currentRole == "MANAGER") {
        TaskFormDialog(
            task = selectedTaskForEdit,
            onDismiss = { showTaskDialog = false },
            onSave = { title, desc, rep, freq, assigned, status, time ->
                viewModel.saveTask(
                    id = selectedTaskForEdit?.id ?: 0,
                    title = title,
                    description = desc,
                    isRepetitive = rep,
                    frequency = freq,
                    assignedTo = assigned,
                    status = status,
                    alertTime = time
                )
                showTaskDialog = false
            }
        )
    }

    // Notification Center Dialog
    if (showNotificationDialog) {
        NotificationCenterDialog(
            alerts = activeAlerts,
            onDismiss = { showNotificationDialog = false },
            onNavigateToTab = { tabIndex ->
                viewModel.selectTab(tabIndex)
            }
        )
    }
}

// ==========================================
// Tab Content UI implementations
// ==========================================

@Composable
fun CagesTab(
    viewModel: FarmViewModel,
    currentRole: String,
    onAddCage: (String) -> Unit,
    onEditCage: (CageEntity) -> Unit,
    onChangeStatus: (CageEntity) -> Unit
) {
    val cages by viewModel.allCages.collectAsStateWithLifecycle()
    var selectedWard by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("الكل") }

    if (selectedWard == null) {
        // ==========================================
        // WARDS LIST VIEW (شاشة عرض العنابر)
        // ==========================================
        val wardsGrouped = cages.groupBy { it.cageNumber }
        
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header & Add Ward Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "عنابر الإنتاج والتربية",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "اختر عنبرًا لإدارة ومتابعة الأقفاص والعيون بداخله",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentRole == "MANAGER") {
                    Button(
                        onClick = { onAddCage("") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_ward_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("عنبر/عين", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (wardsGrouped.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.HomeWork,
                    text = "لا توجد عنابر مسجلة حالياً. اضغط على 'عنبر/عين' لإضافة أول عنبر."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(wardsGrouped.keys.sorted(), key = { it }) { wardName ->
                        val wardCages = wardsGrouped[wardName] ?: emptyList()
                        val eggCount = wardCages.count { it.pigeonStatus == "وجود بيض" }
                        val squabCount = wardCages.count { it.pigeonStatus == "وجود فراخ" }
                        val prepCount = wardCages.count { it.pigeonStatus == "تجهيز للتكاثر" }
                        val stoppedCount = wardCages.count { it.pigeonStatus == "توقف وضع البيض" }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedWard = wardName }
                                .testTag("ward_card_$wardName"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Ward Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.HomeWork,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = wardName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Status Badges Grid Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Total Boxes
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("العيون", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            Text("${wardCages.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }

                                    // Active Eggs
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (eggCount > 0) WarningYellow.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("به بيض", fontSize = 10.sp, color = if (eggCount > 0) AlertOrange else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$eggCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (eggCount > 0) AlertOrange else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    // Active Squabs
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (squabCount > 0) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("به زغاليل", fontSize = 10.sp, color = if (squabCount > 0) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$squabCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (squabCount > 0) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    // Stopped or other
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (stoppedCount > 0) ErrorRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("متوقف", fontSize = 10.sp, color = if (stoppedCount > 0) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$stoppedCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (stoppedCount > 0) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // ==========================================
        // CAGES/EYES IN SELECTED WARD VIEW (أقفاص العين المحددة)
        // ==========================================
        val wardName = selectedWard!!
        val wardCages = cages.filter { it.cageNumber == wardName }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Navigation Breadcrumb / Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { selectedWard = null },
                        modifier = Modifier.testTag("back_to_wards_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة للعنابر"
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = wardName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "إجمالي عيون وأقفاص العنبر: ${wardCages.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (currentRole == "MANAGER") {
                    Button(
                        onClick = { onAddCage(wardName) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_cage_to_ward_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة عين", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search within this ward & Status filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث عن رقم العين أو السلالة...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("cage_search_input")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Filter Row
            val filters = listOf("الكل", "تجهيز للتكاثر", "وجود بيض", "وجود فراخ", "توقف وضع البيض")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                filters.forEach { filter ->
                    val selected = statusFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { statusFilter = filter }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter ward cages
            val filteredCages = wardCages.filter { cage ->
                val matchQuery = cage.nestBoxNumber.contains(searchQuery, ignoreCase = true) ||
                                 cage.breed.contains(searchQuery, ignoreCase = true)
                val matchFilter = statusFilter == "الكل" || cage.pigeonStatus == statusFilter
                matchQuery && matchFilter
            }

            if (filteredCages.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Layers,
                    text = "لا توجد أقفاص أو عيون مطابقة للبحث داخل هذا العنبر."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCages, key = { it.id }) { cage ->
                        CageCard(
                            cage = cage,
                            currentRole = currentRole,
                            onEdit = { onEditCage(cage) },
                            onDelete = { viewModel.deleteCage(cage) },
                            onChangeStatus = { onChangeStatus(cage) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CageCard(
    cage: CageEntity,
    currentRole: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangeStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Cage details and Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Grid4x4, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${cage.cageNumber} - ${cage.nestBoxNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "السلالة: ${cage.breed}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pigeon Status Badge
                val (badgeColor, textColor) = when (cage.pigeonStatus) {
                    "وجود بيض" -> WarningYellow.copy(alpha = 0.2f) to AlertOrange
                    "وجود فراخ" -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
                    "توقف وضع البيض" -> ErrorRed.copy(alpha = 0.12f) to ErrorRed
                    else -> SlateGray.copy(alpha = 0.15f) to SlateGray
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cage.pigeonStatus,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // Dynamic status subdetails
            when (cage.pigeonStatus) {
                "وجود بيض" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Circle, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "إجمالي البيض بالأعشاش: ${cage.eggCount} بيضات", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        if (cage.eggLaidDate.isNotEmpty()) {
                            Text(text = "📅 تاريخ وضع البيض: ${cage.eggLaidDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            // Alerts
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🔍 موعد فحص البيض (تخصيب):", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(cage.eggCheckDate, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🐣 موعد الفقس المتوقع:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(cage.eggHatchingDate, fontSize = 12.sp, color = AlertOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🥚 حالة البيض الحالي:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            val condColor = if (cage.eggCondition == "طبيعي") SuccessGreen else ErrorRed
                            Text(text = cage.eggCondition, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = condColor)
                        }
                    }
                }
                "وجود فراخ" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "عدد الفراخ (الزغاليل): ${cage.squabsCount} زغلول", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        if (cage.squabsWeights.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Scale, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "متابعة الأوزان الحالية: ${cage.squabsWeights.split(",").joinToString(" ج، ")} ج",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                "توقف وضع البيض" -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ErrorRed.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سبب التوقف المسجل: ${cage.stoppedReason.ifEmpty { "غير محدد" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }
                else -> {
                    Text(
                        text = "🕊️ الزوج في مرحلة المراقبة والراحة والتجهيز للدورة القادمة للتكاثر.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Change status is available for BOTH roles
                TextButton(
                    onClick = onChangeStatus,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تغيير الحالة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (currentRole == "MANAGER") {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VaccinationsTab(
    viewModel: FarmViewModel,
    currentRole: String,
    onAddVaccination: () -> Unit,
    onEditVaccination: (VaccinationEntity) -> Unit
) {
    val vaccinations by viewModel.allVaccinations.collectAsStateWithLifecycle()
    var filterStatus by remember { mutableStateOf("الكل") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("جداول التطعيمات والتحصينات", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            
            if (currentRole == "MANAGER") {
                Button(
                    onClick = onAddVaccination,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_vaccine_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة جدول", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Pill Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val statusOptions = listOf("الكل", "جديد", "تم الإعطاء")
            statusOptions.forEach { option ->
                val active = filterStatus == option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { filterStatus = option }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = option,
                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredVac = vaccinations.filter { 
            filterStatus == "الكل" || it.status == filterStatus
        }

        if (filteredVac.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Healing,
                text = "لا توجد تطعيمات أو أدوية مجدولة حالياً."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredVac, key = { it.id }) { vac ->
                    VaccinationCard(
                        vac = vac,
                        currentRole = currentRole,
                        onToggleStatus = {
                            val nextStatus = if (vac.status == "جديد") "تم الإعطاء" else "جديد"
                            viewModel.updateVaccinationStatus(vac, nextStatus)
                        },
                        onEdit = { onEditVaccination(vac) },
                        onDelete = { viewModel.deleteVaccination(vac) }
                    )
                }
            }
        }
    }
}

@Composable
fun VaccinationCard(
    vac: VaccinationEntity,
    currentRole: String,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Vaccines, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = vac.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Status Switch indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (vac.status == "تم الإعطاء") SuccessGreen.copy(alpha = 0.15f)
                            else AlertOrange.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = vac.status,
                        color = if (vac.status == "تم الإعطاء") SuccessGreen else AlertOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📅 تاريخ الإعطاء المجدول: ${vac.scheduledDate}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (vac.instructions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = vac.instructions,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggling status is available for BOTH roles (Worker needs to check/uncheck giving)
                Button(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vac.status == "تم الإعطاء") SlateGray else SuccessGreen
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        if (vac.status == "تم الإعطاء") Icons.Default.Undo else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (vac.status == "تم الإعطاء") "إرجاع كـ معلق" else "تأكيد الإعطاء والتحصين",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (currentRole == "MANAGER") {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WarehouseTab(
    viewModel: FarmViewModel,
    currentRole: String,
    onAddItem: () -> Unit,
    onEditItem: (WarehouseEntity) -> Unit
) {
    val items by viewModel.allWarehouseItems.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("الكل") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("المستودع والمخزن (الأعلاف والمعدات)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            
            if (currentRole == "MANAGER") {
                Button(
                    onClick = onAddItem,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_warehouse_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة صنف", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category tabs
        val categories = listOf("الكل", "أعلاف", "معدات", "أدوية")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val active = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning banner if any items are low in stock
        val lowStockItems = items.filter { it.currentQuantity <= it.lowStockThreshold }
        if (lowStockItems.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("low_stock_warning_banner"),
                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "تحذير نقص مخزون",
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تنبيـــه: يوجد ${lowStockItems.size} صنف يقل مخزونه عن حد الأمان!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "يرجى توريد هذه النواقص فوراً: " + lowStockItems.joinToString("، ") { "${it.itemName} (${it.currentQuantity} ${it.unit})" },
                            fontSize = 11.sp,
                            color = ErrorRed.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        val filteredItems = items.filter { 
            selectedCategory == "الكل" || it.category == selectedCategory
        }

        if (filteredItems.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Inventory,
                text = "المخزن فارغ تماماً من الأصناف تحت هذا التصنيف."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    WarehouseItemCard(
                        item = item,
                        currentRole = currentRole,
                        onEdit = { onEditItem(item) },
                        onDelete = { viewModel.deleteWarehouseItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun WarehouseItemCard(
    item: WarehouseEntity,
    currentRole: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = item.currentQuantity <= item.lowStockThreshold

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) ErrorRed.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isLowStock) 1.5.dp else 1.dp,
            color = if (isLowStock) ErrorRed.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLowStock) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (item.category) {
                            "أعلاف" -> Icons.Default.Agriculture
                            "أدوية" -> Icons.Default.Healing
                            else -> Icons.Default.Handyman
                        }
                        Icon(icon, contentDescription = null, tint = if (isLowStock) ErrorRed else MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.itemName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isLowStock) ErrorRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(text = "الفئة: ${item.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (isLowStock) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ErrorRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(12.dp))
                            Text("مخزون منخفض!", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("الكمية الحالية المتوفرة:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${item.currentQuantity} ${item.unit}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) ErrorRed else MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text("حد إنذار الأمان:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${item.lowStockThreshold} ${item.unit}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Feed consumption & remaining days
            if (item.category == "أعلاف" && item.dailyConsumptionRate > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                val daysLeft = (item.currentQuantity / item.dailyConsumptionRate).toInt()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = WarmBrownTertiary, modifier = Modifier.size(14.dp))
                        Text("الاستهلاك اليومي: ${item.dailyConsumptionRate} ${item.unit}", fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (daysLeft < 5) ErrorRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "يكفي لـ: $daysLeft يوم تقريباً",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (daysLeft < 5) ErrorRed else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (currentRole == "MANAGER") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTab(
    viewModel: FarmViewModel,
    currentRole: String,
    onAddTask: () -> Unit,
    onEditTask: (TaskEntity) -> Unit
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    var filterStatus by remember { mutableStateOf("الكل") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tab Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (currentRole == "WORKER") "قائمة مهامك اليومية" else "مهام العمل والتشغيل اليومي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (currentRole == "WORKER") "أكمل مهامك اليومية وحدث حالتها مباشرة بلمسة واحدة" else "متابعة مستويات الإنجاز والإنتاج اليومي للعنابر",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (currentRole == "MANAGER") {
                Button(
                    onClick = onAddTask,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_task_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مهمة", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Progress Tracker Dashboard Card (Very rewarding visual for workers)
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.status == "تم التنفيذ" }
        val completionProgress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "مؤشر إنجاز المهام اليومي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "$completedTasks من أصل $totalTasks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = completionProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SuccessGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        totalTasks == 0 -> "لم يتم إنشاء مهام لليوم بعد."
                        completedTasks == totalTasks -> "🎉 رائع وممتاز! لقد أنجزت جميع مهام اليوم بنجاح وصحة تامة."
                        completedTasks > 0 -> "⚡ تقدم ممتاز! واصل العمل لإتمام باقي المهام المتبقية."
                        else -> "💪 ابدأ يومك بهمة ونشاط وسجل أولى المهام المنجزة!"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task filters
        val options = listOf("الكل", "قيد الانتظار", "تم التنفيذ")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val active = filterStatus == option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { filterStatus = option }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = option,
                        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredTasks = tasks.filter { 
            filterStatus == "الكل" || it.status == filterStatus
        }

        if (filteredTasks.isEmpty()) {
            EmptyStateView(
                icon = Icons.AutoMirrored.Filled.Assignment,
                text = "لا توجد مهام عمل مطابقة تحت هذا التصنيف."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        currentRole = currentRole,
                        onToggleStatus = {
                            val next = if (task.status == "قيد الانتظار") "تم التنفيذ" else "قيد الانتظار"
                            viewModel.updateTaskStatus(task, next)
                        },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    currentRole: String,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == "تم التنفيذ"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleStatus() }
            .testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) SuccessGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) SuccessGreen.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { onToggleStatus() },
                        colors = CheckboxDefaults.colors(checkedColor = SuccessGreen),
                        modifier = Modifier.testTag("task_checkbox_${task.id}")
                    )
                    Column {
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isCompleted) SlateGray else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                            Text("وقت التنبيه: ${task.alertTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Custom Indicator pill
                Column(horizontalAlignment = Alignment.End) {
                    if (task.isRepetitive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "تكرار: ${task.frequency}", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (task.isAddedByManager) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AlertOrange.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "تكليف إداري", color = AlertOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(start = 34.dp)
                )
            }

            // Deletion controls are ONLY visible for MANAGER role (never displayed to workers)
            if (currentRole == "MANAGER") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_task_button_${task.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف المهمة", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsTab(viewModel: FarmViewModel) {
    val cages by viewModel.allCages.collectAsStateWithLifecycle()
    val warehouse by viewModel.allWarehouseItems.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val vaccinations by viewModel.allVaccinations.collectAsStateWithLifecycle()

    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val reportText by viewModel.analysisReport.collectAsStateWithLifecycle()
    val syncLogs by viewModel.syncLogs.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Summary Header
        item {
            Text("التقارير وسجلات الكفاءة والإنتاج", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text("متابعة حية وشاملة لأداء المزرعة محلياً وسحابياً", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Summary Counters Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Production KPI
                Card(
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Egg, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("البيض النشط", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${cages.sumOf { it.eggCount }} بيضة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Squab KPI
                Card(
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChildCare, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("الفراخ النامية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${cages.sumOf { it.squabsCount }} زغلول", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Total Cages KPI
                Card(
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("العيون النشطة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${cages.size} زوج", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Ward Density Chart (كثافة العنابر)
        item {
            WardsDensityChart(cages = cages)
        }

        // Gemini smart advisory panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("مستشار الإنتاج الذكي", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Text("تحليلات ومقترحات بيطرية مدعومة بـ Gemini AI", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = { viewModel.generateGeminiReport() },
                            enabled = !isAnalyzing,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("ai_analysis_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استشارة ذكية", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (reportText.isNotEmpty()) {
                        Text(
                            text = reportText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        )
                    } else if (isAnalyzing) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text("جاري فحص حالة المزرعة ومخزون الأعلاف والتطعيمات...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اضغط على زر \"استشارة ذكية\" لتوليد تقرير شامل حول كفاية الأعلاف بالمزرعة، وجدولة الأدوية، وحل مشاكل توقف وضع البيض والخصوبة للزغاليل.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Cloud sync history / logs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("سجلات ومزامنة السحابة (Cloud Backup)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("يتم أرشفة السجلات محلياً ورفعها تلقائياً على خوادم السحابة المشفرة.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(10.dp)
                    ) {
                        Text("📋 سجل العمليات والمزامنة الأخيرة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                        
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(syncLogs) { log ->
                                Text(text = log, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Empty States and helpers
// ==========================================

@Composable
fun EmptyStateView(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), 
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = text, 
            fontSize = 13.sp, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// Helper to load icons
@Composable
fun imageNametoIcon(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return if (name == "pigeon") Icons.Default.Pets else Icons.Default.Help
}

// ==========================================
// Forms and Input Dialogs (All dialogs are localized, full validation, beautiful styling)
// ==========================================

@Composable
fun CageFormDialog(
    cage: CageEntity?,
    defaultWard: String = "",
    onDismiss: () -> Unit,
    onSave: (
        cageNumber: String,
        nestBoxNumber: String,
        breed: String,
        status: String,
        eggCount: Int,
        eggLaidDate: String,
        eggCondition: String,
        squabsCount: Int,
        squabsWeights: String,
        stoppedReason: String
    ) -> Unit
) {
    var cageNumber by remember { mutableStateOf(cage?.cageNumber ?: defaultWard) }
    var nestBoxNumber by remember { mutableStateOf(cage?.nestBoxNumber ?: "") }
    var breed by remember { mutableStateOf(cage?.breed ?: "لاحم فرنسي جامبو") }
    var pigeonStatus by remember { mutableStateOf(cage?.pigeonStatus ?: "تجهيز للتكاثر") }
    
    var eggCount by remember { mutableStateOf(cage?.eggCount?.toString() ?: "2") }
    var eggLaidDate by remember { mutableStateOf(cage?.eggLaidDate ?: "2026-07-07") }
    var eggCondition by remember { mutableStateOf(cage?.eggCondition ?: "طبيعي") }
    
    var squabsCount by remember { mutableStateOf(cage?.squabsCount?.toString() ?: "2") }
    var squabsWeights by remember { mutableStateOf(cage?.squabsWeights ?: "") }
    
    var stoppedReason by remember { mutableStateOf(cage?.stoppedReason ?: "") }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (cage == null) "إضافة زوج/عش حمام جديد" else "تعديل بيانات القفص والعنبر",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = cageNumber,
                    onValueChange = { cageNumber = it },
                    label = { Text("رقم أو اسم العنبر (مثال: عنبر 1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_cage_number")
                )

                OutlinedTextField(
                    value = nestBoxNumber,
                    onValueChange = { nestBoxNumber = it },
                    label = { Text("رقم العين / الصندوق داخل القفص (مثال: عين 04)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_nest_number")
                )

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("سلالة الحمام") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Status drop down selections styled as a list
                Text("حالة الحمام الحالية في القفص:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                val states = listOf("تجهيز للتكاثر", "وجود بيض", "وجود فراخ", "توقف وضع البيض")
                Column {
                    states.forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pigeonStatus = st }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = pigeonStatus == st, onClick = { pigeonStatus = st })
                            Text(st, fontSize = 12.sp)
                        }
                    }
                }

                // Conditional fields
                when (pigeonStatus) {
                    "وجود بيض" -> {
                        Divider()
                        Text("تفاصيل وضع البيض وعلاجه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = eggCount,
                            onValueChange = { eggCount = it },
                            label = { Text("عدد البيض المسجل بالأعشاش") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("input_egg_count")
                        )

                        OutlinedTextField(
                            value = eggLaidDate,
                            onValueChange = { eggLaidDate = it },
                            label = { Text("تاريخ وضع البيض (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_egg_laid_date")
                        )

                        Text("حالة الخصوبة والتلقيح للبيض:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val eggConditions = listOf("طبيعي", "غير مخصب", "توقفت عملية التحضين")
                        Column {
                            eggConditions.forEach { ec ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { eggCondition = ec }
                                ) {
                                    RadioButton(selected = eggCondition == ec, onClick = { eggCondition = ec })
                                    Text(ec, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    "وجود فراخ" -> {
                        Divider()
                        Text("تفاصيل الزغاليل النامية:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = squabsCount,
                            onValueChange = { squabsCount = it },
                            label = { Text("عدد الفراخ بالأعشاش") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("input_squabs_count")
                        )

                        OutlinedTextField(
                            value = squabsWeights,
                            onValueChange = { squabsWeights = it },
                            label = { Text("أوزان الفراخ الحالية بالجرام متبوعة بفواصل (مثال: 150,165)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_squabs_weights")
                        )
                    }
                    "توقف وضع البيض" -> {
                        Divider()
                        Text("عطل أو توقف التبويض والإنتاج:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        Text("سبب توقف الإنتاج الحالي:", fontSize = 11.sp)
                        val reasons = listOf("مرحلة تبديل الريش (القلش)", "إصابة بمرض معوي أو تنفسي", "مشاكل صحية أخرى أو إجهاد")
                        Column {
                            reasons.forEach { r ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { stoppedReason = r }
                                ) {
                                    RadioButton(selected = stoppedReason == r, onClick = { stoppedReason = r })
                                    Text(r, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                if (showError) {
                    Text("يرجى ملء اسم العنبر ورقم العين لحفظ السجل.", color = ErrorRed, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (cageNumber.trim().isEmpty() || nestBoxNumber.trim().isEmpty()) {
                                showError = true
                            } else {
                                onSave(
                                    cageNumber,
                                    nestBoxNumber,
                                    breed,
                                    pigeonStatus,
                                    eggCount.toIntOrNull() ?: 0,
                                    eggLaidDate,
                                    eggCondition,
                                    squabsCount.toIntOrNull() ?: 0,
                                    squabsWeights,
                                    stoppedReason
                                )
                            }
                        },
                        modifier = Modifier.testTag("dialog_save_cage_button")
                    ) {
                        Text("حفظ البيانات")
                    }
                }
            }
        }
    }
}

@Composable
fun CageStatusUpdateDialog(
    cage: CageEntity,
    onDismiss: () -> Unit,
    onSave: (
        status: String,
        eggCount: Int,
        eggLaidDate: String,
        eggCondition: String,
        squabsCount: Int,
        squabsWeights: String,
        stoppedReason: String
    ) -> Unit
) {
    var pigeonStatus by remember { mutableStateOf(cage.pigeonStatus) }
    var eggCount by remember { mutableStateOf(cage.eggCount.toString()) }
    var eggLaidDate by remember { mutableStateOf(cage.eggLaidDate.ifEmpty { "2026-07-07" }) }
    var eggCondition by remember { mutableStateOf(cage.eggCondition) }
    var squabsCount by remember { mutableStateOf(cage.squabsCount.toString()) }
    var squabsWeights by remember { mutableStateOf(cage.squabsWeights) }
    var stoppedReason by remember { mutableStateOf(cage.stoppedReason) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "تحديث حالة الطيور: قفص ${cage.cageNumber} - عين ${cage.nestBoxNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                val states = listOf("تجهيز للتكاثر", "وجود بيض", "وجود فراخ", "توقف وضع البيض")
                states.forEach { st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { pigeonStatus = st }
                    ) {
                        RadioButton(selected = pigeonStatus == st, onClick = { pigeonStatus = st })
                        Text(st, fontSize = 12.sp)
                    }
                }

                when (pigeonStatus) {
                    "وجود بيض" -> {
                        Divider()
                        OutlinedTextField(
                            value = eggCount,
                            onValueChange = { eggCount = it },
                            label = { Text("عدد البيض") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = eggLaidDate,
                            onValueChange = { eggLaidDate = it },
                            label = { Text("تاريخ الوضع (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("خصوبة البيض:", fontSize = 11.sp)
                        val eggConditions = listOf("طبيعي", "غير مخصب", "توقفت عملية التحضين")
                        eggConditions.forEach { ec ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { eggCondition = ec }
                            ) {
                                RadioButton(selected = eggCondition == ec, onClick = { eggCondition = ec })
                                    Text(ec, fontSize = 11.sp)
                            }
                        }
                    }
                    "وجود فراخ" -> {
                        Divider()
                        OutlinedTextField(
                            value = squabsCount,
                            onValueChange = { squabsCount = it },
                            label = { Text("عدد الزغاليل") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = squabsWeights,
                            onValueChange = { squabsWeights = it },
                            label = { Text("الأوزان بالجرام (مثال: 140,150)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "توقف وضع البيض" -> {
                        Divider()
                        Text("سبب توقف الإنتاج:", fontSize = 11.sp)
                        val reasons = listOf("مرحلة تبديل الريش (القلش)", "إصابة بمرض معوي أو تنفسي", "مشاكل صحية أخرى أو إجهاد")
                        reasons.forEach { r ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { stoppedReason = r }
                            ) {
                                RadioButton(selected = stoppedReason == r, onClick = { stoppedReason = r })
                                Text(r, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                pigeonStatus,
                                eggCount.toIntOrNull() ?: 0,
                                eggLaidDate,
                                eggCondition,
                                squabsCount.toIntOrNull() ?: 0,
                                squabsWeights,
                                stoppedReason
                            )
                        }
                    ) {
                        Text("حفظ التغييرات")
                    }
                }
            }
        }
    }
}

@Composable
fun VaccinationFormDialog(
    vac: VaccinationEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, scheduledDate: String, status: String, instructions: String) -> Unit
) {
    var title by remember { mutableStateOf(vac?.title ?: "") }
    var scheduledDate by remember { mutableStateOf(vac?.scheduledDate ?: "2026-07-08") }
    var status by remember { mutableStateOf(vac?.status ?: "جديد") }
    var instructions by remember { mutableStateOf(vac?.instructions ?: "") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (vac == null) "جدولة تطعيم / دواء جديد" else "تعديل بيانات التطعيم المجدول",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم التطعيم أو المركب الطبي (مثال: نيوكاسل)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_vaccine_title")
                )

                OutlinedTextField(
                    value = scheduledDate,
                    onValueChange = { scheduledDate = it },
                    label = { Text("تاريخ الجدولة المجدول (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("طريقة الإعطاء والتفاصيل الإرشادية:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val instOptions = listOf(
                    "إضافة في مياه الشرب النظيفة (نسبة 1 مل/لتر)",
                    "بالحقن العضلي في صدر الطائر البالغ فقط",
                    "بالرش الرذاذي المائي فوق الأقفاص",
                    "عن طريق الفم مباشرة للحمام المصاب"
                )
                instOptions.forEach { opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { instructions = opt }
                    ) {
                        RadioButton(selected = instructions == opt, onClick = { instructions = opt })
                        Text(opt, fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("تعليمات إعطاء بديلة أو تفاصيل إضافية") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("حالة التطعيم الحالية:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = status == "جديد", onClick = { status = "جديد" })
                    Text("جديد (معلق)", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = status == "تم الإعطاء", onClick = { status = "تم الإعطاء" })
                    Text("تم الإعطاء", fontSize = 12.sp)
                }

                if (showError) {
                    Text("يرجى كتابة اسم التطعيم أو المركب الطبي للحفظ.", color = ErrorRed, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.trim().isEmpty()) {
                                showError = true
                            } else {
                                onSave(title, scheduledDate, status, instructions)
                            }
                        }
                    ) {
                        Text("حفظ الجدول")
                    }
                }
            }
        }
    }
}

@Composable
fun WarehouseFormDialog(
    item: WarehouseEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, qty: Double, unit: String, consumption: Double, threshold: Double) -> Unit
) {
    var itemName by remember { mutableStateOf(item?.itemName ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "أعلاف") }
    var currentQuantity by remember { mutableStateOf(item?.currentQuantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "كجم") }
    var dailyConsumptionRate by remember { mutableStateOf(item?.dailyConsumptionRate?.toString() ?: "0.0") }
    var lowStockThreshold by remember { mutableStateOf(item?.lowStockThreshold?.toString() ?: "10.0") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (item == null) "إضافة صنف جديد للمستودع" else "تعديل بيانات الصنف والمخزون",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("اسم الصنف (مثال: علف لاحم 18%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_warehouse_name")
                )

                Text("الفئة / التصنيف:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val cats = listOf("أعلاف", "معدات", "أدوية")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    cats.forEach { cat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = category == cat, onClick = { category = cat })
                            Text(cat, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = currentQuantity,
                    onValueChange = { currentQuantity = it },
                    label = { Text("الكمية الحالية المتوفرة بالمخزن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_warehouse_qty")
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("وحدة القياس للمخزون (كجم، قطعة، لتر)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (category == "أعلاف") {
                    OutlinedTextField(
                        value = dailyConsumptionRate,
                        onValueChange = { dailyConsumptionRate = it },
                        label = { Text("معدل الاستهلاك اليومي التقريبي بالوحدة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = lowStockThreshold,
                    onValueChange = { lowStockThreshold = it },
                    label = { Text("حد الأمان لإطلاق تنبيه انخفاض المخزون") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Text("يرجى تعبئة كافة الحقول والأرقام بشكل صحيح.", color = ErrorRed, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = currentQuantity.toDoubleOrNull()
                            val threshold = lowStockThreshold.toDoubleOrNull()
                            if (itemName.trim().isEmpty() || qty == null || threshold == null) {
                                showError = true
                            } else {
                                onSave(
                                    itemName,
                                    category,
                                    qty,
                                    unit,
                                    dailyConsumptionRate.toDoubleOrNull() ?: 0.0,
                                    threshold
                                )
                            }
                        }
                    ) {
                        Text("حفظ الصنف")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskFormDialog(
    task: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, rep: Boolean, freq: String, assigned: String, status: String, time: String) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var isRepetitive by remember { mutableStateOf(task?.isRepetitive ?: true) }
    var frequency by remember { mutableStateOf(task?.frequency ?: "يومي") }
    var assignedTo by remember { mutableStateOf(task?.assignedTo ?: "عامل الميدان") }
    var status by remember { mutableStateOf(task?.status ?: "قيد الانتظار") }
    var alertTime by remember { mutableStateOf(task?.alertTime ?: "08:00") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (task == null) "إضافة وإسناد مهمة عمل جديدة" else "تعديل تفاصيل المهمة والعمل",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة (مثال: تعقيم الأقفاص)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("شرح وتوجيهات تنفيذ المهمة") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRepetitive, onCheckedChange = { isRepetitive = it })
                    Text("مهمة متكررة بشكل دوري", fontSize = 12.sp)
                }

                if (isRepetitive) {
                    Text("تكرار الجدولة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row {
                        RadioButton(selected = frequency == "يومي", onClick = { frequency = "يومي" })
                        Text("يومي", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = frequency == "أسبوعي", onClick = { frequency = "أسبوعي" })
                        Text("أسبوعي", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = alertTime,
                    onValueChange = { alertTime = it },
                    label = { Text("وقت التنبيه والإشعار اليومي (ساعة:دقيقة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("العامل المكلّف بالمهمة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Text("يرجى ملء عنوان المهمة للحفظ.", color = ErrorRed, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.trim().isEmpty()) {
                                showError = true
                            } else {
                                onSave(title, description, isRepetitive, frequency, assignedTo, status, alertTime)
                            }
                        }
                    ) {
                        Text("إسناد المهمة")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterDialog(
    alerts: List<FarmAlert>,
    onDismiss: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("notification_center_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, NaturalBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header of Notification Center
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = NaturalDeepText)
                    }
                    Text(
                        text = "مركز التنبيهات والإشعارات 🔔",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalDeepText,
                        textAlign = TextAlign.Right
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (alerts.isEmpty()) {
                    // Empty notifications state with premium visual
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(36.dp))
                                .background(SuccessGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "كل شيء تحت السيطرة!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalDeepText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "لا توجد تنبيهات نشطة لمواعيد الفحص أو التطعيمات.",
                            fontSize = 12.sp,
                            color = NaturalMutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    // List of alerts
                    Text(
                        text = "لديك (${alerts.size}) تنبيهات تحتاج للمتابعة والعمل:",
                        fontSize = 13.sp,
                        color = NaturalMutedText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Right
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(alerts) { alert ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (alert.isUrgent) Color(0xFFFDF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (alert.isUrgent) Color(0xFFF8B4B4) else NaturalBorder
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Badge of Urgency
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (alert.isUrgent) ErrorRed else AlertOrange)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (alert.isUrgent) "عاجل" else "قريباً",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Title & Icon
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = alert.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (alert.isUrgent) Color(0xFF9B1C1C) else NaturalDeepText
                                            )
                                            Icon(
                                                imageVector = when (alert.type) {
                                                    AlertType.EGG_CHECK -> Icons.Default.Search
                                                    AlertType.VACCINATION -> Icons.Default.Medication
                                                    AlertType.LOW_STOCK -> Icons.Default.Warning
                                                },
                                                contentDescription = null,
                                                tint = if (alert.isUrgent) ErrorRed else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Description of alert
                                    Text(
                                        text = alert.description,
                                        fontSize = 12.sp,
                                        color = if (alert.isUrgent) Color(0xFF771D1D) else NaturalDeepText,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Quick action buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Button(
                                            onClick = {
                                                val tabIndex = when (alert.type) {
                                                    AlertType.EGG_CHECK -> 0
                                                    AlertType.VACCINATION -> 1
                                                    AlertType.LOW_STOCK -> 2
                                                }
                                                onNavigateToTab(tabIndex)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (alert.isUrgent) ErrorRed else MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = when (alert.type) {
                                                    AlertType.EGG_CHECK -> "اذهب للأقفاص وفحص"
                                                    AlertType.VACCINATION -> "تطبيق التطعيم"
                                                    AlertType.LOW_STOCK -> "إدارة المستودع"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardsDensityChart(cages: List<CageEntity>) {
    val wardsGrouped = cages.groupBy { it.cageNumber }.filterKeys { it.isNotBlank() }
    val sortedWards = wardsGrouped.keys.sorted()

    var selectedWard by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "كثافة ومخزون العنابر (أزواج الحمام)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "توزيع أزواج الحمام والعيون المنتجة في كل عنبر",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sortedWards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد بيانات كافية لعرض المخطط البياني للجمهرة والكثافة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxPairs = sortedWards.maxOfOrNull { wardsGrouped[it]?.size ?: 0 } ?: 1
                val maxAxisValue = ((maxPairs + 4) / 5) * 5 // Round up to nearest multiple of 5 for nice axis

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Y-Axis Labels
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 24.dp, end = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 4 downTo 0) {
                            val valLabel = (maxAxisValue * i) / 4
                            Text(
                                text = "$valLabel",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Chart Bars Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Horizontal Gridlines
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 24.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(5) {
                                Divider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    thickness = 0.8.dp
                                )
                            }
                        }

                        // Bars
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            sortedWards.forEach { wardName ->
                                val wardCages = wardsGrouped[wardName] ?: emptyList()
                                val count = wardCages.size
                                val barHeightFactor = count.toFloat() / maxAxisValue.toFloat()
                                val isSelected = selectedWard == wardName

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            selectedWard = if (isSelected) null else wardName
                                        },
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    // Count text on top of the bar
                                    Text(
                                        text = "$count",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    // Bar container
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(0.7f) // Maximum 70% of available height
                                            .fillMaxWidth(0.5f)  // Bar width is 50% of column width
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                            )
                                            .fillMaxHeight(barHeightFactor)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // X-Axis Label
                                    Text(
                                        text = wardName,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        modifier = Modifier.height(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Details Card
                selectedWard?.let { wardName ->
                    val wardCages = wardsGrouped[wardName] ?: emptyList()
                    val eggCount = wardCages.count { it.pigeonStatus == "وجود بيض" }
                    val squabCount = wardCages.count { it.pigeonStatus == "وجود فراخ" }
                    val stopCount = wardCages.count { it.pigeonStatus == "توقف وضع البيض" }
                    val prepCount = wardCages.count { it.pigeonStatus == "تجهيز للتكاثر" }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 تفاصيل إنتاج $wardName:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "إجمالي أزواج: ${wardCages.size}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LabelValueMini("بيض", "$eggCount زوج", WarningYellow)
                                LabelValueMini("زغاليل", "$squabCount زوج", SuccessGreen)
                                LabelValueMini("تجهيز", "$prepCount زوج", MaterialTheme.colorScheme.primary)
                                LabelValueMini("متوقف", "$stopCount زوج", ErrorRed)
                            }
                        }
                    }
                } ?: run {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 انقر على أي عمود لعرض تفاصيل وكثافة الإنتاج الكاملة للعنبر المحدد.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun LabelValueMini(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
