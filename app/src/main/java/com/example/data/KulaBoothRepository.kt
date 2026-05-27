package com.example.data

import kotlinx.coroutines.flow.Flow

class KulaBoothRepository(private val dao: KulaBoothDao) {

    val allCapExItems: Flow<List<CapExItem>> = dao.getAllCapExItems()
    val allIngredients: Flow<List<Ingredient>> = dao.getAllIngredients()
    val allMasterIngredients: Flow<List<MasterIngredient>> = dao.getAllMasterIngredients()
    val allOpexItems: Flow<List<OpexItem>> = dao.getAllOpexItems()
    val allProductSettings: Flow<List<ProductSettings>> = dao.getAllProductSettings()

    // --- CapEx Transactions ---
    suspend fun insertCapExItem(item: CapExItem) = dao.insertCapExItem(item)
    suspend fun updateCapExItem(item: CapExItem) = dao.updateCapExItem(item)
    suspend fun deleteCapExItem(item: CapExItem) = dao.deleteCapExItem(item)
    suspend fun deleteCapExItemById(id: Int) = dao.deleteCapExItemById(id)

    // --- Master Ingredients Transactions ---
    suspend fun insertMasterIngredient(master: MasterIngredient) = dao.insertMasterIngredient(master)
    suspend fun updateMasterIngredient(master: MasterIngredient) = dao.updateMasterIngredient(master)
    suspend fun deleteMasterIngredient(master: MasterIngredient) = dao.deleteMasterIngredient(master)
    suspend fun deleteMasterIngredientById(id: Int) = dao.deleteMasterIngredientById(id)

    // --- Ingredients Transactions ---
    suspend fun insertIngredient(ingredient: Ingredient) = dao.insertIngredient(ingredient)
    suspend fun updateIngredient(ingredient: Ingredient) = dao.updateIngredient(ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient) = dao.deleteIngredient(ingredient)
    suspend fun deleteIngredientById(id: Int) = dao.deleteIngredientById(id)
    suspend fun deleteIngredientsByProductId(productId: Int) = dao.deleteIngredientsByProductId(productId)

    // --- OpEx Transactions ---
    suspend fun insertOpexItem(item: OpexItem) = dao.insertOpexItem(item)
    suspend fun updateOpexItem(item: OpexItem) = dao.updateOpexItem(item)
    suspend fun deleteOpexItem(item: OpexItem) = dao.deleteOpexItem(item)
    suspend fun deleteOpexItemById(id: Int) = dao.deleteOpexItemById(id)

    // --- Product Settings Transactions ---
    suspend fun upsertProductSettings(settings: ProductSettings) = dao.upsertProductSettings(settings)
    suspend fun deleteProductSettings(settings: ProductSettings) = dao.deleteProductSettings(settings)
    suspend fun deleteProductSettingsById(id: Int) {
        dao.deleteProductSettingsById(id)
        dao.deleteIngredientsByProductId(id)
    }

    // --- Sales Transactions ---
    val allSales: Flow<List<Sale>> = dao.getAllSales()
    suspend fun getSaleById(id: Int): Sale? = dao.getSaleById(id)
    suspend fun insertSale(sale: Sale) = dao.insertSale(sale)
    suspend fun deleteSale(sale: Sale) = dao.deleteSale(sale)
    suspend fun deleteSaleById(id: Int) = dao.deleteSaleById(id)
    suspend fun deleteAllSales() = dao.deleteAllSales()

    suspend fun getIngredientsByProductIdSync(productId: Int): List<Ingredient> = dao.getIngredientsByProductIdSync(productId)
    suspend fun getMasterIngredientsList(): List<MasterIngredient> = dao.getMasterIngredientsList()

    // --- API & Sync Configuration ---
    val apiConfig: Flow<ApiConfig?> = dao.getApiConfig()
    suspend fun insertApiConfig(config: ApiConfig) = dao.insertApiConfig(config)
}
