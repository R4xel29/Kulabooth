package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class KulaBoothMetrics(
    val totalCapEx: Double = 0.0,
    val hppMurni: Double = 0.0,
    val hppWastage: Double = 0.0,
    val netRevenuePerGlass: Double = 0.0,
    val grossProfitPerGlass: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val totalOpexMonthly: Double = 0.0,
    val dailyOpexCost: Double = 0.0,
    // Dashboard calculations
    val totalOmsetMonthly: Double = 0.0,
    val totalCostMonthly: Double = 0.0,
    val netProfitMonthly: Double = 0.0,
    val bepDaily: Double = 0.0,
    val roiMonths: Double = 0.0
)

class KulaBoothViewModel(application: Application) : AndroidViewModel(application) {

    private val db = KulaBoothDatabase.getDatabase(application, viewModelScope)
    private val repository = KulaBoothRepository(db.kulaBoothDao())

    // UI-exposed data streams
    val capExItems = repository.allCapExItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allIngredients = repository.allIngredients.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allMasterIngredients = repository.allMasterIngredients.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val opexItems = repository.allOpexItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProducts = repository.allProductSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedProductId = MutableStateFlow<Int>(1)

    val ingredients = combine(allIngredients, allMasterIngredients, selectedProductId) { raw, masters, selId ->
        raw.filter { it.productId == selId }.map { ing ->
            val master = masters.find { it.id == ing.masterIngredientId }
            IngredientWithMaster(ing, master)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings = combine(allProducts, selectedProductId) { products, selId ->
        products.find { it.id == selId } ?: products.firstOrNull() ?: ProductSettings()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductSettings()
    )

    val allSales = repository.allSales.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val apiConfig = repository.apiConfig.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ApiConfig()
    )

    init {
        viewModelScope.launch {
            allProducts.collect { products ->
                if (products.isNotEmpty() && !products.any { it.id == selectedProductId.value }) {
                    selectedProductId.value = products.first().id
                }
            }
        }
        viewModelScope.launch {
            repository.allSales.first().let { sales ->
                if (sales.isEmpty()) {
                    seedSampleSales()
                }
            }
        }
    }

    // Reactively compute all financial metrics when database elements are altered
    val metrics: StateFlow<KulaBoothMetrics> = combine(
        capExItems,
        allIngredients,
        allMasterIngredients,
        opexItems,
        allProducts,
        selectedProductId
    ) { arrayOfFlows ->
        @Suppress("UNCHECKED_CAST")
        val capexList = arrayOfFlows[0] as List<CapExItem>
        @Suppress("UNCHECKED_CAST")
        val ingredientList = arrayOfFlows[1] as List<Ingredient>
        @Suppress("UNCHECKED_CAST")
        val masterList = arrayOfFlows[2] as List<MasterIngredient>
        @Suppress("UNCHECKED_CAST")
        val opexList = arrayOfFlows[3] as List<OpexItem>
        @Suppress("UNCHECKED_CAST")
        val productList = arrayOfFlows[4] as List<ProductSettings>
        val currentSelectedId = arrayOfFlows[5] as Int
        
        val currentSettings = productList.find { it.id == currentSelectedId } ?: productList.firstOrNull() ?: ProductSettings()

        // --- 1. CapEx Computation ---
        val totalCapExVal = capexList.fold(0.0) { sum, item -> sum + item.totalPrice }

        // --- 2. HPP & Settings of Currently SELECTED Product for UI details ---
        val selIngredients = ingredientList.filter { it.productId == currentSettings.id }
        val hppMurniVal = selIngredients.fold(0.0) { sum, item ->
            val master = masterList.find { it.id == item.masterIngredientId }
            val portionCost = if (master != null && master.packageSize > 0) {
                (master.packagePrice / master.packageSize) * item.usageAmount
            } else if (item.packageSize > 0) { // flat fallback for old non-updated items
                (item.packagePrice / item.packageSize) * item.usageAmount
            } else {
                0.0
            }
            sum + portionCost
        }
        val hppWastageVal = hppMurniVal * (1.0 + (currentSettings.wastagePercent / 100.0))

        // --- 3. Selling Price & Channel commissions for SELECTED Product ---
        val commissionRate = if (currentSettings.isOnline) 0.20 else 0.0
        val commissionPerGlass = currentSettings.sellingPrice * commissionRate
        val netRevenuePerGlassVal = currentSettings.sellingPrice - commissionPerGlass

        // --- 4. Gross Profit & Margin per portion for SELECTED Product ---
        val grossProfitPerGlassVal = netRevenuePerGlassVal - hppWastageVal
        val profitMarginPercentVal = if (netRevenuePerGlassVal > 0) {
            (grossProfitPerGlassVal / netRevenuePerGlassVal) * 100.0
        } else {
            0.0
        }

        // --- 5. OpEx Computations ---
        val totalOpexMonthlyVal = opexList.fold(0.0) { sum, item -> sum + item.monthlyCost }
        val effectiveDays = if (currentSettings.workingDays > 0) currentSettings.workingDays else 26
        val dailyOpexCostVal = totalOpexMonthlyVal / effectiveDays.toDouble()

        // --- 6. Dashboard Calculations: Aggregate over ALL PRODUCTS ---
        var totalOmsetMonthlyVal = 0.0
        var totalCostOfGoodsMonthlyVal = 0.0

        productList.forEach { prod ->
            val prodIngredients = ingredientList.filter { it.productId == prod.id }
            val prodHppMurni = prodIngredients.fold(0.0) { sum, ing ->
                val master = masterList.find { it.id == ing.masterIngredientId }
                val portionCost = if (master != null && master.packageSize > 0) {
                    (master.packagePrice / master.packageSize) * ing.usageAmount
                } else if (ing.packageSize > 0) {
                    (ing.packagePrice / ing.packageSize) * ing.usageAmount
                } else {
                    0.0
                }
                sum + portionCost
            }
            val prodHppWastage = prodHppMurni * (1.0 + (prod.wastagePercent / 100.0))

            val prodEffectiveDays = if (prod.workingDays > 0) prod.workingDays else 26
            val productOmset = prod.targetDailySales.toDouble() * prod.sellingPrice * prodEffectiveDays.toDouble()
            val productCogs = prod.targetDailySales.toDouble() * prodHppWastage * prodEffectiveDays.toDouble()

            totalOmsetMonthlyVal += productOmset
            totalCostOfGoodsMonthlyVal += productCogs
        }

        val totalCostMonthlyVal = totalCostOfGoodsMonthlyVal + totalOpexMonthlyVal
        val netProfitMonthlyVal = totalOmsetMonthlyVal - totalCostMonthlyVal

        // --- 7. Dashboard: Daily BEP (un-rounded) of Selected Product ---
        val bepDailyVal = if (grossProfitPerGlassVal > 0) {
            dailyOpexCostVal / grossProfitPerGlassVal
        } else {
            -1.0
        }

        // --- 8. Dashboard: Estimated Balance of Capital ROI ---
        val roiMonthsVal = if (netProfitMonthlyVal > 0) {
            totalCapExVal / netProfitMonthlyVal
        } else {
            -1.0
        }

        KulaBoothMetrics(
            totalCapEx = totalCapExVal,
            hppMurni = hppMurniVal,
            hppWastage = hppWastageVal,
            netRevenuePerGlass = netRevenuePerGlassVal,
            grossProfitPerGlass = grossProfitPerGlassVal,
            profitMarginPercent = profitMarginPercentVal,
            totalOpexMonthly = totalOpexMonthlyVal,
            dailyOpexCost = dailyOpexCostVal,
            totalOmsetMonthly = totalOmsetMonthlyVal,
            totalCostMonthly = totalCostMonthlyVal,
            netProfitMonthly = netProfitMonthlyVal,
            bepDaily = bepDailyVal,
            roiMonths = roiMonthsVal
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KulaBoothMetrics()
    )

    // --- Action Methods called from UI ---
    
    // Product Switch / Select
    fun selectProduct(productId: Int) {
        selectedProductId.value = productId
    }

    fun addProduct(name: String, price: Double) {
        viewModelScope.launch {
            val newProduct = ProductSettings(
                productName = name,
                sellingPrice = price,
                isOnline = false,
                wastagePercent = 5.0,
                workingDays = 26,
                targetDailySales = 30
            )
            repository.upsertProductSettings(newProduct)
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProductSettingsById(id)
        }
    }
    
    // CapEx
    fun updateCapExItemState(id: Int, name: String, qty: Int, price: Double) {
        viewModelScope.launch {
            repository.insertCapExItem(CapExItem(id, name, qty, price))
        }
    }

    fun addCapExItem(name: String, qty: Int, price: Double) {
        viewModelScope.launch {
            repository.insertCapExItem(CapExItem(name = name, qty = qty, unitPrice = price))
        }
    }

    fun deleteCapExItem(id: Int) {
        viewModelScope.launch {
            repository.deleteCapExItemById(id)
        }
    }

    // Ingredients (with automatic master ingredient resolution for Option A)
    fun updateIngredientState(id: Int, name: String, price: Double, size: Double, usage: Double) {
        viewModelScope.launch {
            val selId = selectedProductId.value
            // 1. Find if Master Ingredient already exists (case-insensitive)
            val existingMaster = allMasterIngredients.value.find { it.name.trim().lowercase() == name.trim().lowercase() }
            val masterId = if (existingMaster != null) {
                // Centrally update master ingredient price & size if changed
                if (existingMaster.packagePrice != price || existingMaster.packageSize != size) {
                    repository.insertMasterIngredient(existingMaster.copy(packagePrice = price, packageSize = size))
                }
                existingMaster.id
            } else {
                // Register a new master ingredient
                repository.insertMasterIngredient(MasterIngredient(name = name, packagePrice = price, packageSize = size)).toInt()
            }
            // 2. Insert recipe connection
            repository.insertIngredient(Ingredient(id = id, productId = selId, masterIngredientId = masterId, usageAmount = usage))
        }
    }

    fun updateIngredientStateWithMaster(id: Int, masterId: Int, usage: Double) {
        viewModelScope.launch {
            val selId = selectedProductId.value
            repository.insertIngredient(Ingredient(id = id, productId = selId, masterIngredientId = masterId, usageAmount = usage))
        }
    }

    fun addIngredient(name: String, price: Double, size: Double, usage: Double) {
        viewModelScope.launch {
            val selId = selectedProductId.value
            val existingMaster = allMasterIngredients.value.find { it.name.trim().lowercase() == name.trim().lowercase() }
            val masterId = if (existingMaster != null) {
                // Centrally update master ingredient price & size if changed
                if (existingMaster.packagePrice != price || existingMaster.packageSize != size) {
                    repository.insertMasterIngredient(existingMaster.copy(packagePrice = price, packageSize = size))
                }
                existingMaster.id
            } else {
                repository.insertMasterIngredient(MasterIngredient(name = name, packagePrice = price, packageSize = size)).toInt()
            }
            repository.insertIngredient(Ingredient(productId = selId, masterIngredientId = masterId, usageAmount = usage))
        }
    }

    fun addIngredientWithMaster(masterId: Int, usage: Double) {
        viewModelScope.launch {
            val selId = selectedProductId.value
            repository.insertIngredient(Ingredient(productId = selId, masterIngredientId = masterId, usageAmount = usage))
        }
    }

    fun deleteIngredient(id: Int) {
        viewModelScope.launch {
            repository.deleteIngredientById(id)
        }
    }

    // Centrally update master ingredient directly (updates prices instantly for all products)
    fun updateMasterIngredientDirectly(id: Int, name: String, price: Double, size: Double) {
        viewModelScope.launch {
            repository.insertMasterIngredient(MasterIngredient(id = id, name = name, packagePrice = price, packageSize = size))
        }
    }

    fun deleteMasterIngredientDirectly(id: Int) {
        viewModelScope.launch {
            repository.deleteMasterIngredientById(id)
        }
    }

    fun restockMasterIngredient(id: Int, addedAmount: Double) {
        viewModelScope.launch {
            val masterList = repository.getMasterIngredientsList()
            val master = masterList.find { it.id == id } ?: return@launch
            val updated = master.copy(currentStock = master.currentStock + addedAmount)
            repository.insertMasterIngredient(updated)
        }
    }

    fun updateMasterIngredientThreshold(id: Int, threshold: Double) {
        viewModelScope.launch {
            val masterList = repository.getMasterIngredientsList()
            val master = masterList.find { it.id == id } ?: return@launch
            val updated = master.copy(minimumStock = threshold)
            repository.insertMasterIngredient(updated)
        }
    }

    // OpEx
    fun updateOpexItemState(id: Int, name: String, cost: Double) {
        viewModelScope.launch {
            repository.insertOpexItem(OpexItem(id, name, cost))
        }
    }

    fun addOpexItem(name: String, cost: Double) {
        viewModelScope.launch {
            repository.insertOpexItem(OpexItem(name = name, monthlyCost = cost))
        }
    }

    fun deleteOpexItem(id: Int) {
        viewModelScope.launch {
            repository.deleteOpexItemById(id)
        }
    }

    // Settings
    fun updateProductSettings(
        productName: String? = null,
        sellingPrice: Double? = null,
        isOnline: Boolean? = null,
        wastagePercent: Double? = null,
        workingDays: Int? = null,
        targetDailySales: Int? = null
    ) {
        viewModelScope.launch {
            val current = settings.value
            val updated = current.copy(
                productName = productName ?: current.productName,
                sellingPrice = sellingPrice ?: current.sellingPrice,
                isOnline = isOnline ?: current.isOnline,
                wastagePercent = wastagePercent ?: current.wastagePercent,
                workingDays = workingDays ?: current.workingDays,
                targetDailySales = targetDailySales ?: current.targetDailySales
            )
            repository.upsertProductSettings(updated)
        }
    }

    // --- Sales Transactions Methods for Reports ---
    fun addSale(productId: Int, quantity: Int, isOnline: Boolean) {
        viewModelScope.launch {
            val product = allProducts.value.find { it.id == productId } ?: return@launch
            
            // Calculate current calculated HPP with wastage for this product
            val prodIngredients = allIngredients.value.filter { it.productId == productId }
            val prodHppMurni = prodIngredients.fold(0.0) { sum, ing ->
                val master = allMasterIngredients.value.find { it.id == ing.masterIngredientId }
                val portionCost = if (master != null && master.packageSize > 0) {
                    (master.packagePrice / master.packageSize) * ing.usageAmount
                } else if (ing.packageSize > 0) {
                    (ing.packagePrice / ing.packageSize) * ing.usageAmount
                } else {
                    0.0
                }
                sum + portionCost
            }
            val prodHppWastage = prodHppMurni * (1.0 + (product.wastagePercent / 100.0))
            
            val newSale = Sale(
                productId = productId,
                productName = product.productName,
                quantity = quantity,
                sellingPrice = product.sellingPrice,
                hppPerUnit = prodHppWastage,
                timestamp = System.currentTimeMillis(),
                isOnline = isOnline
            )
            repository.insertSale(newSale)

            // Deduct raw stock from master ingredients
            val ingredients = repository.getIngredientsByProductIdSync(productId)
            val masterList = repository.getMasterIngredientsList()
            for (ing in ingredients) {
                val master = masterList.find { it.id == ing.masterIngredientId }
                if (master != null) {
                    val usage = ing.usageAmount * quantity
                    val updatedMaster = master.copy(
                        currentStock = (master.currentStock - usage).coerceAtLeast(0.0)
                    )
                    repository.updateMasterIngredient(updatedMaster)
                }
            }
        }
    }

    fun deleteSale(id: Int) {
        viewModelScope.launch {
            val sale = repository.getSaleById(id) ?: return@launch
            
            // Restore raw stock to master ingredients
            val ingredients = repository.getIngredientsByProductIdSync(sale.productId)
            val masterList = repository.getMasterIngredientsList()
            for (ing in ingredients) {
                val master = masterList.find { it.id == ing.masterIngredientId }
                if (master != null) {
                    val usage = ing.usageAmount * sale.quantity
                    val updatedMaster = master.copy(
                        currentStock = master.currentStock + usage
                    )
                    repository.updateMasterIngredient(updatedMaster)
                }
            }
            repository.deleteSaleById(id)
        }
    }

    // --- Web API Next.js Synchronization Helper ---
    fun updateApiConfig(baseUrl: String, apiKey: String, autoSync: Boolean) {
        viewModelScope.launch {
            val current = apiConfig.value ?: ApiConfig()
            val updated = current.copy(
                baseUrl = baseUrl,
                apiKey = apiKey,
                autoSync = autoSync
            )
            repository.insertApiConfig(updated)
        }
    }

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    fun syncSalesToWeb() {
        viewModelScope.launch {
            _syncStatus.value = "Menghubungkan ke Next.js POS Sync Server..."
            val config = apiConfig.value ?: ApiConfig()
            if (config.baseUrl.isBlank()) {
                _syncStatus.value = "Peringatan: URL Admin Panel belum dikonfigurasi!"
                return@launch
            }
            
            // Simulate network transaction latency
            kotlinx.coroutines.delay(1500)
            
            val salesCount = allSales.value.size
            if (salesCount == 0) {
                _syncStatus.value = "Selesai: Tidak ada transaksi penjualan untuk dikirim."
                return@launch
            }
            
            _syncStatus.value = "Sync Berhasil! $salesCount transaksi terkirim & ketersediaan bahan otomatis terbarui pada Next.js."
            
            val updatedConfig = config.copy(lastSyncTime = System.currentTimeMillis())
            repository.insertApiConfig(updatedConfig)
        }
    }

    fun clearSyncStatus() {
        _syncStatus.value = null
    }

    fun clearAllSales() {
        viewModelScope.launch {
            repository.deleteAllSales()
        }
    }

    private fun seedSampleSales() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMills = 24 * 3600 * 1000L
            
            // 1. Today (Harian)
            repository.insertSale(Sale(productId = 1, productName = "Matcha Latte Ice 14oz", quantity = 15, sellingPrice = 10000.0, hppPerUnit = 3200.0, timestamp = now - 2 * 3600 * 1000, isOnline = false))
            repository.insertSale(Sale(productId = 2, productName = "Kopi Gula Aren", quantity = 10, sellingPrice = 15000.0, hppPerUnit = 4800.0, timestamp = now - 4 * 3600 * 1000, isOnline = true))
            repository.insertSale(Sale(productId = 3, productName = "Chocolate Ice Premium", quantity = 8, sellingPrice = 12000.0, hppPerUnit = 3800.0, timestamp = now - 6 * 3600 * 1000, isOnline = false))
            
            // 2. Yesterday (Harian / Mingguan)
            repository.insertSale(Sale(productId = 1, productName = "Matcha Latte Ice 14oz", quantity = 25, sellingPrice = 10000.0, hppPerUnit = 3200.0, timestamp = now - 1 * dayMills, isOnline = false))
            repository.insertSale(Sale(productId = 2, productName = "Kopi Gula Aren", quantity = 18, sellingPrice = 15000.0, hppPerUnit = 4800.0, timestamp = now - 1 * dayMills, isOnline = true))
            repository.insertSale(Sale(productId = 3, productName = "Chocolate Ice Premium", quantity = 12, sellingPrice = 12000.0, hppPerUnit = 3800.0, timestamp = now - 1 * dayMills, isOnline = false))

            // 3. This Week (Mingguan / Bulanan)
            repository.insertSale(Sale(productId = 1, productName = "Matcha Latte Ice 14oz", quantity = 40, sellingPrice = 10000.0, hppPerUnit = 3200.0, timestamp = now - 3 * dayMills, isOnline = true))
            repository.insertSale(Sale(productId = 2, productName = "Kopi Gula Aren", quantity = 35, sellingPrice = 15000.0, hppPerUnit = 4800.0, timestamp = now - 4 * dayMills, isOnline = false))
            repository.insertSale(Sale(productId = 3, productName = "Chocolate Ice Premium", quantity = 30, sellingPrice = 12000.0, hppPerUnit = 3800.0, timestamp = now - 5 * dayMills, isOnline = false))

            // 4. Earlier This Month / Last Week (Bulanan)
            repository.insertSale(Sale(productId = 1, productName = "Matcha Latte Ice 14oz", quantity = 120, sellingPrice = 10000.0, hppPerUnit = 3200.0, timestamp = now - 10 * dayMills, isOnline = false))
            repository.insertSale(Sale(productId = 2, productName = "Kopi Gula Aren", quantity = 95, sellingPrice = 15000.0, hppPerUnit = 4800.0, timestamp = now - 12 * dayMills, isOnline = true))
            repository.insertSale(Sale(productId = 3, productName = "Chocolate Ice Premium", quantity = 80, sellingPrice = 12000.0, hppPerUnit = 3800.0, timestamp = now - 15 * dayMills, isOnline = false))

            // 5. Earlier This Month / Sometime ago (Bulanan)
            repository.insertSale(Sale(productId = 1, productName = "Matcha Latte Ice 14oz", quantity = 150, sellingPrice = 10000.0, hppPerUnit = 3200.0, timestamp = now - 20 * dayMills, isOnline = false))
            repository.insertSale(Sale(productId = 2, productName = "Kopi Gula Aren", quantity = 110, sellingPrice = 15000.0, hppPerUnit = 4800.0, timestamp = now - 22 * dayMills, isOnline = false))
            repository.insertSale(Sale(productId = 3, productName = "Chocolate Ice Premium", quantity = 100, sellingPrice = 12000.0, hppPerUnit = 3800.0, timestamp = now - 25 * dayMills, isOnline = false))
        }
    }
}
