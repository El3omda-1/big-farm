package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.CageEntity
import com.example.data.FarmDatabase
import com.example.data.FarmRepository
import com.example.data.TaskEntity
import com.example.data.VaccinationEntity
import com.example.data.WarehouseEntity
import com.example.data.FirestoreSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FarmRepository
    
    // States for data
    val allCages: StateFlow<List<CageEntity>>
    val allVaccinations: StateFlow<List<VaccinationEntity>>
    val allWarehouseItems: StateFlow<List<WarehouseEntity>>
    val allTasks: StateFlow<List<TaskEntity>>

    // User Roles: "MANAGER" or "WORKER"
    private val _currentRole = MutableStateFlow("WORKER") // Default to worker/unauthorized until logged in
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Authentication State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInUserDisplayName = MutableStateFlow("")
    val loggedInUserDisplayName: StateFlow<String> = _loggedInUserDisplayName.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Cloud Synchronization Simulation State
    private val _isCloudSynced = MutableStateFlow(true)
    val isCloudSynced: StateFlow<Boolean> = _isCloudSynced.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<String>>(emptyList())
    val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()

    // Selected Tab State
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Gemini Smart Analytics Report
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisReport = MutableStateFlow<String>("")
    val analysisReport: StateFlow<String> = _analysisReport.asStateFlow()

    // Active Notifications / Alerts System Flow
    val activeAlerts: StateFlow<List<FarmAlert>>

    init {
        val database = FarmDatabase.getDatabase(application)
        repository = FarmRepository(database)

        // Bind DB Flow states
        allCages = repository.allCages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allVaccinations = repository.allVaccinations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allWarehouseItems = repository.allWarehouseItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Combine flows dynamically to detect alerts for Egg Check, Vaccinations, and Low Stock
        activeAlerts = combine(allCages, allVaccinations, allWarehouseItems) { cages, vaccinations, warehouseItems ->
            val list = mutableListOf<FarmAlert>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())
            
            // 1. Egg checking alerts
            cages.forEach { cage ->
                if (cage.pigeonStatus == "وجود بيض" && cage.eggCheckDate.isNotEmpty()) {
                    val diff = getDaysBetween(today, cage.eggCheckDate)
                    if (diff <= 2) {
                        val isUrgent = diff <= 0
                        val description = if (diff < 0) {
                            "القفص ${cage.cageNumber} - ${cage.nestBoxNumber}: فحص البيض متأخر بـ ${-diff} يوم! (كان مجدولاً في ${cage.eggCheckDate})"
                        } else if (diff == 0L) {
                            "القفص ${cage.cageNumber} - ${cage.nestBoxNumber}: موعد فحص البيض مستحق اليوم!"
                        } else {
                            "القفص ${cage.cageNumber} - ${cage.nestBoxNumber}: فحص البيض قريب خلال $diff يوم (في ${cage.eggCheckDate})"
                        }
                        list.add(
                            FarmAlert(
                                id = "egg_${cage.id}",
                                title = "🔍 اقتراب موعد فحص البيض",
                                description = description,
                                type = AlertType.EGG_CHECK,
                                isUrgent = isUrgent,
                                dateStr = cage.eggCheckDate,
                                referenceId = cage.id
                            )
                        )
                    }
                }
            }
            
            // 2. Scheduled vaccination alerts
            vaccinations.forEach { vac ->
                if (vac.status == "جديد" && vac.scheduledDate.isNotEmpty()) {
                    val diff = getDaysBetween(today, vac.scheduledDate)
                    if (diff <= 2) {
                        val isUrgent = diff <= 0
                        val description = if (diff < 0) {
                            "تطعيم \"${vac.title}\" متأخر عن موعده بـ ${-diff} يوم! (كان في ${vac.scheduledDate})"
                        } else if (diff == 0L) {
                            "تطعيم \"${vac.title}\" مستحق للتطبيق اليوم!"
                        } else {
                            "تطعيم \"${vac.title}\" مستحق قريباً بعد $diff يوم (في ${vac.scheduledDate})"
                        }
                        list.add(
                            FarmAlert(
                                id = "vac_${vac.id}",
                                title = "💉 موعد تطعيم مجدول",
                                description = description,
                                type = AlertType.VACCINATION,
                                isUrgent = isUrgent,
                                dateStr = vac.scheduledDate,
                                referenceId = vac.id
                            )
                        )
                    }
                }
            }

            // 3. Low stock alerts
            warehouseItems.forEach { item ->
                if (item.currentQuantity <= item.lowStockThreshold) {
                    val description = "صنف \"${item.itemName}\" منخفض بالمخزون: الكمية الحالية ${item.currentQuantity} ${item.unit} (الحد الحرج ${item.lowStockThreshold} ${item.unit})"
                    list.add(
                        FarmAlert(
                            id = "stock_${item.id}",
                            title = "⚠️ مخزون منخفض بالمستودع",
                            description = description,
                            type = AlertType.LOW_STOCK,
                            isUrgent = item.currentQuantity <= (item.lowStockThreshold / 2),
                            dateStr = "",
                            referenceId = item.id
                        )
                    )
                }
            }
            
            list.sortedWith(compareBy<FarmAlert> { !it.isUrgent }.thenBy { it.dateStr })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize Firestore Sync Manager
        FirestoreSyncManager.init(application)

        // Prepopulate DB and set initial sync details
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            _lastSyncTime.value = sdf.format(Date())
            _syncLogs.value = listOf(
                "🏁 تم تشغيل النظام بنجاح وتأسيس قاعدة البيانات المحلية.",
                "☁️ تم الاتصال بالسحابة الآمنة ومزامنة البيانات الأولية تلقائياً."
            )
        }

        // Connect Firestore Sync Manager states to Viewmodel states
        viewModelScope.launch {
            FirestoreSyncManager.isFirestoreSyncing.collect { syncing ->
                _isSyncing.value = syncing
                _isCloudSynced.value = !syncing
                if (!syncing) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    _lastSyncTime.value = sdf.format(Date())
                }
            }
        }

        viewModelScope.launch {
            FirestoreSyncManager.syncMessages.collect { messages ->
                val currentLogs = _syncLogs.value.toMutableList()
                messages.reversed().forEach { msg ->
                    val formattedMsg = if (msg.startsWith("[")) msg else {
                        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        "[${sdf.format(Date())}] $msg"
                    }
                    if (!currentLogs.any { it.contains(msg) }) {
                        currentLogs.add(0, formattedMsg)
                    }
                }
                if (currentLogs.size > 30) {
                    _syncLogs.value = currentLogs.take(30)
                } else {
                    _syncLogs.value = currentLogs
                }
            }
        }
    }

    // Role switcher
    fun setRole(role: String) {
        _currentRole.value = role
        addSyncLog("🔑 تم تغيير صلاحية الحساب الحالي إلى: ${if (role == "MANAGER") "المدير (صلاحيات كاملة)" else "عامل الميدان (صلاحيات محددة)"}")
    }

    fun login(usernameInput: String, passwordInput: String): Boolean {
        _loginError.value = null
        val u = usernameInput.trim().lowercase()
        val p = passwordInput.trim()
        
        if (u.isEmpty() || p.isEmpty()) {
            _loginError.value = "يرجى إدخال اسم المستخدم وكلمة المرور"
            return false
        }
        
        if (u == "admin" && p == "admin123") {
            _currentRole.value = "MANAGER"
            _loggedInUserDisplayName.value = "المشرف العام (المدير)"
            _isLoggedIn.value = true
            addSyncLog("🔑 تم تسجيل دخول المشرف العام بنجاح.")
            return true
        } else if (u == "worker" && p == "worker123") {
            _currentRole.value = "WORKER"
            _loggedInUserDisplayName.value = "عامل الميدان"
            _isLoggedIn.value = true
            addSyncLog("🔑 تم تسجيل دخول عامل الميدان بنجاح.")
            return true
        } else {
            _loginError.value = "اسم المستخدم أو كلمة المرور غير صحيحة"
            return false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentRole.value = "WORKER" // Reset to low-privileged role
        _loggedInUserDisplayName.value = ""
        _loginError.value = null
        addSyncLog("🚪 تم تسجيل الخروج من الحساب الحالي.")
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // ==========================================
    // Cages Actions (Tab 1)
    // ==========================================
    fun saveCage(
        id: Int = 0,
        cageNumber: String,
        nestBoxNumber: String,
        breed: String = "لاحم فرنسي",
        pigeonStatus: String,
        eggCount: Int = 0,
        eggLaidDate: String = "",
        eggCondition: String = "طبيعي",
        squabsCount: Int = 0,
        squabsWeights: String = "",
        stoppedReason: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Calculate automatic dates for egg state
            var calculatedCheckDate = ""
            var calculatedHatchDate = ""
            if (pigeonStatus == "وجود بيض" && eggLaidDate.isNotEmpty()) {
                calculatedCheckDate = addDaysToDateString(eggLaidDate, 7)
                calculatedHatchDate = addDaysToDateString(eggLaidDate, 18)
            }

            val cage = CageEntity(
                id = id,
                cageNumber = cageNumber,
                nestBoxNumber = nestBoxNumber,
                breed = breed,
                pigeonStatus = pigeonStatus,
                eggCount = if (pigeonStatus == "وجود بيض") eggCount else 0,
                eggLaidDate = if (pigeonStatus == "وجود بيض") eggLaidDate else "",
                eggCheckDate = calculatedCheckDate,
                eggHatchingDate = calculatedHatchDate,
                eggCondition = if (pigeonStatus == "وجود بيض") eggCondition else "طبيعي",
                squabsCount = if (pigeonStatus == "وجود فراخ") squabsCount else 0,
                squabsWeights = if (pigeonStatus == "وجود فراخ") squabsWeights else "",
                stoppedReason = if (pigeonStatus == "توقف وضع البيض") stoppedReason else "",
                lastUpdated = System.currentTimeMillis()
            )

            if (id == 0) {
                val newId = repository.insertCage(cage)
                addSyncLog("🐣 إضافة زوج حمام جديد في قفص $cageNumber - عين $nestBoxNumber بنجاح.")
                FirestoreSyncManager.syncCage(cage.copy(id = newId.toInt()))
            } else {
                repository.updateCage(cage)
                addSyncLog("📝 تحديث بيانات قفص $cageNumber - عين $nestBoxNumber.")
                FirestoreSyncManager.syncCage(cage)
            }
        }
    }

    fun deleteCage(cage: CageEntity) {
        // Double check permission (Only Manager can delete)
        if (_currentRole.value != "MANAGER") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCage(cage)
            addSyncLog("🗑️ حذف قفص ${cage.cageNumber} - عين ${cage.nestBoxNumber} من قاعدة البيانات.")
            FirestoreSyncManager.deleteCage(cage.id)
        }
    }

    // ==========================================
    // Vaccinations Actions (Tab 2)
    // ==========================================
    fun saveVaccination(
        id: Int = 0,
        title: String,
        scheduledDate: String,
        status: String = "جديد",
        instructions: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val vac = VaccinationEntity(
                id = id,
                title = title,
                scheduledDate = scheduledDate,
                status = status,
                instructions = instructions,
                lastUpdated = System.currentTimeMillis()
            )
            if (id == 0) {
                val newId = repository.insertVaccination(vac)
                addSyncLog("💉 جدولة تطعيم/دواء جديد: $title بتاريخ $scheduledDate.")
                FirestoreSyncManager.syncVaccination(vac.copy(id = newId.toInt()))
            } else {
                repository.updateVaccination(vac)
                addSyncLog("✏️ تعديل جدول تطعيم/دواء: $title.")
                FirestoreSyncManager.syncVaccination(vac)
            }
        }
    }

    fun updateVaccinationStatus(vac: VaccinationEntity, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = vac.copy(status = newStatus, lastUpdated = System.currentTimeMillis())
            repository.updateVaccination(updated)
            addSyncLog("🩹 تغيير حالة تطعيم [${vac.title}] إلى: $newStatus")
            FirestoreSyncManager.syncVaccination(updated)
        }
    }

    fun deleteVaccination(vac: VaccinationEntity) {
        if (_currentRole.value != "MANAGER") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVaccination(vac)
            addSyncLog("🗑️ حذف تطعيم/دواء: ${vac.title}.")
            FirestoreSyncManager.deleteVaccination(vac.id)
        }
    }

    // ==========================================
    // Warehouse Actions (Tab 3)
    // ==========================================
    fun saveWarehouseItem(
        id: Int = 0,
        itemName: String,
        category: String,
        currentQuantity: Double,
        unit: String,
        dailyConsumptionRate: Double = 0.0,
        lowStockThreshold: Double = 10.0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = WarehouseEntity(
                id = id,
                itemName = itemName,
                category = category,
                currentQuantity = currentQuantity,
                unit = unit,
                dailyConsumptionRate = if (category == "أعلاف") dailyConsumptionRate else 0.0,
                lowStockThreshold = lowStockThreshold,
                lastUpdated = System.currentTimeMillis()
            )
            if (id == 0) {
                val newId = repository.insertWarehouseItem(item)
                addSyncLog("📦 تسجيل صنف جديد في المستودع: $itemName بنجاح.")
                FirestoreSyncManager.syncWarehouseItem(item.copy(id = newId.toInt()))
            } else {
                repository.updateWarehouseItem(item)
                addSyncLog("📦 تحديث مخزون صنف: $itemName.")
                FirestoreSyncManager.syncWarehouseItem(item)
            }
        }
    }

    fun deleteWarehouseItem(item: WarehouseEntity) {
        if (_currentRole.value != "MANAGER") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWarehouseItem(item)
            addSyncLog("🗑️ حذف صنف من المستودع: ${item.itemName}.")
            FirestoreSyncManager.deleteWarehouseItem(item.id)
        }
    }

    // ==========================================
    // Tasks Actions (Tab 4)
    // ==========================================
    fun saveTask(
        id: Int = 0,
        title: String,
        description: String = "",
        isRepetitive: Boolean = true,
        frequency: String = "يومي",
        assignedTo: String = "عامل الميدان",
        status: String = "قيد الانتظار",
        alertTime: String = "08:00"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskEntity(
                id = id,
                title = title,
                description = description,
                isRepetitive = isRepetitive,
                frequency = if (isRepetitive) frequency else "",
                assignedTo = assignedTo,
                status = status,
                isAddedByManager = (_currentRole.value == "MANAGER"),
                alertTime = alertTime,
                lastUpdated = System.currentTimeMillis()
            )
            if (id == 0) {
                val newId = repository.insertTask(task)
                addSyncLog("📋 إسناد مهمة جديدة للعمل اليومي: $title.")
                FirestoreSyncManager.syncTask(task.copy(id = newId.toInt()))
            } else {
                repository.updateTask(task)
                addSyncLog("📋 تعديل مهمة عمل: $title.")
                FirestoreSyncManager.syncTask(task)
            }
        }
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(status = newStatus, lastUpdated = System.currentTimeMillis())
            repository.updateTask(updated)
            addSyncLog("✅ تحديث حالة مهمة [${task.title}] إلى: $newStatus")
            FirestoreSyncManager.syncTask(updated)
        }
    }

    fun deleteTask(task: TaskEntity) {
        if (_currentRole.value != "MANAGER") return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
            addSyncLog("🗑️ حذف مهمة العمل: ${task.title}.")
            FirestoreSyncManager.deleteTask(task.id)
        }
    }

    // ==========================================
    // Cloud Sync Simulator & Logs
    // ==========================================
    private fun triggerCloudSync() {
        viewModelScope.launch {
            _isCloudSynced.value = false
            _isSyncing.value = true
            delay(1500) // Simulate cloud upload lag
            _isSyncing.value = false
            _isCloudSynced.value = true
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            _lastSyncTime.value = sdf.format(Date())
            addSyncLog("☁️ تمت مزامنة جميع التحديثات الأخيرة مع قاعدة البيانات السحابية المركزية بنجاح.")
        }
    }

    fun forceManualSync() {
        viewModelScope.launch {
            FirestoreSyncManager.forceFullFirestoreSync(
                allCages.value,
                allVaccinations.value,
                allWarehouseItems.value,
                allTasks.value
            )
        }
    }

    private fun addSyncLog(message: String) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val formattedMsg = "[$timestamp] $message"
        val currentLogs = _syncLogs.value.toMutableList()
        currentLogs.add(0, formattedMsg) // Newest logs first
        // Limit log size to 30 elements
        if (currentLogs.size > 30) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        _syncLogs.value = currentLogs
    }

    // Helper to calculate dates
    private fun addDaysToDateString(dateStr: String, days: Int): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return ""
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            sdf.format(cal.time)
        } catch (e: Exception) {
            ""
        }
    }

    // ==========================================
    // Gemini Farm Advisor & Report Generator (Tab 5)
    // ==========================================
    fun generateGeminiReport() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisReport.value = ""
            addSyncLog("🧠 جاري تشغيل مستشار الذكاء الاصطناعي Gemini لتحليل سجلات المزرعة...")

            try {
                // Prepare a text representation of the database for Gemini
                val cages = allCages.value
                val warehouse = allWarehouseItems.value
                val vaccines = allVaccinations.value
                val tasks = allTasks.value

                val cagesSummary = cages.groupBy { it.pigeonStatus }.map { (status, list) ->
                    "- $status: ${list.size} أقفاص (إجمالي البيض: ${list.sumOf { it.eggCount }}, إجمالي الفراخ: ${list.sumOf { it.squabsCount }})"
                }.joinToString("\n")

                val lowStockItems = warehouse.filter { it.currentQuantity <= it.lowStockThreshold }
                val warehouseSummary = warehouse.joinToString("\n") { 
                    "- ${it.itemName}: ${it.currentQuantity} ${it.unit} (معدل استهلاك: ${it.dailyConsumptionRate} ${it.unit}/يوم، حد إنذار: ${it.lowStockThreshold} ${it.unit})"
                }
                
                val pendingVaccines = vaccines.filter { it.status == "جديد" }.joinToString("\n") { "- ${it.title} في تاريخ ${it.scheduledDate}" }

                val prompt = """
                    أنت خبير ومستشار برتبة بروفيسور في إدارة مزارع إنتاج الحمام اللاحم الفرنسي (French Utility Pigeon). 
                    قم بتحليل حالة المزرعة التالية وقدم تقريراً فنياً دقيقاً، تنبيهات عاجلة، وتوجيهات عملية باللغة العربية الفصحى وبشكل منسق ورائع ومقنع جداً.
                    
                    إليك بيانات المزرعة الحالية:
                    
                    1. توزيع أقفاص وأزواج الحمام وحالتها الإنتاجية:
                    $cagesSummary
                    (إجمالي الأقفاص الكلي: ${cages.size})
                    
                    تفاصيل الأقفاص المعطلة أو المتوقفة عن وضع البيض:
                    ${cages.filter { it.pigeonStatus == "توقف وضع البيض" }.joinToString("\n") { "  * قفص ${it.cageNumber} عين ${it.nestBoxNumber}: سبب التوقف [${it.stoppedReason}]" }}
                    
                    تفاصيل البيض غير المخصب أو تالف:
                    ${cages.filter { it.eggCondition != "طبيعي" }.joinToString("\n") { "  * قفص ${it.cageNumber} عين ${it.nestBoxNumber}: حالة البيض [${it.eggCondition}]" }}

                    2. حالة مستودع الأعلاف والمعدات والأدوية:
                    $warehouseSummary
                    (انتبه بشكل خاص للأصناف التي انخفضت عن حد الأمان!)

                    3. جدول التطعيمات والتحصينات المخطط لها:
                    $pendingVaccines

                    4. حالة إنجاز المهام اليومية للعمال:
                    - إجمالي المهام: ${tasks.size}
                    - المهام المنجزة: ${tasks.count { it.status == "تم التنفيذ" }}
                    - المهام المعلقة: ${tasks.count { it.status == "قيد الانتظار" }}

                    المطلوب منك في التقرير:
                    - مقدمة محفزة ومختصرة لتقييم صحة المزرعة ومعدل الإنتاج الحالي.
                    - تحليل دقيق لحالة الأعلاف: احسب كم يوماً سيكفي مخزون كل نوع من الأعلاف بناءً على معدل الاستهلاك اليومي المذكور، ونبه فوراً على الأعلاف التي شارفت على النفاد.
                    - توصيات بيطرية لحالات التوقف عن وضع البيض (القلش أو المرض) المسجلة بالتفصيل.
                    - نصيحة فنية لتفادي البيض غير المخصب أو مشاكل توقف التحضين بناءً على الحالات المسجلة.
                    - نصائح لتحسين أوزان الفراخ (الزغاليل) والوصول بها للوزن القياسي للحمام اللاحم الفرنسي (حوالي 600-800 جرام عند عمر 28 يوم).
                    
                    اجعل التقرير منسقاً باستخدام نقاط واضحة وعناوين بارزة وبلغة تدل على الخبرة والاحترافية والاهتمام بالتفاصيل.
                """.trimIndent()

                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    // Fallback locally generated report if API key is empty/placeholder
                    _analysisReport.value = generateLocalFallbackReport(cages, warehouse, vaccines, tasks)
                    addSyncLog("💡 تم توليد التقرير محلياً بنجاح (المستشار في وضع غير متصل).")
                } else {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()

                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                    
                    val partsJson = JSONObject().put("text", prompt)
                    val contentJson = JSONObject().put("parts", JSONArray().put(partsJson))
                    val bodyJson = JSONObject().put("contents", JSONArray().put(contentJson))

                    val requestBody = bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val response = client.newCall(request).execute()
                            val responseBody = response.body?.string()
                            if (response.isSuccessful && responseBody != null) {
                                val jsonResponse = JSONObject(responseBody)
                                val candidates = jsonResponse.getJSONArray("candidates")
                                val textResult = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text")
                                
                                _analysisReport.value = textResult
                                addSyncLog("💡 نجح Gemini في تحليل بيانات المزرعة وإصدار تقريره الذكي.")
                            } else {
                                val errorMsg = "فشل في استلام رد من Gemini. الكود: ${response.code}"
                                _analysisReport.value = generateLocalFallbackReport(cages, warehouse, vaccines, tasks) + "\n\n*(تنبيه: تم استخدام محرك التقارير المحلي لعدم تمكن الاتصال بـ Gemini: $errorMsg)*"
                                addSyncLog("⚠️ تم استخدام التقارير المحلية لتعذر الاستجابة السحابية.")
                            }
                        } catch (e: Exception) {
                            _analysisReport.value = generateLocalFallbackReport(cages, warehouse, vaccines, tasks) + "\n\n*(تنبيه: تم استخدام محرك التقارير المحلي بسبب استثناء في الاتصال: ${e.localizedMessage})*"
                            addSyncLog("⚠️ حدث خطأ أثناء الاتصال بـ Gemini، تم التبديل للتقارير المحلية.")
                        } finally {
                            _isAnalyzing.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _analysisReport.value = "حدث خطأ غير متوقع أثناء إعداد البيانات للتقرير: ${e.localizedMessage}"
                _isAnalyzing.value = false
            }
        }
    }

    private fun generateLocalFallbackReport(
        cages: List<CageEntity>,
        warehouse: List<WarehouseEntity>,
        vaccines: List<VaccinationEntity>,
        tasks: List<TaskEntity>
    ): String {
        val totalCages = cages.size
        val hasEggs = cages.count { it.pigeonStatus == "وجود بيض" }
        val hasSquabs = cages.count { it.pigeonStatus == "وجود فراخ" }
        val breedingStopped = cages.count { it.pigeonStatus == "توقف وضع البيض" }
        val preparing = cages.count { it.pigeonStatus == "تجهيز للتكاثر" }

        val totalEggs = cages.sumOf { it.eggCount }
        val totalSquabs = cages.sumOf { it.squabsCount }

        val lowWarehouse = warehouse.filter { it.currentQuantity <= it.lowStockThreshold }

        val warehouseAlerts = if (lowWarehouse.isNotEmpty()) {
            lowWarehouse.joinToString("\n") { "⚠️ انخفاض حاد في مخزون [${it.itemName}]: المتبقي ${it.currentQuantity} ${it.unit} (حد الأمان ${it.lowStockThreshold} ${it.unit})" }
        } else {
            "✅ مخزون الأعلاف والمعدات والأدوية في النطاق الآمن تماماً."
        }

        // Feed run-out calculations
        val feedDaysInfo = warehouse.filter { it.category == "أعلاف" && it.dailyConsumptionRate > 0 }.joinToString("\n") {
            val days = (it.currentQuantity / it.dailyConsumptionRate).toInt()
            "🌾 صنف [${it.itemName}]: المتبقي يكفي لمدة حوالي $days أيام بناءً على معدل استهلاك يومي ${it.dailyConsumptionRate} كجم."
        }

        val unfertileEggs = cages.count { it.eggCondition == "غير مخصب" }
        val stoppedIncubating = cages.count { it.eggCondition == "توقفت عملية التحضين" }

        return """
            📊 **تقرير الذكاء الاصطناعي لتحليل أداء المزرعة (الوضع الاحتياطي)**
            
            مرحباً بك في لوحة الاستشارة الذكية لمزارع الحمام اللاحم الفرنسي. إليك تقرير كفاءة الإنتاج الحالي للمزرعة:
            
            📝 **1. الكفاءة الإنتاجية الإجمالية:**
            - **إجمالي الأقفاص النشطة:** $totalCages أقفاص.
            - **أزواج في مرحلة التجهيز:** $preparing أزواج (معدل خمول طبيعي).
            - **أزواج في مرحلة التحضين (وجود بيض):** $hasEggs أزواج (إجمالي البيض المحتضن: $totalEggs بيضة).
            - **أزواج لديها فراخ زغاليل:** $hasSquabs أزواج (إجمالي الفراخ النامية: $totalSquabs فرخ).
            - **أزواج معطلة (متوقفة عن الإنتاج):** $breedingStopped أزواج.
            
            🥚 **2. تحليل كفاءة البيض والفقس:**
            - سجلت المزرعة وجود **$unfertileEggs** بيض غير مخصب و **$stoppedIncubating** حالة توقف للتحضين.
            - *توصية فنية:* يُنصح بزيادة الفيتامينات الغنية بـ (هـ + سيلينيوم) للذكور في مرحلة التجهيز لتحسين الخصوبة، والتأكد من هدوء العنبر لمنع الإناث من هجر الأعشاش.
            
            🌾 **3. كفاية وجودة مخزون الأعلاف:**
            $feedDaysInfo
            
            🚨 **تنبيهات المخزون العاجلة:**
            $warehouseAlerts
            
            🦠 **4. الصحة والوقاية:**
            - هناك **${vaccines.count { it.status == "جديد" }}** تطعيمات معلقة مجدولة قريباً. يرجى المتابعة والالتزام بتطبيق التحصينات المجدولة لتفادي الأوبئة وخاصة النيوكاسل (شبه الطاعون).
            
            🛠️ **5. كفاءة العمالة والمهام:**
            - معدل إنجاز العمال للمهام الحالية هو **${if(tasks.isNotEmpty()) (tasks.count { it.status == "تم التنفيذ" } * 100 / tasks.size) else 0}%** (تم تنفيذ ${tasks.count { it.status == "تم التنفيذ" }} من أصل ${tasks.size} مهام). يرجى تحفيز العمال على استكمال المهام اليومية الصباحية في مواعيدها المحددة.
            
            ---
            *نصيحة ذهبية:* الوزن القياسي للحمام اللاحم الفرنسي المخصص للذبح هو 600 جرام فما فوق عند عمر 4 أسابيع، ركز على استخدام أعلاف بياض غنية بالبروتين 18% للأهالي أثناء إطعام الزغاليل لضمان أعلى معدل نمو.
        """.trimIndent()
    }

    private fun getDaysBetween(dateStr1: String, dateStr2: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date1 = sdf.parse(dateStr1) ?: return 999
            val date2 = sdf.parse(dateStr2) ?: return 999
            val diff = date2.time - date1.time
            diff / (24 * 60 * 60 * 1000)
        } catch (e: Exception) {
            999
        }
    }
}

enum class AlertType {
    EGG_CHECK,
    VACCINATION,
    LOW_STOCK
}

data class FarmAlert(
    val id: String,
    val title: String,
    val description: String,
    val type: AlertType,
    val isUrgent: Boolean,
    val dateStr: String,
    val referenceId: Int
)
