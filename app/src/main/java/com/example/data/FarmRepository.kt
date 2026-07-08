package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take

class FarmRepository(private val database: FarmDatabase) {

    private val cageDao = database.cageDao()
    private val vaccinationDao = database.vaccinationDao()
    private val warehouseDao = database.warehouseDao()
    private val taskDao = database.taskDao()

    // 1. Cages
    val allCages: Flow<List<CageEntity>> = cageDao.getAllCages()
    suspend fun getCageById(id: Int) = cageDao.getCageById(id)
    suspend fun insertCage(cage: CageEntity) = cageDao.insertCage(cage)
    suspend fun updateCage(cage: CageEntity) = cageDao.updateCage(cage)
    suspend fun deleteCage(cage: CageEntity) = cageDao.deleteCage(cage)
    suspend fun deleteCageById(id: Int) = cageDao.deleteCageById(id)

    // 2. Vaccinations
    val allVaccinations: Flow<List<VaccinationEntity>> = vaccinationDao.getAllVaccinations()
    suspend fun insertVaccination(vac: VaccinationEntity) = vaccinationDao.insertVaccination(vac)
    suspend fun updateVaccination(vac: VaccinationEntity) = vaccinationDao.updateVaccination(vac)
    suspend fun deleteVaccination(vac: VaccinationEntity) = vaccinationDao.deleteVaccination(vac)
    suspend fun deleteVaccinationById(id: Int) = vaccinationDao.deleteVaccinationById(id)

    // 3. Warehouse
    val allWarehouseItems: Flow<List<WarehouseEntity>> = warehouseDao.getAllWarehouseItems()
    suspend fun insertWarehouseItem(item: WarehouseEntity) = warehouseDao.insertWarehouseItem(item)
    suspend fun updateWarehouseItem(item: WarehouseEntity) = warehouseDao.updateWarehouseItem(item)
    suspend fun deleteWarehouseItem(item: WarehouseEntity) = warehouseDao.deleteWarehouseItem(item)
    suspend fun deleteWarehouseItemById(id: Int) = warehouseDao.deleteWarehouseItemById(id)

    // 4. Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Int) = taskDao.deleteTaskById(id)

