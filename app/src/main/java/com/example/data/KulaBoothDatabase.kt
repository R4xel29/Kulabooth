package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CapExItem::class,
        Ingredient::class,
        OpexItem::class,
        ProductSettings::class,
        MasterIngredient::class,
        Sale::class,
        ApiConfig::class
    ],
    version = 7,
    exportSchema = false
)
abstract class KulaBoothDatabase : RoomDatabase() {

    abstract fun kulaBoothDao(): KulaBoothDao

    companion object {
        @Volatile
        private var INSTANCE: KulaBoothDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KulaBoothDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KulaBoothDatabase::class.java,
                    "kulabooth_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(KulaBoothDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class KulaBoothDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.beginTransaction()
            try {
                // Seeding Tab 1: CapEx (Modal Awal & Alat)
                db.execSQL("INSERT INTO capex_items (id, name, qty, unitPrice) VALUES (1, 'Booth / Gerobak Kayu Minimalis', 1, 2500000.0)")
                db.execSQL("INSERT INTO capex_items (id, name, qty, unitPrice) VALUES (2, 'Cup Sealer Manual Matrix', 1, 750000.0)")
                db.execSQL("INSERT INTO capex_items (id, name, qty, unitPrice) VALUES (3, 'Termos Es Batu Jumbo Gajah', 1, 180000.0)")
                db.execSQL("INSERT INTO capex_items (id, name, qty, unitPrice) VALUES (4, 'Timbangan Digital & Sendok Takar', 1, 70000.0)")

                // Seeding Master Ingredients database table (Daftar Bahan Baku Global with stock and threshold)
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (1, 'Powder Matcha Premix', 'gr', 65000.0, 1000.0, 5000.0, 500.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (2, 'Susu Evaporasi', 'ml', 16000.0, 400.0, 2400.0, 400.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (3, 'Air & Es Batu Kristal', 'ml', 10000.0, 5000.0, 25000.0, 5000.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (4, 'Plastic Cup + Lid 14oz', 'pcs', 500.0, 1.0, 150.0, 30.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (5, 'Sedotan & Kantong', 'pcs', 150.0, 1.0, 150.0, 30.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (6, 'Espresso Roast Java', 'gr', 120000.0, 1000.0, 4000.0, 500.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (7, 'Susu UHT Full Cream', 'ml', 18000.0, 1000.0, 10000.0, 2000.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (8, 'Sirup Gula Aren', 'ml', 35000.0, 1000.0, 4000.0, 500.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (9, 'Premium Cocoa Powder', 'gr', 90000.0, 1000.0, 3000.0, 500.0)")
                db.execSQL("INSERT INTO master_ingredients (id, name, unit, packagePrice, packageSize, currentStock, minimumStock) VALUES (10, 'Susu Kental Manis', 'ml', 12000.0, 370.0, 2220.0, 370.0)")

                // Seeding Product Settings (Products)
                db.execSQL("INSERT INTO product_settings (id, productName, sellingPrice, isOnline, wastagePercent, workingDays, targetDailySales) VALUES (1, 'Matcha Latte Ice 14oz', 10000.0, 0, 5.0, 26, 40)")
                db.execSQL("INSERT INTO product_settings (id, productName, sellingPrice, isOnline, wastagePercent, workingDays, targetDailySales) VALUES (2, 'Kopi Gula Aren', 15000.0, 0, 5.0, 26, 30)")
                db.execSQL("INSERT INTO product_settings (id, productName, sellingPrice, isOnline, wastagePercent, workingDays, targetDailySales) VALUES (3, 'Chocolate Ice Premium', 12000.0, 0, 5.0, 26, 25)")

                // Seeding Tab 2: Ingredients associated with productIds referencing the masterIngredientId
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (1, 1, 1, '', 0.0, 0.0, 25.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (2, 1, 2, '', 0.0, 0.0, 20.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (3, 1, 3, '', 0.0, 0.0, 250.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (4, 1, 4, '', 0.0, 0.0, 1.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (5, 1, 5, '', 0.0, 0.0, 1.0)")

                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (6, 2, 6, '', 0.0, 0.0, 15.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (7, 2, 7, '', 0.0, 0.0, 120.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (8, 2, 8, '', 0.0, 0.0, 20.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (9, 2, 4, '', 0.0, 0.0, 1.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (10, 2, 5, '', 0.0, 0.0, 1.0)")

                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (11, 3, 9, '', 0.0, 0.0, 30.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (12, 3, 10, '', 0.0, 0.0, 40.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (13, 3, 2, '', 0.0, 0.0, 30.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (14, 3, 4, '', 0.0, 0.0, 1.0)")
                db.execSQL("INSERT INTO ingredients (id, productId, masterIngredientId, name, packagePrice, packageSize, usageAmount) VALUES (15, 3, 5, '', 0.0, 0.0, 1.0)")

                // Seeding Tab 3: OpEx (Biaya Operasional)
                db.execSQL("INSERT INTO opex_items (id, name, monthlyCost) VALUES (1, 'Sewa Lapak Teras Minimarket', 600000.0)")
                db.execSQL("INSERT INTO opex_items (id, name, monthlyCost) VALUES (2, 'Biaya Listrik & Kebersihan', 200000.0)")
                db.execSQL("INSERT INTO opex_items (id, name, monthlyCost) VALUES (3, 'Gaji Asisten Booth (Jika ada)', 0.0)")

                // Seeding API configuration row for web synchronization integration
                db.execSQL("INSERT INTO api_configs (id, baseUrl, apiKey, autoSync, lastSyncTime) VALUES (1, 'https://api.arumseduh.com', 'mb_live_a1b9f7c3e8d24b60a9c8e7f5d63b2a19', 0, 0)")

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
