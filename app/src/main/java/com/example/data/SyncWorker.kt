package com.example.data

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class SyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = KulaBoothDatabase.getDatabase(applicationContext, kotlinx.coroutines.MainScope())
            val repository = KulaBoothRepository(db.kulaBoothDao())
            val config = repository.apiConfig.first() ?: ApiConfig()

            if (!config.autoSync || config.baseUrl.isBlank()) {
                return Result.success()
            }

            var baseUrl = config.baseUrl.trim()
            if (!baseUrl.endsWith("/")) baseUrl += "/"
            if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) baseUrl = "http://$baseUrl"

            val salesList = repository.allSales.first()
            val localProductsToSync = repository.allProductSettings.first()
                .filter { it.webId == null }
                .map { prod -> LocalProductDto(id = prod.id, name = prod.productName, price = prod.sellingPrice) }

            val opexListToSync = repository.allOpexItems.first()
                .map { opex -> OpexDto(id = opex.id, name = opex.name, monthlyCost = opex.monthlyCost) }

            val masterIngredientsToSync = repository.getMasterIngredientsList()
                .map { master ->
                    MasterIngredientDto(
                        id = master.id,
                        name = master.name,
                        unit = master.unit,
                        packagePrice = master.packagePrice,
                        packageSize = master.packageSize,
                        currentStock = master.currentStock,
                        minimumStock = master.minimumStock
                    )
                }

            val recipesToSync = repository.allIngredients.first()
                .map { ing ->
                    RecipeDto(
                        id = ing.id,
                        productId = ing.productId,
                        masterIngredientId = ing.masterIngredientId,
                        usageAmount = ing.usageAmount
                    )
                }

            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
                .build()

            val apiService = retrofit.create(KulaBoothApiService::class.java)

            val transactions = salesList.map { sale ->
                SyncTransaction(
                    id = sale.id,
                    productId = sale.productId,
                    productName = sale.productName,
                    sellingPrice = sale.sellingPrice,
                    quantity = sale.quantity,
                    timestamp = sale.timestamp
                )
            }

            val requestBody = SyncRequest(
                api_key = config.apiKey,
                client = "KulaBooth AutoSync Worker",
                sales_transactions = transactions,
                local_products = localProductsToSync,
                opex_items = opexListToSync,
                master_ingredients = masterIngredientsToSync,
                recipes = recipesToSync
            )

            val response = apiService.syncSales(requestBody)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    // Update webId local products yang berhasil didaftarkan di server
                    body.created_products?.forEach { created ->
                        val allProducts = repository.allProductSettings.first()
                        val localProd = allProducts.find { it.id == created.localId }
                        if (localProd != null) {
                            repository.upsertProductSettings(localProd.copy(webId = created.webId))
                        }
                    }
                    // Update waktu sync terakhir
                    repository.insertApiConfig(config.copy(lastSyncTime = System.currentTimeMillis()))
                    Result.success()
                } else {
                    Result.retry()
                }
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "KulaBoothAutoSync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
