package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KulaBoothDao {
    // --- CapEx ---
    @Query("SELECT * FROM capex_items ORDER BY id ASC")
    fun getAllCapExItems(): Flow<List<CapExItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapExItem(item: CapExItem)

    @Update
    suspend fun updateCapExItem(item: CapExItem)

    @Delete
    suspend fun deleteCapExItem(item: CapExItem)

    @Query("DELETE FROM capex_items WHERE id = :id")
    suspend fun deleteCapExItemById(id: Int)


    // --- Ingredients ---
    @Query("SELECT * FROM ingredients ORDER BY id ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE productId = :productId ORDER BY id ASC")
    fun getIngredientsByProduct(productId: Int): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredientById(id: Int)

    @Query("DELETE FROM ingredients WHERE productId = :productId")
    suspend fun deleteIngredientsByProductId(productId: Int)


    // --- Master Ingredients ---
    @Query("SELECT * FROM master_ingredients ORDER BY id ASC")
    fun getAllMasterIngredients(): Flow<List<MasterIngredient>>

    @Query("SELECT * FROM master_ingredients")
    suspend fun getMasterIngredientsList(): List<MasterIngredient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterIngredient(master: MasterIngredient): Long

    @Update
    suspend fun updateMasterIngredient(master: MasterIngredient)

    @Delete
    suspend fun deleteMasterIngredient(master: MasterIngredient)

    @Query("DELETE FROM master_ingredients WHERE id = :id")
    suspend fun deleteMasterIngredientById(id: Int)



    // --- OpEx ---
    @Query("SELECT * FROM opex_items ORDER BY id ASC")
    fun getAllOpexItems(): Flow<List<OpexItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpexItem(item: OpexItem)

    @Update
    suspend fun updateOpexItem(item: OpexItem)

    @Delete
    suspend fun deleteOpexItem(item: OpexItem)

    @Query("DELETE FROM opex_items WHERE id = :id")
    suspend fun deleteOpexItemById(id: Int)


    // --- Product Settings ---
    @Query("SELECT * FROM product_settings ORDER BY id ASC")
    fun getAllProductSettings(): Flow<List<ProductSettings>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProductSettings(settings: ProductSettings): Long

    @Delete
    suspend fun deleteProductSettings(settings: ProductSettings)

    @Query("DELETE FROM product_settings WHERE id = :id")
    suspend fun deleteProductSettingsById(id: Int)

    @Query("SELECT * FROM ingredients WHERE productId = :productId")
    suspend fun getIngredientsByProductIdSync(productId: Int): List<Ingredient>

    // --- Sales Transactions ---
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: Int): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Int)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()

    // --- API & Sync Configuration ---
    @Query("SELECT * FROM api_configs WHERE id = 1 LIMIT 1")
    fun getApiConfig(): Flow<ApiConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiConfig(config: ApiConfig)
}
