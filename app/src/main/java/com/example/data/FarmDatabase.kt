package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "cages")
data class CageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cageNumber: String, // رقم القفص
    val nestBoxNumber: String, // رقم العين
    val breed: String = "لاحم فرنسي", // السلالة
    val pigeonStatus: String, // حالة الحمامة: تجهيز للتكاثر، وجود بيض، وجود فراخ، توقف وضع البيض
    
    // تفاصيل إضافية لحالة "وجود بيض"
    val eggCount: Int = 0,
    val eggLaidDate: String = "", // تاريخ وضع البيض
    val eggCheckDate: String = "", // موعد فحص البيض (تاريخ افتراضي = تاريخ الوضع + 7 أيام)
    val eggHatchingDate: String = "", // موعد الفقس (تاريخ افتراضي = تاريخ الوضع + 18 يوم)
    val eggCondition: String = "طبيعي", // حالة البيض: طبيعي، غير مخصب، توقف التحضين
    
    // تفاصيل إضافية لحالة "وجود فراخ"
    val squabsCount: Int = 0,
    val squabsWeights: String = "", // أوزان الفراخ مفصولة بفاصلة (مثال: "120,130,115")
    
    // تفاصيل إضافية لحالة "توقف وضع البيض"
    val stoppedReason: String = "", // سبب التوقف: مرض، قلش، مشاكل صحية أخرى
    
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // اسم التطعيم أو الدواء
    val scheduledDate: String, // تاريخ الجدولة (YYYY-MM-DD)
    val status: String = "جديد", // جديد، تم الإعطاء
    val instructions: String = "", // طريقة الإعطاء والتفاصيل
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "warehouse")
data class WarehouseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String, // اسم الصنف (علف، معدات، أدوية)
    val category: String, // الفئة: أعلاف، معدات، أدوية
    val currentQuantity: Double, // الكمية الحالية
    val unit: String, // الوحدة: كجم، قطعة، لتر
    val dailyConsumptionRate: Double = 0.0, // معدل الاستهلاك اليومي (للأعلاف خاصة)
    val lowStockThreshold: Double = 10.0, // حد إنذار انخفاض المخزون
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // عنوان المهمة
    val description: String = "", // تفاصيل إضافية
    val isRepetitive: Boolean = true, // تكرار المهمة
    val frequency: String = "يومي", // يومي، أسبوعي
    val assignedTo: String = "عامل الميدان", // المكلف بالمهمة
    val status: String = "قيد الانتظار", // قيد الانتظار، تم التنفيذ
    val isAddedByManager: Boolean = false, // مضافة من المشرف
    val alertTime: String = "08:00", // وقت التنبيه (ساعة:دقيقة)
    val lastUpdated: Long = System.currentTimeMillis()
)

// ==========================================
// 2. DAOs (Data Access Objects)
// ==========================================

@Dao
interface CageDao {
    @Query("SELECT * FROM cages ORDER BY cageNumber ASC, nestBoxNumber ASC")
    fun getAllCages(): Flow<List<CageEntity>>

    @Query("SELECT * FROM cages WHERE id = :id LIMIT 1")
    suspend fun getCageById(id: Int): CageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCage(cage: CageEntity): Long

    @Update
    suspend fun updateCage(cage: CageEntity)

    @Delete
    suspend fun deleteCage(cage: CageEntity)

    @Query("DELETE FROM cages WHERE id = :id")
    suspend fun deleteCageById(id: Int)
}

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations ORDER BY scheduledDate ASC")
    fun getAllVaccinations(): Flow<List<VaccinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: VaccinationEntity): Long

    @Update
    suspend fun updateVaccination(vaccination: VaccinationEntity)

    @Delete
    suspend fun deleteVaccination(vaccination: VaccinationEntity)

    @Query("DELETE FROM vaccinations WHERE id = :id")
    suspend fun deleteVaccinationById(id: Int)
}

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouse ORDER BY itemName ASC")
    fun getAllWarehouseItems(): Flow<List<WarehouseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouseItem(item: WarehouseEntity): Long

    @Update
    suspend fun updateWarehouseItem(item: WarehouseEntity)

    @Delete
    suspend fun deleteWarehouseItem(item: WarehouseEntity)

    @Query("DELETE FROM warehouse WHERE id = :id")
    suspend fun deleteWarehouseItemById(id: Int)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY status DESC, alertTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

// ==========================================
// 3. Database class
// ==========================================

@Database(
    entities = [
        CageEntity::class,
        VaccinationEntity::class,
        WarehouseEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {
    
    abstract fun cageDao(): CageDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: FarmDatabase? = null

        fun getDatabase(context: Context): FarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FarmDatabase::class.java,
                    "farm_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
