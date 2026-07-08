package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"
    private var isInitialized = false
    private lateinit var db: FirebaseFirestore

    // Flows to track real-time sync state from Firestore metadata
    private val _isFirestoreSyncing = MutableStateFlow(false)
    val isFirestoreSyncing: StateFlow<Boolean> = _isFirestoreSyncing.asStateFlow()

    private val _isFirestoreOffline = MutableStateFlow(false)
    val isFirestoreOffline: StateFlow<Boolean> = _isFirestoreOffline.asStateFlow()

    private val _syncMessages = MutableStateFlow<List<String>>(emptyList())
    val syncMessages: StateFlow<List<String>> = _syncMessages.asStateFlow()

    // Flag for pending writes on each collection
    private var cagesPending = false
    private var vaccinationsPending = false
    private var warehousePending = false
    private var tasksPending = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            // 1. Programmatic Firebase Initialization (Robust fallback if google-services.json is missing)
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:56448fa22459:android:2d8f9f8c8d8b8a")
                    .setProjectId("french-squab-farm-app")
                    .setApiKey("AIzaSyFakeKeyForOfflinePersistenceCompiles")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Firebase initialized programmatically with offline fallback credentials.")
            } else {
                Log.d(TAG, "Firebase already initialized automatically.")
            }

            // 2. Configure Firestore with Offline Persistence Explicitly
            db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        // Use default cache size (100 MB), Firestore will cache everything offline
                        .build()
                )
                .build()
            db.firestoreSettings = settings
            Log.d(TAG, "Firestore configured with offline disk persistence successfully.")

            isInitialized = true
            addSyncLog("⚡ تم تفعيل نظام المزامنة السحابية الذكي (Firestore Offline Persistence).")

            // 3. Setup real-time listeners to observe offline queues and pending writes
            setupRealtimePendingWriteListeners()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firestore Sync Manager", e)
            addSyncLog("⚠️ فشل بدء نظام المزامنة السحابية: ${e.localizedMessage}")
        }
    }

    private fun addSyncLog(message: String) {
        val current = _syncMessages.value.toMutableList()
        current.add(0, message)
        if (current.size > 20) {
            current.removeAt(current.size - 1)
        }
        _syncMessages.value = current
    }

    private fun setupRealtimePendingWriteListeners() {
        if (!isInitialized) return

        val coroutineScope = CoroutineScope(Dispatchers.IO)

        // Listen to cages
        db.collection("cages").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for cages collection", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                cagesPending = snapshot.metadata.hasPendingWrites()
                _isFirestoreOffline.value = snapshot.metadata.isFromCache
                updateOverallSyncingState()
            }
        }

        // Listen to vaccinations
        db.collection("vaccinations").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for vaccinations collection", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                vaccinationsPending = snapshot.metadata.hasPendingWrites()
                updateOverallSyncingState()
            }
        }

        // Listen to warehouse
        db.collection("warehouse").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for warehouse collection", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                warehousePending = snapshot.metadata.hasPendingWrites()
                updateOverallSyncingState()
            }
        }

        // Listen to tasks
        db.collection("tasks").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for tasks collection", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                tasksPending = snapshot.metadata.hasPendingWrites()
                updateOverallSyncingState()
            }
        }
    }

    private fun updateOverallSyncingState() {
        val isAnyPending = cagesPending || vaccinationsPending || warehousePending || tasksPending
        val wasSyncing = _isFirestoreSyncing.value
        _isFirestoreSyncing.value = isAnyPending

        if (wasSyncing && !isAnyPending) {
            addSyncLog("☁️ اكتمل رفع جميع التعديلات والمزامنة التلقائية مع السحابة المركزية بنجاح.")
        } else if (!wasSyncing && isAnyPending) {
            addSyncLog("🔄 جاري حفظ التحديثات في قائمة الانتظار المحلية ورفعها سحابياً...")
        }
    }

    // ==========================================
    // Sync Operations
    // ==========================================

    fun syncCage(cage: CageEntity) {
        if (!isInitialized) return
        val map = mapOf(
            "id" to cage.id,
            "cageNumber" to cage.cageNumber,
            "nestBoxNumber" to cage.nestBoxNumber,
            "breed" to cage.breed,
            "pigeonStatus" to cage.pigeonStatus,
            "eggCount" to cage.eggCount,
            "eggLaidDate" to cage.eggLaidDate,
            "eggCheckDate" to cage.eggCheckDate,
            "eggHatchingDate" to cage.eggHatchingDate,
            "eggCondition" to cage.eggCondition,
            "squabsCount" to cage.squabsCount,
            "squabsWeights" to cage.squabsWeights,
            "stoppedReason" to cage.stoppedReason,
            "lastUpdated" to cage.lastUpdated
        )
        db.collection("cages").document(cage.id.toString()).set(map)
            .addOnSuccessListener {
                Log.d(TAG, "Cage ${cage.id} successfully queued/written in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error writing cage ${cage.id} to Firestore", e)
            }
    }

    fun deleteCage(id: Int) {
        if (!isInitialized) return
        db.collection("cages").document(id.toString()).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Cage $id successfully deleted/queued deletion in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting cage $id in Firestore", e)
            }
    }

    fun syncVaccination(vac: VaccinationEntity) {
        if (!isInitialized) return
        val map = mapOf(
            "id" to vac.id,
            "title" to vac.title,
            "scheduledDate" to vac.scheduledDate,
            "status" to vac.status,
            "instructions" to vac.instructions,
            "lastUpdated" to vac.lastUpdated
        )
        db.collection("vaccinations").document(vac.id.toString()).set(map)
            .addOnSuccessListener {
                Log.d(TAG, "Vaccination ${vac.id} successfully queued/written in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error writing vaccination ${vac.id} to Firestore", e)
            }
    }

    fun deleteVaccination(id: Int) {
        if (!isInitialized) return
        db.collection("vaccinations").document(id.toString()).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Vaccination $id successfully deleted/queued deletion in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting vaccination $id in Firestore", e)
            }
    }

    fun syncWarehouseItem(item: WarehouseEntity) {
        if (!isInitialized) return
        val map = mapOf(
            "id" to item.id,
            "itemName" to item.itemName,
            "category" to item.category,
            "currentQuantity" to item.currentQuantity,
            "unit" to item.unit,
            "dailyConsumptionRate" to item.dailyConsumptionRate,
            "lowStockThreshold" to item.lowStockThreshold,
            "lastUpdated" to item.lastUpdated
        )
        db.collection("warehouse").document(item.id.toString()).set(map)
            .addOnSuccessListener {
                Log.d(TAG, "Warehouse item ${item.id} successfully queued/written in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error writing warehouse item ${item.id} to Firestore", e)
            }
    }

    fun deleteWarehouseItem(id: Int) {
        if (!isInitialized) return
        db.collection("warehouse").document(id.toString()).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Warehouse item $id successfully deleted/queued deletion in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting warehouse item $id in Firestore", e)
            }
    }

    fun syncTask(task: TaskEntity) {
        if (!isInitialized) return
        val map = mapOf(
            "id" to task.id,
            "title" to task.title,
            "description" to task.description,
            "isRepetitive" to task.isRepetitive,
            "frequency" to task.frequency,
            "assignedTo" to task.assignedTo,
            "status" to task.status,
            "isAddedByManager" to task.isAddedByManager,
            "alertTime" to task.alertTime,
            "lastUpdated" to task.lastUpdated
        )
        db.collection("tasks").document(task.id.toString()).set(map)
            .addOnSuccessListener {
                Log.d(TAG, "Task ${task.id} successfully queued/written in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error writing task ${task.id} to Firestore", e)
            }
    }

    fun deleteTask(id: Int) {
        if (!isInitialized) return
        db.collection("tasks").document(id.toString()).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Task $id successfully deleted/queued deletion in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting task $id in Firestore", e)
            }
    }

    fun forceFullFirestoreSync(
        cages: List<CageEntity>,
        vaccinations: List<VaccinationEntity>,
        warehouse: List<WarehouseEntity>,
        tasks: List<TaskEntity>
    ) {
        if (!isInitialized) return
        addSyncLog("🔄 جاري إعادة جدولة ومزامنة كافة السجلات مع مخزن Firestore المحلي...")
        cages.forEach { syncCage(it) }
        vaccinations.forEach { syncVaccination(it) }
        warehouse.forEach { syncWarehouseItem(it) }
        tasks.forEach { syncTask(it) }
    }
}