    // pre-populate with mock data if database is empty to make it lively
    suspend fun prepopulateIfEmpty() {
        // Check Cages
        val cagesList = cageDao.getAllCages().take(1).first()
        if (cagesList.isEmpty()) {
            // Seed Cages
            cageDao.insertCage(CageEntity(
                cageNumber = "عنبر 1 - أ",
                nestBoxNumber = "عين 01",
                breed = "لاحم فرنسي جامبو",
                pigeonStatus = "وجود بيض",
                eggCount = 2,
                eggLaidDate = "2026-07-02",
                eggCheckDate = "2026-07-09",
                eggHatchingDate = "2026-07-20",
                eggCondition = "طبيعي"
            ))
            cageDao.insertCage(CageEntity(
                cageNumber = "عنبر 1 - أ",
                nestBoxNumber = "عين 02",
                breed = "لاحم فرنسي سوبر",
                pigeonStatus = "وجود فراخ",
                squabsCount = 2,
                squabsWeights = "165,180"
            ))
            cageDao.insertCage(CageEntity(
                cageNumber = "عنبر 1 - ب",
                nestBoxNumber = "عين 05",
                breed = "لاحم فرنسي جامبو",
                pigeonStatus = "تجهيز للتكاثر"
            ))
            cageDao.insertCage(CageEntity(
                cageNumber = "عنبر 2 - أ",
                nestBoxNumber = "عين 12",
                breed = "لاحم فرنسي",
                pigeonStatus = "توقف وضع البيض",
                stoppedReason = "مرحلة تبديل الريش (القلش)"
            ))
            cageDao.insertCage(CageEntity(
                cageNumber = "عنبر 2 - ب",
                nestBoxNumber = "عين 03",
                breed = "لاحم فرنسي",
                pigeonStatus = "وجود بيض",
                eggCount = 2,
                eggLaidDate = "2026-07-05",
                eggCheckDate = "2026-07-12",
                eggHatchingDate = "2026-07-23",
                eggCondition = "غير مخصب"
            ))

            // Seed Vaccinations
            vaccinationDao.insertVaccination(VaccinationEntity(
                title = "لقاح نيوكاسل (Newcastle)",
                scheduledDate = "2026-07-10",
                status = "جديد",
                instructions = "يُعطى في مياه الشرب. نسبة 1 مل لكل لتر مياه نظيفة لجميع العنابر."
            ))
            vaccinationDao.insertVaccination(VaccinationEntity(
                title = "جرعة فيتامينات وفوسفور",
                scheduledDate = "2026-07-05",
                status = "تم الإعطاء",
                instructions = "إضافة مكملات لزيادة الخصوبة وتحفيز إنتاج البيض في عنابر التجهيز."
            ))
            vaccinationDao.insertVaccination(VaccinationEntity(
                title = "تحصين الجدري المائي",
                scheduledDate = "2026-07-18",
                status = "جديد",
                instructions = "بالوخز في جناح الطيور البالغة والفراخ الكبيرة."
            ))

            // Seed Warehouse
            warehouseDao.insertWarehouseItem(WarehouseEntity(
                itemName = "علف تسمين لاحم 18%",
                category = "أعلاف",
                currentQuantity = 350.0,
                unit = "كجم",
                dailyConsumptionRate = 18.5,
                lowStockThreshold = 50.0
            ))
            warehouseDao.insertWarehouseItem(WarehouseEntity(
                itemName = "علف بياض منتج 16%",
                category = "أعلاف",
                currentQuantity = 45.0, // Low stock!
                unit = "كجم",
                dailyConsumptionRate = 12.0,
                lowStockThreshold = 50.0
            ))
            warehouseDao.insertWarehouseItem(WarehouseEntity(
                itemName = "أعشاش فخارية للتحضين",
                category = "معدات",
                currentQuantity = 25.0,
                unit = "قطعة",
                lowStockThreshold = 5.0
            ))
            warehouseDao.insertWarehouseItem(WarehouseEntity(
                itemName = "مساقي ومكالف بلاستيكية",
                category = "معدات",
                currentQuantity = 15.0,
                unit = "قطعة",
                lowStockThreshold = 3.0
            ))
            warehouseDao.insertWarehouseItem(WarehouseEntity(
                itemName = "فيتامين هـ + سيلينيوم مستورد",
                category = "أدوية",
                currentQuantity = 4.5,
                unit = "لتر",
                lowStockThreshold = 1.0
            ))

            // Seed Tasks
            taskDao.insertTask(TaskEntity(
                title = "توزيع الأعلاف والمياه الصباحية",
                description = "ملء المعالف والمساقي والتأكد من نظافة مياه الشرب في جميع الأقفاص.",
                isRepetitive = true,
                frequency = "يومي",
                assignedTo = "عامل الميدان",
                status = "قيد الانتظار",
                alertTime = "07:30"
            ))
            taskDao.insertTask(TaskEntity(
                title = "فحص أعشاش عنبر 1 وتدوين البيض الجديد",
                description = "متابعة الأقفاص وتحديث حالة البيض وتحديد غير المخصب منها لتنظيف العش.",
                isRepetitive = true,
                frequency = "يومي",
                assignedTo = "عامل الميدان",
                status = "قيد الانتظار",
                alertTime = "09:30"
            ))
            taskDao.insertTask(TaskEntity(
                title = "غسيل وتطهير عنبر 2 بالكامل",
                description = "استخدام المطهر الموصى به لغسيل أقفاص وأرضية العنبر والوقاية من الأمراض.",
                isRepetitive = true,
                frequency = "أسبوعي",
                assignedTo = "عامل الميدان",
                status = "تم التنفيذ",
                alertTime = "11:00"
            ))
            taskDao.insertTask(TaskEntity(
                title = "تطبيق لقاح النيوكاسل المجدول",
                description = "مهمة طارئة مضافة من المشرف: إعداد وتطبيق لقاح النيوكاسل في مياه الشرب صباحاً.",
                isRepetitive = false,
                assignedTo = "عامل الميدان",
                status = "قيد الانتظار",
                isAddedByManager = true,
                alertTime = "08:00"
            ))
        }
    }
}
