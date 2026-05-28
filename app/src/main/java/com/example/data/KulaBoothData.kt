package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capex_items")
data class CapExItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val qty: Int,
    val unitPrice: Double
) {
    val totalPrice: Double
        get() = qty * unitPrice
}

@Entity(tableName = "master_ingredients")
data class MasterIngredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val unit: String = "gr",
    val packagePrice: Double,
    val packageSize: Double, // in gram/ml/pcs
    val currentStock: Double = 0.0,
    val minimumStock: Double = 0.0
)

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int = 1, // linked to ProductSettings id
    val masterIngredientId: Int = 0, // linked to MasterIngredient id
    val name: String = "", // keep for backward compatibility or direct inputs
    val packagePrice: Double = 0.0, // keep for backward compatibility
    val packageSize: Double = 0.0, // keep for backward compatibility
    val usageAmount: Double  // in gram/ml/pcs
) {
    val costPerPortion: Double
        get() = if (packageSize > 0) (packagePrice / packageSize) * usageAmount else 0.0
}

data class IngredientWithMaster(
    val ingredient: Ingredient,
    val master: MasterIngredient?
) {
    val id: Int get() = ingredient.id
    val name: String get() = master?.name ?: ingredient.name
    val packagePrice: Double get() = master?.packagePrice ?: ingredient.packagePrice
    val packageSize: Double get() = master?.packageSize ?: ingredient.packageSize
    val usageAmount: Double get() = ingredient.usageAmount
    val costPerPortion: Double
        get() = if (packageSize > 0) (packagePrice / packageSize) * usageAmount else 0.0
}

@Entity(tableName = "opex_items")
data class OpexItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val monthlyCost: Double
)

@Entity(tableName = "product_settings")
data class ProductSettings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val webId: String? = null,
    val productName: String = "Matcha Latte Ice 14oz",
    val sellingPrice: Double = 8000.0,
    val isOnline: Boolean = false, // false = Offline/Langsung, true = Online Delivery (20% commission)
    val wastagePercent: Double = 5.0, // 5% by default
    val workingDays: Int = 26,       // 26 days default
    val targetDailySales: Int = 50,  // 50 cups default
    val imageUrl: String? = null
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val sellingPrice: Double,
    val hppPerUnit: Double,
    val timestamp: Long,
    val isOnline: Boolean
)

@Entity(tableName = "api_configs")
data class ApiConfig(
    @PrimaryKey val id: Int = 1,
    val baseUrl: String = "https://api.arumseduh.com",
    val apiKey: String = "mb_live_a1b9f7c3e8d24b60a9c8e7f5d63b2a19",
    val autoSync: Boolean = false,
    val lastSyncTime: Long = 0L
)



