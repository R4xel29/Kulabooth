package com.example.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Part
import retrofit2.http.Query

interface KulaBoothApiService {
    @POST("api/pos/sync")
    suspend fun syncSales(
        @Body request: SyncRequest
    ): Response<SyncResponse>

    @GET("api/pos/sync")
    suspend fun getProducts(
        @Query("api_key") apiKey: String
    ): Response<ProductsSyncResponse>

    @Multipart
    @POST("api/pos/upload-image")
    suspend fun uploadProductImage(
        @Part("api_key") apiKey: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
}

data class ProductsSyncResponse(
    val success: Boolean,
    val products: List<WebProduct>,
    val ingredient_stocks: List<WebIngredientStock>?
)

data class WebIngredientStock(
    val id: String,
    val name: String,
    val unit: String,
    val stock: Double,
    val costPerUnit: Int
)

data class ImageUploadResponse(
    val success: Boolean,
    val imageUrl: String?,
    val error: String?
)

data class WebProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val image: String?,
    val category: String
)

data class SyncRequest(
    val api_key: String,
    val client: String,
    val sales_transactions: List<SyncTransaction>,
    val local_products: List<LocalProductDto>,
    val opex_items: List<OpexDto>,
    val master_ingredients: List<MasterIngredientDto>,
    val recipes: List<RecipeDto>
)

data class MasterIngredientDto(
    val id: Int,
    val name: String,
    val unit: String,
    val packagePrice: Double,
    val packageSize: Double,
    val currentStock: Double,
    val minimumStock: Double
)

data class RecipeDto(
    val id: Int,
    val productId: Int,
    val masterIngredientId: Int,
    val usageAmount: Double
)

data class OpexDto(
    val id: Int,
    val name: String,
    val monthlyCost: Double
)

data class LocalProductDto(
    val id: Int,
    val name: String,
    val price: Double
)

data class SyncTransaction(
    val id: Int,
    val productId: Int,
    val productName: String,
    val sellingPrice: Double,
    val quantity: Int,
    val timestamp: Long
)

data class SyncResponse(
    val success: Boolean,
    val message: String,
    val client: String,
    val processed: List<String>,
    val skipped: List<String>,
    val created_products: List<CreatedProductDto>?,
    val processed_opex: List<String>?,
    val processed_ingredients: Int?,
    val processed_recipes: Int?,
    val sync_errors: List<String>?
)

data class CreatedProductDto(
    val localId: Int,
    val webId: String
)
