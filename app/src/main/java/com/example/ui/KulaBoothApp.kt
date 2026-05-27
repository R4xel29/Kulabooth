package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.KulaBoothMetrics
import com.example.viewmodel.KulaBoothViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KulaBoothApp(viewModel: KulaBoothViewModel) {
    val capExItems by viewModel.capExItems.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val allMasterIngredients by viewModel.allMasterIngredients.collectAsState()
    val opexItems by viewModel.opexItems.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val selectedProductId by viewModel.selectedProductId.collectAsState()
    val allSales by viewModel.allSales.collectAsState()
    val apiConfig by viewModel.apiConfig.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "KulaBooth v2.0",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Kalkulator Bisnis Booth & Kuliner - Konsultan Finansial UMKM",
                            color = EmeraldLight,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary
                )
            )
        },
        bottomBar = {
            KulaBoothBottomNav(
                selectedTab = activeTab,
                onTabSelected = { 
                    activeTab = it
                    focusManager.clearFocus()
                }
            )
        },
        containerColor = SlateBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (activeTab) {
                0 -> CapExTab(
                    items = capExItems,
                    totalCapEx = metrics.totalCapEx,
                    onAddItem = viewModel::addCapExItem,
                    onUpdateItem = viewModel::updateCapExItemState,
                    onDeleteItem = viewModel::deleteCapExItem
                )
                1 -> RecipeCostingTab(
                    ingredients = ingredients,
                    allMasterIngredients = allMasterIngredients,
                    settings = settings,
                    metrics = metrics,
                    allProducts = allProducts,
                    selectedProductId = selectedProductId,
                    onSelectProduct = viewModel::selectProduct,
                    onAddProduct = viewModel::addProduct,
                    onDeleteProduct = viewModel::deleteProduct,
                    onUpdateSettings = viewModel::updateProductSettings,
                    onAddIngredient = viewModel::addIngredient,
                    onUpdateIngredient = viewModel::updateIngredientState,
                    onDeleteIngredient = viewModel::deleteIngredient,
                    onUpdateMasterIngredient = viewModel::updateMasterIngredientDirectly,
                    onDeleteMasterIngredient = viewModel::deleteMasterIngredientDirectly
                )
                2 -> OpExTab(
                    opexItems = opexItems,
                    settings = settings,
                    metrics = metrics,
                    onUpdateSettings = viewModel::updateProductSettings,
                    onAddItem = viewModel::addOpexItem,
                    onUpdateItem = viewModel::updateOpexItemState,
                    onDeleteItem = viewModel::deleteOpexItem
                )
                3 -> DashboardTab(
                    settings = settings,
                    metrics = metrics,
                    onUpdateSettings = viewModel::updateProductSettings
                )
                4 -> SalesReportTab(
                    allSales = allSales,
                    allProducts = allProducts,
                    allMasterIngredients = allMasterIngredients,
                    apiConfig = apiConfig ?: com.example.data.ApiConfig(),
                    syncStatus = syncStatus,
                    onAddSale = viewModel::addSale,
                    onDeleteSale = viewModel::deleteSale,
                    onClearAllSales = viewModel::clearAllSales,
                    onUpdateConfig = viewModel::updateApiConfig,
                    onSyncSales = viewModel::syncSalesToWeb,
                    onClearSyncStatus = viewModel::clearSyncStatus,
                    onRestockIngredient = viewModel::restockMasterIngredient,
                    onUpdateThreshold = viewModel::updateMasterIngredientThreshold
                )
            }
        }
    }
}

// Custom responsive Bottom Navigation designed for finger-friendly targets and premium active pills
@Composable
fun KulaBoothBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        NavigationItem("Modal", Icons.Default.MonetizationOn, "Tab Modal Awal"),
        NavigationItem("HPP Resep", Icons.Default.MenuBook, "Tab Costing HPP"),
        NavigationItem("Operasional", Icons.Default.Build, "Tab Biaya Operasional"),
        NavigationItem("Dashboard", Icons.AutoMirrored.Filled.TrendingUp, "Tab Dashboard BEP"),
        NavigationItem("Laporan", Icons.Default.Assessment, "Tab Laporan Penjualan")
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { 
                    Icon(
                        imageVector = tab.icon, 
                        contentDescription = tab.contentDescription,
                        tint = if (selectedTab == index) EmeraldPrimary else NeutralGray
                    ) 
                },
                label = { 
                    Text(
                        text = tab.label,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        color = if (selectedTab == index) EmeraldPrimary else NeutralGray
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EmeraldLight
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector, val contentDescription: String)


// ==========================================
// TAB 1: MODAL AWAL & ALAT (CapEx)
// ==========================================
@Composable
fun CapExTab(
    items: List<CapExItem>,
    totalCapEx: Double,
    onAddItem: (String, Int, Double) -> Unit,
    onUpdateItem: (Int, String, Int, Double) -> Unit,
    onDeleteItem: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CapExItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Card depicting Summary real-time
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("capex_summary_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL INVESTASI MODAL AWAL (CapEx)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Utils.formatRupiah(totalCapEx),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Untuk membeli aset fisik, perlengkapan, dan peralatan booth kuliner.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = EmeraldLight
                        )
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "Daftar Perlengkapan & Alat (${items.size} Item)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
            }

            if (items.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        title = "Belum Ada Item Modal",
                        subtitle = "Silakan klik tombol tambah (+) di bawah untuk menginput aset awal bisnis Anda."
                    )
                }
            } else {
                items(items, key = { it.id }) { item ->
                    CapExItemRow(
                        item = item,
                        onEditClick = { editingItem = item },
                        onDeleteClick = { onDeleteItem(item.id) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_capex_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Item Modal Awal")
        }
    }

    // Dialog Add/Edit
    if (showAddDialog) {
        CapExEditDialog(
            item = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty, price ->
                onAddItem(name, qty, price)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        CapExEditDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { name, qty, price ->
                onUpdateItem(item.id, name, qty, price)
                editingItem = null
            }
        )
    }
}

@Composable
fun CapExItemRow(
    item: CapExItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().testTag("capex_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.qty} unit",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "  •  ",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "${Utils.formatRupiah(item.unitPrice)} /unit",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Subtotal: " + Utils.formatRupiah(item.totalPrice),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EmeraldDark
                )
            }

            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.testTag("edit_capex_${item.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Item CapEx",
                        tint = TealPrimary
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.testTag("delete_capex_${item.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Item CapEx",
                        tint = ErrorRed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapExEditDialog(
    item: CapExItem?,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Double) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var qtyString by remember { mutableStateOf(item?.qty?.toString() ?: "1") }
    var priceString by remember { mutableStateOf(item?.unitPrice?.toInt()?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (item == null) "Tambah Item Modal" else "Edit Item Modal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Alat / Aset") },
                    placeholder = { Text("Contoh: Booth Kayu") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("capex_name_field")
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { qtyString = it },
                    label = { Text("Jumlah (Qty)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("capex_qty_field")
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = priceString,
                    onValueChange = { priceString = it },
                    label = { Text("Harga Satuan") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("capex_price_field")
                )

                // Real-time currency preview helper under input field
                val parsedPrice = Utils.parseDouble(priceString)
                val parsedQty = Utils.parseInt(qtyString)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldLight, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Harga Satuan:", fontSize = 11.sp, color = EmeraldDark)
                        Text(Utils.formatRupiah(parsedPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Subtotal:", fontSize = 11.sp, color = EmeraldDark)
                        Text(Utils.formatRupiah(parsedPrice * parsedQty), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("capex_dialog_dismiss")) {
                        Text("Batal", color = NeutralGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    Utils.parseInt(qtyString),
                                    Utils.parseDouble(priceString)
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.testTag("capex_dialog_confirm")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}


// ==========================================
// TAB 2: COSTING RECOUP & HPP
// ==========================================
@Composable
fun RecipeCostingTab(
    ingredients: List<IngredientWithMaster>,
    allMasterIngredients: List<MasterIngredient>,
    settings: ProductSettings,
    metrics: KulaBoothMetrics,
    allProducts: List<ProductSettings>,
    selectedProductId: Int,
    onSelectProduct: (Int) -> Unit,
    onAddProduct: (String, Double) -> Unit,
    onDeleteProduct: (Int) -> Unit,
    onUpdateSettings: (productName: String?, sellingPrice: Double?, isOnline: Boolean?, wastagePercent: Double?, workingDays: Int?, targetDailySales: Int?) -> Unit,
    onAddIngredient: (String, Double, Double, Double) -> Unit,
    onUpdateIngredient: (Int, String, Double, Double, Double) -> Unit,
    onDeleteIngredient: (Int) -> Unit,
    onUpdateMasterIngredient: (Int, String, Double, Double) -> Unit,
    onDeleteMasterIngredient: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<IngredientWithMaster?>(null) }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showDeleteProductConfirm by remember { mutableStateOf(false) }

    var newProductName by remember { mutableStateOf("") }
    var newProductPrice by remember { mutableStateOf("") }

    // Text inputs local state for instant editing
    var productNameInput by remember { mutableStateOf(settings.productName) }
    var sellingPriceInput by remember { mutableStateOf(settings.sellingPrice.toInt().toString()) }
    var wastagePercentInput by remember { mutableStateOf(settings.wastagePercent.toInt().toString()) }

    // Sync input components when model settings changes
    LaunchedEffect(settings) {
        productNameInput = settings.productName
        sellingPriceInput = settings.sellingPrice.toInt().toString()
        wastagePercentInput = settings.wastagePercent.toInt().toString()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- MULTI-PRODUCT SELECTION ROW ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("product_selector_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pilih Menu / Varian Produk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark
                        )
                        IconButton(
                            onClick = { showAddProductDialog = true },
                            modifier = Modifier.size(28.dp).testTag("btn_add_product")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Tambah Menu Baru",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allProducts.forEach { prod ->
                            val isSelected = prod.id == selectedProductId
                            val bgChip = if (isSelected) EmeraldPrimary else (if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            val tcChip = if (isSelected) Color.White else (if (isSystemInDarkTheme()) Color(0xFFE2E8F0) else Color(0xFF475569))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bgChip)
                                    .clickable { onSelectProduct(prod.id) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalCafe,
                                        contentDescription = null,
                                        tint = tcChip,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = prod.productName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tcChip
                                    )
                                }
                            }
                        }
                    }

                    if (allProducts.size > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showDeleteProductConfirm = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                                modifier = Modifier.testTag("btn_delete_active_product")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Menu",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hapus Menu Terpilih", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // --- MASTER INGREDIENTS CENTRAL WIDGET (Option A) ---
        item {
            var isExpanded by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("master_ingredients_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Daftar Bahan Baku Global (${allMasterIngredients.size} Bahan)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Satu bahan untuk banyak menu. Edit harga di sini untuk massal.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (allMasterIngredients.isEmpty()) {
                            Text(
                                "Belum ada bahan baku utama.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            allMasterIngredients.forEach { master ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = master.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Kemasan: ${Utils.formatRupiah(master.packagePrice)} per ${master.packageSize.toInt()} gr/ml/pcs",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    
                                    var editingMasterItem by remember { mutableStateOf<MasterIngredient?>(null) }
                                    
                                    IconButton(
                                        onClick = { editingMasterItem = master },
                                        modifier = Modifier.size(28.dp).testTag("edit_master_${master.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Master Harga",
                                            tint = TealPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    editingMasterItem?.let { item ->
                                        MasterIngredientEditDialog(
                                            master = item,
                                            onDismiss = { editingMasterItem = null },
                                            onConfirm = { name, price, size ->
                                                onUpdateMasterIngredient(item.id, name, price, size)
                                                editingMasterItem = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Essential Inputs Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Pengaturan Produk & Pricing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Product name
                    OutlinedTextField(
                        value = productNameInput,
                        onValueChange = {
                            productNameInput = it
                            onUpdateSettings(it, null, null, null, null, null)
                        },
                        label = { Text("Nama Produk") },
                        placeholder = { Text("Contoh: Matcha Latte Ice") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rekomendasi Menu Populer:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val menuPresets = listOf(
                        Pair("Matcha Latte Ice", 15000.0),
                        Pair("Kopi Gula Aren", 12000.0),
                        Pair("Chocolate Ice Premium", 14000.0),
                        Pair("Mango Smoothies Creamy", 16000.0),
                        Pair("Thai Tea Milk Ice", 10000.0),
                        Pair("Es Teh Manis Jumbo", 5000.0)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        menuPresets.forEach { preset ->
                            val isSelected = productNameInput.equals(preset.first, ignoreCase = true)
                            val chipBg = if (isSelected) EmeraldPrimary else (if (isSystemInDarkTheme()) Color(0xFF114232) else EmeraldLight)
                            val chipText = if (isSelected) Color.White else (if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark)
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(chipBg)
                                    .clickable {
                                        productNameInput = preset.first
                                        sellingPriceInput = preset.second.toInt().toString()
                                        onUpdateSettings(preset.first, preset.second, null, null, null, null)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalCafe,
                                        contentDescription = null,
                                        tint = chipText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = preset.first,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = chipText
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Selling price & helper currency text
                    OutlinedTextField(
                        value = sellingPriceInput,
                        onValueChange = {
                            sellingPriceInput = it
                            onUpdateSettings(null, Utils.parseDouble(it), null, null, null, null)
                        },
                        label = { Text("Harga Jual ke Konsumen (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("product_price_input")
                    )
                    Text(
                        text = "Harga Terformat: " + Utils.formatRupiah(Utils.parseDouble(sellingPriceInput)),
                        fontSize = 11.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Sales Channel Toggle Pills
                    Text(
                        text = "Saluran / Channel Penjualan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!settings.isOnline) EmeraldPrimary else Color.Transparent)
                                .clickable { onUpdateSettings(null, null, false, null, null, null) }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = "Offline Store",
                                tint = if (!settings.isOnline) Color.White else Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Offline/Langsung (Komisi 0%)",
                                color = if (!settings.isOnline) Color.White else Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (settings.isOnline) EmeraldPrimary else Color.Transparent)
                                .clickable { onUpdateSettings(null, null, true, null, null, null) }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.DeliveryDining,
                                contentDescription = "Online Delivery",
                                tint = if (settings.isOnline) Color.White else Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Online Delivery (Potong 20%)",
                                color = if (settings.isOnline) Color.White else Color(0xFF475569),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Wastage / Buffer percent
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = wastagePercentInput,
                            onValueChange = {
                                wastagePercentInput = it
                                onUpdateSettings(null, null, null, Utils.parseDouble(it), null, null)
                            },
                            label = { Text("Wastage / Buffer Margin (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                                focusedLabelColor = EmeraldPrimary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).testTag("wastage_input")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        InfoTooltip(text = "Mengantisipasi bahan tumpah atau es mencair.")
                    }
                }
            }
        }

        // Section Ingredient List Header with Add Action inside
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bahan Baku Per Porsi (${ingredients.size} Bahan)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_ingredient_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Bahan", fontSize = 12.sp)
                }
            }
        }

        // Recipe items table
        if (ingredients.isEmpty()) {
            item {
                EmptyStatePlaceholder(
                    title = "Belum Ada Bahan Baku",
                    subtitle = "Tambahkan komposisi takaran bahan per porsi minuman Anda di sini."
                )
            }
        } else {
            items(ingredients, key = { it.id }) { ingredient ->
                IngredientRow(
                    ingredient = ingredient,
                    onEditClick = { editingIngredient = ingredient },
                    onDeleteClick = { onDeleteIngredient(ingredient.id) }
                )
            }
        }

        // Real-time calculated costing output cards
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth().testTag("hpp_realtime_output_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "RANGKUMAN HPP & KEUNTUNGAN (Per Gelas)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldAccent,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutputFinancialMetricRow("HPP Murni per Gelas", metrics.hppMurni)
                    OutputFinancialMetricRow("HPP + Wastage (${settings.wastagePercent}%)", metrics.hppWastage)
                    OutputFinancialMetricRow("Pendapatan Bersih (Setelah Komisi)", metrics.netRevenuePerGlass)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Profit Kotor per Gelas", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            Utils.formatRupiah(metrics.grossProfitPerGlass),
                            color = if (metrics.grossProfitPerGlass > 0) SuccessGreen else ErrorRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Margin Keuntungan (%)", 
                            color = Color(0xFF94A3B8), 
                            fontSize = 12.sp
                        )
                        Text(
                            String.format("%.1f%%", metrics.profitMarginPercent),
                            color = if (metrics.profitMarginPercent > 20) SuccessGreen else if (metrics.profitMarginPercent > 0) WarningOrange else ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Dialog sheets
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Tambah Menu Baru", fontWeight = FontWeight.Bold, color = if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Buat menu baru untuk dihitung HPP & profitnya masing-masing secara terpisah.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = newProductName,
                        onValueChange = { newProductName = it },
                        label = { Text("Nama Menu") },
                        placeholder = { Text("Contoh: Kopi Gula Aren") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_product_name_input")
                    )
                    OutlinedTextField(
                        value = newProductPrice,
                        onValueChange = { newProductPrice = it },
                        label = { Text("Harga Jual (Rp)") },
                        placeholder = { Text("Contoh: 15000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_product_price_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProductName.isNotBlank()) {
                            val priceValue = Utils.parseDouble(newProductPrice)
                            onAddProduct(newProductName, priceValue)
                            newProductName = ""
                            newProductPrice = ""
                            showAddProductDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.testTag("add_product_confirm")
                ) {
                    Text("Tambah", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDeleteProductConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteProductConfirm = false },
            title = { Text("Hapus Menu", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text("Apakah Anda yakin ingin menghapus menu \"${settings.productName}\"? Semua data resep bahan baku untuk produk ini juga akan ikut terhapus permanen.", fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(settings.id)
                        showDeleteProductConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.testTag("delete_product_confirm")
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProductConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showAddDialog) {
        IngredientEditDialog(
            ingredient = null,
            allMasterIngredients = allMasterIngredients,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, size, usage ->
                onAddIngredient(name, price, size, usage)
                showAddDialog = false
            }
        )
    }

    editingIngredient?.let { ing ->
        IngredientEditDialog(
            ingredient = ing,
            allMasterIngredients = allMasterIngredients,
            onDismiss = { editingIngredient = null },
            onConfirm = { name, price, size, usage ->
                onUpdateIngredient(ing.id, name, price, size, usage)
                editingIngredient = null
            }
        )
    }
}

@Composable
fun IngredientRow(
    ingredient: IngredientWithMaster,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().testTag("ingredient_item_${ingredient.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kemasan Global: ${Utils.formatRupiah(ingredient.packagePrice)} / ${ingredient.packageSize.toInt()} gr/ml/pcs",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Text(
                    text = "Takaran Gelas: ${ingredient.usageAmount} gr/ml/pcs",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Biaya Resep: " + Utils.formatRupiah(ingredient.costPerPortion) + " /porsi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EmeraldDark
                )
            }

            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.testTag("edit_ing_${ingredient.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Bahan",
                        tint = TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.testTag("delete_ing_${ingredient.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Bahan",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditDialog(
    ingredient: IngredientWithMaster?,
    allMasterIngredients: List<MasterIngredient>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var priceStr by remember { mutableStateOf(ingredient?.packagePrice?.toInt()?.toString() ?: "") }
    var sizeStr by remember { mutableStateOf(ingredient?.packageSize?.toInt()?.toString() ?: "") }
    var usageStr by remember { mutableStateOf(ingredient?.usageAmount?.toString() ?: "") }

    var selectFromMaster by remember { mutableStateOf(allMasterIngredients.isNotEmpty() && ingredient == null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (ingredient == null) "Tambah Bahan Resep" else "Edit Takaran Resep",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (ingredient == null && allMasterIngredients.isNotEmpty()) {
                    // Selection tabs: Select Existing (Bahan Global) vs Create New
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectFromMaster) EmeraldPrimary else Color.Transparent)
                                .clickable { selectFromMaster = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Bahan Global Ada",
                                color = if (selectFromMaster) Color.White else (if (isSystemInDarkTheme()) Color(0xFFE2E8F0) else Color(0xFF475569)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!selectFromMaster) EmeraldPrimary else Color.Transparent)
                                .clickable { selectFromMaster = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Buat Bahan Baru",
                                color = if (!selectFromMaster) Color.White else (if (isSystemInDarkTheme()) Color(0xFFE2E8F0) else Color(0xFF475569)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (selectFromMaster && ingredient == null) {
                    Text(
                        "Pilih salah satu Bahan Global untuk dimasukkan ke Resep:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var selectedMaster by remember { mutableStateOf<MasterIngredient?>(null) }

                    // Horizontal scrolling list of Master Ingredients to select safely
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allMasterIngredients.forEach { m ->
                            val isChosen = selectedMaster?.id == m.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isChosen) EmeraldPrimary else (if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9)))
                                    .clickable {
                                        selectedMaster = m
                                        name = m.name
                                        priceStr = m.packagePrice.toInt().toString()
                                        sizeStr = m.packageSize.toInt().toString()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = m.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) Color.White else (if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569))
                                )
                            }
                        }
                    }

                    if (selectedMaster != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Terpilih: ${selectedMaster?.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                                Text("Harga Kemasan: ${Utils.formatRupiah(selectedMaster?.packagePrice ?: 0.0)}", fontSize = 11.sp, color = Color.Gray)
                                Text("Isi Kemasan: ${selectedMaster?.packageSize?.toInt()} gr/ml/pcs", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (ingredient != null) {
                    // Edit mode - display linked master name and price read-only with a friendly notice
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Komposisi Menu Terhubung", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldPrimary)
                            Text("Bahan Baku: ${ingredient.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Kemasan: ${Utils.formatRupiah(ingredient.packagePrice)} per ${ingredient.packageSize.toInt()} gr/ml/pcs", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                "Ukuran dan harga di atas adalah global. Jika ingin mengubah harga kemasannya secara massal, ubah di panel 'Daftar Bahan Baku Global' di halaman luar.",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 13.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                } else {
                    // Create New fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Bahan Baku Baru") },
                        placeholder = { Text("Contoh: Susu SKM Frisian") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("ing_name_field")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Harga Per Kemasan (Rp)") },
                        placeholder = { Text("Contoh: 16000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("ing_price_field")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sizeStr,
                        onValueChange = { sizeStr = it },
                        label = { Text("Total Isi Kemasan (gram / ml / pcs)") },
                        placeholder = { Text("Contoh: 375") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("ing_size_field")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Dosage input
                OutlinedTextField(
                    value = usageStr,
                    onValueChange = { usageStr = it },
                    label = { Text("Takaran Digunakan per Gelas (Porsi)") },
                    placeholder = { Text("Contoh: 40") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ing_usage_field")
                )

                // Real-time calculation feedback inside dialog
                val parsedPrice = Utils.parseDouble(priceStr)
                val parsedSize = Utils.parseDouble(sizeStr)
                val parsedUsage = Utils.parseDouble(usageStr)
                val costPerPortionVal = if (parsedSize > 0) (parsedPrice / parsedSize) * parsedUsage else 0.0

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldLight, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Perkiraan Biaya per Porsi:", 
                        fontSize = 11.sp, 
                        color = EmeraldDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        Utils.formatRupiah(costPerPortionVal), 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = EmeraldDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("ing_dialog_dismiss")) {
                        Text("Batal", color = NeutralGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    Utils.parseDouble(priceStr),
                                    Utils.parseDouble(sizeStr),
                                    Utils.parseDouble(usageStr)
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.testTag("ing_dialog_confirm")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun MasterIngredientEditDialog(
    master: MasterIngredient,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(master.name) }
    var priceStr by remember { mutableStateOf(master.packagePrice.toInt().toString()) }
    var sizeStr by remember { mutableStateOf(master.packageSize.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Edit Bahan Baku Utama",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mengubah detail ini akan secara instan memperbarui perhitungan HPP untuk semua menu resep yang terhubung.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Bahan") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Harga Kemasan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = sizeStr,
                    onValueChange = { sizeStr = it },
                    label = { Text("Isi Kemasan (gram / ml / pcs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = NeutralGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    Utils.parseDouble(priceStr),
                                    Utils.parseDouble(sizeStr)
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun OutputFinancialMetricRow(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(Utils.formatRupiah(value), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}


// ==========================================
// TAB 3: BIAYA OPERASIONAL (OpEx)
// ==========================================
@Composable
fun OpExTab(
    opexItems: List<OpexItem>,
    settings: ProductSettings,
    metrics: KulaBoothMetrics,
    onUpdateSettings: (productName: String?, sellingPrice: Double?, isOnline: Boolean?, wastagePercent: Double?, workingDays: Int?, targetDailySales: Int?) -> Unit,
    onAddItem: (String, Double) -> Unit,
    onUpdateItem: (Int, String, Double) -> Unit,
    onDeleteItem: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<OpexItem?>(null) }
    var workingDaysInput by remember { mutableStateOf(settings.workingDays.toString()) }

    LaunchedEffect(settings.workingDays) {
        workingDaysInput = settings.workingDays.toString()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Constant Operational inputs
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Parameter Hari Kerja Efektif",
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = workingDaysInput,
                                onValueChange = {
                                    workingDaysInput = it
                                    onUpdateSettings(null, null, null, null, Utils.parseInt(it), null)
                                },
                                label = { Text("Hari Kerja dalam 1 Bulan") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                                    focusedLabelColor = EmeraldPrimary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f).testTag("working_days_input")
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            InfoTooltip(text = "Jumlah hari lapak Anda aktif menjual minuman dalam 1 bulan (Default: 26 hari).")
                        }
                    }
                }
            }

            // Realtime Outputs Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("opex_summary_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL OPERASIONAL BULANAN", fontSize = 10.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                            Text(Utils.formatRupiah(metrics.totalOpexMonthly), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("BEBAN OPERASIONAL HARIAN", fontSize = 10.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                            val displayOpexHarian = if (settings.workingDays > 0) metrics.totalOpexMonthly / settings.workingDays.toDouble() else 0.0
                            Text(Utils.formatRupiah(displayOpexHarian), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }

            // List Title
            item {
                Text(
                    text = "Daftar Pengeluaran Rutin Bulanan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
            }

            // Expenses items List
            if (opexItems.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        title = "Belum Ada Pengeluaran",
                        subtitle = "Tambahkan sewa tempat, listrik, internet, sampah, gaji tim, dll."
                    )
                }
            } else {
                items(opexItems, key = { it.id }) { item ->
                    OpexItemRow(
                        item = item,
                        onEditClick = { editingItem = item },
                        onDeleteClick = { onDeleteItem(item.id) }
                    )
                }
            }
        }

        // Floating Action Button to add items to the budget
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_opex_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Pengeluaran Bulanan")
        }
    }

    if (showAddDialog) {
        OpexEditDialog(
            item = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cost ->
                onAddItem(name, cost)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        OpexEditDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { name, cost ->
                onUpdateItem(item.id, name, cost)
                editingItem = null
            }
        )
    }
}

@Composable
fun OpexItemRow(
    item: OpexItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().testTag("opex_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Utils.formatRupiah(item.monthlyCost) + " /bulan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EmeraldDark
                )
            }

            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.testTag("edit_opex_${item.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Pengeluaran",
                        tint = TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.testTag("delete_opex_${item.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Pengeluaran",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpexEditDialog(
    item: OpexItem?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var costStr by remember { mutableStateOf(item?.monthlyCost?.toInt()?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (item == null) "Tambah Pengeluaran" else "Edit Pengeluaran",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pengeluaran") },
                    placeholder = { Text("Contoh: Sewa Lapak") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("opex_name_field")
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("Biaya / Anggaran per Bulan (Rp)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("opex_cost_field")
                )

                // Rupiah visual validator
                val parsedCost = Utils.parseDouble(costStr)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldLight, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Output:", fontSize = 11.sp, color = EmeraldDark)
                    Text(Utils.formatRupiah(parsedCost), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("opex_dialog_dismiss")) {
                        Text("Batal", color = NeutralGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    Utils.parseDouble(costStr)
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.testTag("opex_dialog_confirm")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}


// ==========================================
// TAB 4: DASHBOARD BALIK MODAL & TARGET (BEP & ROI Tracker)
// ==========================================
@Composable
fun DashboardTab(
    settings: ProductSettings,
    metrics: KulaBoothMetrics,
    onUpdateSettings: (productName: String?, sellingPrice: Double?, isOnline: Boolean?, wastagePercent: Double?, workingDays: Int?, targetDailySales: Int?) -> Unit
) {
    var dailySalesTargetInput by remember { mutableStateOf(settings.targetDailySales.toString()) }

    LaunchedEffect(settings.targetDailySales) {
        dailySalesTargetInput = settings.targetDailySales.toString()
    }

    // --- State & Calculations for Sensitivity Analysis Simulation ---
    var selectedSimVar by remember { mutableStateOf(0) } // 0 = Harga Jual, 1 = Volume Sales, 2 = Biaya Opex
    var pctSimAdjustment by remember { mutableStateOf(0f) } // -30% to +30%

    val simFactor = 1.0 + (pctSimAdjustment.toDouble() / 100.0)
    val simSellingPrice = if (selectedSimVar == 0) settings.sellingPrice * simFactor else settings.sellingPrice
    val simTargetSales = if (selectedSimVar == 1) settings.targetDailySales.toDouble() * simFactor else settings.targetDailySales.toDouble()
    val simOpexMonthly = if (selectedSimVar == 2) metrics.totalOpexMonthly * simFactor else metrics.totalOpexMonthly

    val effectiveDays = if (settings.workingDays > 0) settings.workingDays else 26
    val simOmset = simTargetSales * simSellingPrice * effectiveDays.toDouble()
    val simHppCost = simTargetSales * metrics.hppWastage * effectiveDays.toDouble()
    val simTotalCost = simHppCost + simOpexMonthly
    val simNetProfit = simOmset - simTotalCost

    val commRate = if (settings.isOnline) 0.20 else 0.0
    val simCommPerGlass = simSellingPrice * commRate
    val simNetRevenuePerGlass = simSellingPrice - simCommPerGlass
    val simGrossProfitPerGlass = simNetRevenuePerGlass - metrics.hppWastage
    val simDailyOpexCost = simOpexMonthly / effectiveDays.toDouble()

    val simBepDaily = if (simGrossProfitPerGlass > 0) simDailyOpexCost / simGrossProfitPerGlass else -1.0
    val simRoiMonths = if (simNetProfit > 0) metrics.totalCapEx / simNetProfit else -1.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Sales Goal Input Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Target Strategi Penjualan",
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dailySalesTargetInput,
                            onValueChange = {
                                dailySalesTargetInput = it
                                onUpdateSettings(null, null, null, null, null, Utils.parseInt(it))
                            },
                            label = { Text("Target Penjualan per Hari (Gelas)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0),
                                focusedLabelColor = EmeraldPrimary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).testTag("daily_target_input")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        InfoTooltip(text = "Berapa banyak gelas produk yang diekspektasikan laku terjual dalam satu hari operasional (Default: 50 gelas).")
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "Metrik Kelayakan Bisnis (Proyeksi Bulanan)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E293B)
            )
        }

        // 1. Total Omset (Penjualan) Bulanan Card
        item {
            DashboardMetricCard(
                title = "TOTAL OMSET (PENJUALAN) BULANAN",
                value = Utils.formatRupiah(metrics.totalOmsetMonthly),
                subtitle = "Target penjualan: ${settings.targetDailySales} gelas/hari x ${Utils.formatRupiah(settings.sellingPrice)} x ${settings.workingDays} hari kerja efektif.",
                colorBrush = Brush.horizontalGradient(listOf(EmeraldPrimary, EmeraldSecondary)),
                icon = Icons.Default.ShoppingCart,
                tag = "omset_card"
            )
        }

        // 2. Total Biaya Bulanan Card
        item {
            DashboardMetricCard(
                title = "TOTAL BIAYA OPERASIONAL & HPP BULANAN",
                value = Utils.formatRupiah(metrics.totalCostMonthly),
                subtitle = "Meliputi bahan habis pakai (HPP + wastage Rp ${metrics.hppWastage.toInt()} per gelas) serta total modal operasional bulanan (${Utils.formatRupiah(metrics.totalOpexMonthly)}).",
                colorBrush = Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFF97316))),
                icon = Icons.Default.TrendingDown,
                tag = "cost_card"
            )
        }

        // 3. Proyeksi Keuntungan Bersih Bulanan Card
        item {
            val isProfitNegative = metrics.netProfitMonthly <= 0
            val profitColor = if (!isProfitNegative) SuccessGreen else ErrorRed
            val profitBrush = if (!isProfitNegative) {
                Brush.horizontalGradient(listOf(Color(0xFF047857), Color(0xFF059669)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFB91C1C), Color(0xFFEF4444)))
            }
            DashboardMetricCard(
                title = "PROYEKSI KEUNTUNGAN BERSIH BULANAN",
                value = Utils.formatRupiah(metrics.netProfitMonthly),
                subtitle = if (!isProfitNegative) {
                    "Bisnis menghasilkan laba bersih positif! Diperoleh dari selisih Omset dikurangi seluruh beban modal."
                } else {
                    "Peringatan! Bisnis mengalami kerugian operasional. Pertimbangkan menaikkan harga jual, efisiensi bahan, atau menaikkan target harian."
                },
                colorBrush = profitBrush,
                icon = Icons.Default.MonetizationOn,
                tag = "profit_card"
            )
        }

        // 4. Break-Even Point (BEP) Harian
        item {
            val roundedBep = if (metrics.bepDaily > 0) ceil(metrics.bepDaily).toInt() else 0
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth().testTag("bep_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSystemInDarkTheme()) Color(0xFF115E59) else TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = if (isSystemInDarkTheme()) EmeraldAccent else TealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "BREAK-EVEN POINT (BEP) HARIAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (metrics.bepDaily > 0) "$roundedBep Gelas / Hari" else "Tidak akan pernah impas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (metrics.bepDaily > 0) (if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)) else ErrorRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (metrics.bepDaily > 0) {
                                "Anda wajib menjual minimal $roundedBep gelas/hari hanya untuk menutup biaya operasional harian (${Utils.formatRupiah(metrics.dailyOpexCost)})."
                            } else {
                                "Profit kotor per gelas bernilai nol atau minus. Harap naikan harga jual atau hemat bahan baku agar bisnis layak."
                            },
                            fontSize = 12.sp,
                            color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }
                }
            }
        }

        // 5. Estimasi Balik Modal (ROI)
        item {
            val isNetProfitPositive = metrics.netProfitMonthly > 0
            val formattedRoi = if (isNetProfitPositive && metrics.roiMonths >= 0) {
                String.format("%.1f Bulan", metrics.roiMonths)
            } else {
                "Tidak Terhingga"
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardStroke(
                    1.dp, 
                    if (isNetProfitPositive) (if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0)) else Color(0xFFFECACA)
                ),
                modifier = Modifier.fillMaxWidth().testTag("roi_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isNetProfitPositive) (if (isSystemInDarkTheme()) Color(0xFF064E3B) else EmeraldLight) else Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentReturn,
                            contentDescription = null,
                            tint = if (isNetProfitPositive) EmeraldPrimary else ErrorRed
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "ESTIMASI BALIK MODAL (ROI / PAYBACK)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedRoi,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isNetProfitPositive) EmeraldPrimary else ErrorRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isNetProfitPositive) {
                                "Kembalian seluruh investasi modal awal (${Utils.formatRupiah(metrics.totalCapEx)}) dalam waktu $formattedRoi berdasarkan laba bersih bulanan."
                            } else {
                                "Tidak akan pernah balik modal, naikkan harga jual atau target harian karena keuntungan masih minus atau nol."
                            },
                            fontSize = 12.sp,
                            color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)
                        )
                    }
                }
            }
        }

        // --- SUB-FITUR: ANALISIS SENSITIVITAS REAL-TIME ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("sensitivity_analysis_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Poll,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analisis Sensitivitas Bisnis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Simulasikan fluktuasi variabel terpilih (-30% s.d +30%) terhadap metrik kelayakan bisnis.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "1. Pilih Variabel Input Kunci:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) EmeraldAccent else Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val pillVars = listOf(
                            Triple("Harga Jual", Icons.Default.AttachMoney, 0),
                            Triple("Volume Harian", Icons.AutoMirrored.Filled.TrendingUp, 1),
                            Triple("Biaya Opex", Icons.Default.AccountBalanceWallet, 2)
                        )
                        pillVars.forEach { (label, icon, idx) ->
                            val active = selectedSimVar == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) (if (isSystemInDarkTheme()) DarkBg else EmeraldLight) else (if (isSystemInDarkTheme()) Color(0xFF022C22) else Color(0xFFF1F5F9)))
                                    .clickable {
                                        selectedSimVar = idx
                                        pctSimAdjustment = 0f // Reset on change variable
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (active) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) (if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Atur Fluktuasi Persentase:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) EmeraldAccent else Color(0xFF475569)
                        )

                        val sign = if (pctSimAdjustment > 0) "+" else ""
                        Text(
                            text = "$sign${pctSimAdjustment.toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (pctSimAdjustment > 0) SuccessGreen else if (pctSimAdjustment < 0) ErrorRed else (if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)),
                            modifier = Modifier
                                .background(
                                    if (pctSimAdjustment > 0) (if (isSystemInDarkTheme()) DarkBg else EmeraldLight) else if (pctSimAdjustment < 0) Color(0xFFFEE2E2) else (if (isSystemInDarkTheme()) Color(0xFF022C22) else Color(0xFFF1F5F9)),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Slider(
                        value = pctSimAdjustment,
                        onValueChange = { pctSimAdjustment = it },
                        valueRange = -30f..30f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sensitivity_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(-15, 0, 15).forEach { num ->
                            Button(
                                onClick = { pctSimAdjustment = num.toFloat() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                            ) {
                                Text(
                                    text = if (num == 0) "Normal (0%)" else "${if (num > 0) "+" else ""}$num%",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "3. Tabel Perbandingan Real-time:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("Metrik Utama", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Text("Saat Ini", modifier = Modifier.weight(1.2f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), textAlign = TextAlign.End)
                        Text("Simulasi", modifier = Modifier.weight(1.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), textAlign = TextAlign.End)
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Row: Net Profit
                    val profitDiff = simNetProfit - metrics.netProfitMonthly
                    SimilarityRow(
                        label = "Laba Bersih Bulanan",
                        currentVal = Utils.formatRupiah(metrics.netProfitMonthly),
                        simulatedVal = Utils.formatRupiah(simNetProfit),
                        diffVal = if (profitDiff == 0.0) "=" else "${if (profitDiff > 0) "+" else ""}${Utils.formatRupiah(profitDiff)}",
                        isPositive = profitDiff >= 0
                    )

                    // Row: BEP Daily
                    val currentBep = if (metrics.bepDaily > 0) ceil(metrics.bepDaily).toInt() else 0
                    val simulatedBep = if (simBepDaily > 0) ceil(simBepDaily).toInt() else 0
                    val bepDiff = simulatedBep - currentBep
                    SimilarityRow(
                        label = "BEP Harian",
                        currentVal = if (currentBep > 0) "$currentBep Gelas" else "Tidak Impas",
                        simulatedVal = if (simulatedBep > 0) "$simulatedBep Gelas" else "Tidak Impas",
                        diffVal = if (currentBep <= 0 || simulatedBep <= 0) "N/A" else if (bepDiff == 0) "=" else "${if (bepDiff > 0) "+" else ""}$bepDiff Gelas",
                        isPositive = bepDiff <= 0
                    )

                    // Row: ROI Months
                    val isCurrentRoiValid = metrics.netProfitMonthly > 0 && metrics.roiMonths >= 0
                    val isSimRoiValid = simNetProfit > 0 && simRoiMonths >= 0
                    val roiDiff = if (isCurrentRoiValid && isSimRoiValid) simRoiMonths - metrics.roiMonths else 0.0
                    SimilarityRow(
                        label = "Balik Modal (ROI)",
                        currentVal = if (isCurrentRoiValid) String.format("%.1f Bln", metrics.roiMonths) else "Terhambat",
                        simulatedVal = if (isSimRoiValid) String.format("%.1f Bln", simRoiMonths) else "Terhambat",
                        diffVal = if (isCurrentRoiValid && isSimRoiValid) {
                            if (roiDiff == 0.0) "=" else String.format("%s%.1f Bln", if (roiDiff > 0) "+" else "", roiDiff)
                        } else "N/A",
                        isPositive = roiDiff <= 0
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Grafik Komparasi Laba Bersih:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val maxVal = maxOf(metrics.netProfitMonthly, simNetProfit, 100000.0)

                        Text("Kondisi Sekarang: ${Utils.formatRupiah(metrics.netProfitMonthly)}", fontSize = 10.sp, color = Color(0xFF475569))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            val currentPct = if (metrics.netProfitMonthly > 0) (metrics.netProfitMonthly / maxVal).toFloat() else 0.01f
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(currentPct.coerceIn(0.01f, 1f))
                                    .background(if (metrics.netProfitMonthly > 0) EmeraldPrimary else ErrorRed)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Hasil Simulasi: ${Utils.formatRupiah(simNetProfit)}", fontSize = 10.sp, color = Color(0xFF475569))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            val simPct = if (simNetProfit > 0) (simNetProfit / maxVal).toFloat() else 0.01f
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(simPct.coerceIn(0.01f, 1f))
                                    .background(if (simNetProfit > 0) SuccessGreen else ErrorRed)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimilarityRow(
    label: String,
    currentVal: String,
    simulatedVal: String,
    diffVal: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
        )
        Text(
            text = currentVal,
            modifier = Modifier.weight(1.2f),
            fontSize = 12.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.End
        )
        Column(
            modifier = Modifier.weight(1.4f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = simulatedVal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (diffVal == "=") Color(0xFF0F172A) else if (isPositive) SuccessGreen else ErrorRed,
                textAlign = TextAlign.End
            )
            if (diffVal != "=") {
                Text(
                    text = diffVal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) SuccessGreen else ErrorRed,
                    textAlign = TextAlign.End
                )
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9))
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    colorBrush: Brush,
    icon: ImageVector,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorBrush)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.82f),
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}


// ==========================================
// REUSABLE PRESENTATION UTILITIES Composable
// ==========================================

@Composable
fun EmptyStatePlaceholder(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF115E59) else Color(0xFFF1F5F9)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = NeutralGray.copy(alpha = 0.6f),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InfoTooltip(text: String) {
    var showExplanation by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { showExplanation = !showExplanation },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Help,
                contentDescription = "Bantuan Info",
                tint = EmeraldSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (showExplanation) {
            Dialog(onDismissRequest = { showExplanation = false }) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Informasi Bantuan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = text,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showExplanation = false },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Mengerti")
                        }
                    }
                }
            }
        }
    }
}

// Visual layout helper for card border styling
fun CardStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportTab(
    allSales: List<Sale>,
    allProducts: List<ProductSettings>,
    allMasterIngredients: List<MasterIngredient>,
    apiConfig: com.example.data.ApiConfig,
    syncStatus: String?,
    onAddSale: (Int, Int, Boolean) -> Unit,
    onDeleteSale: (Int) -> Unit,
    onClearAllSales: () -> Unit,
    onUpdateConfig: (String, String, Boolean) -> Unit,
    onSyncSales: () -> Unit,
    onClearSyncStatus: () -> Unit,
    onRestockIngredient: (Int, Double) -> Unit,
    onUpdateThreshold: (Int, Double) -> Unit
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0 = Transaksi, 1 = Stok & Sync

    var selectedPeriodTab by remember { mutableStateOf(0) } // 0 = Harian, 1 = Mingguan, 2 = Bulanan
    var showAddSaleDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    // Dialogs states for restock and limit
    var restockIngredientId by remember { mutableStateOf<Int?>(null) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var restockInputAmount by remember { mutableStateOf("") }

    var limitIngredientId by remember { mutableStateOf<Int?>(null) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var limitInputAmount by remember { mutableStateOf("") }

    var baseUrlState by remember(apiConfig.baseUrl) { mutableStateOf(apiConfig.baseUrl) }
    var apiKeyState by remember(apiConfig.apiKey) { mutableStateOf(apiConfig.apiKey) }
    var autoSyncState by remember(apiConfig.autoSync) { mutableStateOf(apiConfig.autoSync) }

    val filteredSales = remember(allSales, selectedPeriodTab) {
        val now = System.currentTimeMillis()
        val dayMills = 24 * 3600 * 1000L
        when (selectedPeriodTab) {
            0 -> allSales.filter { it.timestamp >= now - dayMills }
            1 -> allSales.filter { it.timestamp >= now - 7 * dayMills }
            2 -> allSales.filter { it.timestamp >= now - 30 * dayMills }
            else -> allSales
        }
    }

    val totalQtySold = filteredSales.sumOf { it.quantity }
    val grossOmset = filteredSales.sumOf { it.quantity * it.sellingPrice }
    val totalCommission = filteredSales.sumOf { 
        if (it.isOnline) it.quantity * it.sellingPrice * 0.20 else 0.0 
    }
    val netOmset = grossOmset - totalCommission
    val totalHppVal = filteredSales.sumOf { it.quantity * it.hppPerUnit }
    val grossProfit = netOmset - totalHppVal

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row Selector
        TabRow(
            selectedTabIndex = selectedTabState,
            containerColor = if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color.White,
            contentColor = EmeraldPrimary
        ) {
            Tab(
                selected = selectedTabState == 0,
                onClick = { selectedTabState = 0 },
                text = { Text("Transaksi & Omset", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTabState == 1,
                onClick = { selectedTabState = 1 },
                text = { Text("Stok & Integrasi Web", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTabState == 0) {
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Period selector (Harian, Mingguan, Bulanan)
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val periods = listOf("Harian (24j)", "Mingguan (7h)", "Bulanan (30h)")
                                    periods.forEachIndexed { idx, label ->
                                        val isSelected = selectedPeriodTab == idx
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                                .clickable { selectedPeriodTab = idx }
                                                .padding(vertical = 8.dp)
                                                .testTag("period_tab_$idx")
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else (if (isSystemInDarkTheme()) Color.LightGray else Color(0xFF475569))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Summary cards
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Row 1: Total Omset & Unit Terjual
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) DarkSurface else EmeraldLight),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("TOTAL OMSET (BERSIH)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSystemInDarkTheme()) EmeraldAccent else EmeraldDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(Utils.formatRupiah(netOmset), fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isSystemInDarkTheme()) Color.White else EmeraldDark)
                                        if (totalCommission > 0) {
                                            Text("Setelah komisi: ${Utils.formatRupiahShort(totalCommission)}", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("UNIT TERJUAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeutralGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$totalQtySold Porsi", fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B))
                                    }
                                }
                            }

                            // Row 2: Total HPP & Keuntungan Kotor (Keuntungan kotor is highly optimized/highlighted)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF2D1E15) else Color(0xFFFEF3C7)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("TOTAL HPP RESEP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSystemInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFB45309))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(Utils.formatRupiah(totalHppVal), fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isSystemInDarkTheme()) Color.White else Color(0xFFB45309))
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF0F2E1E) else Color(0xFFDCFCE7)),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("KEUNTUNGAN KOTOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(Utils.formatRupiah(grossProfit), fontSize = 16.sp, fontWeight = FontWeight.Black, color = EmeraldPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // Simple Visualization: Bar Chart
                    item {
                        SimpleBarChart(omset = netOmset, hpp = totalHppVal)
                    }

                    // Transactions Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Riwayat Penjualan (${filteredSales.size} Transaksi)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B)
                            )
                            
                            if (allSales.isNotEmpty()) {
                                TextButton(
                                    onClick = { showClearConfirm = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed),
                                    modifier = Modifier.testTag("clear_sales_button")
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = "Hapus data", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Ledger List
                    if (filteredSales.isEmpty()) {
                        item {
                            EmptyStatePlaceholder(
                                title = "Belum Ada Penjualan",
                                subtitle = "Belum ada transaksi tercatat dalam periode ini. Klik tombol tambah berwarna hijau di pojok kanan bawah untuk mencatat penjualan baru!"
                            )
                        }
                    } else {
                        items(filteredSales, key = { it.id }) { sale ->
                            SaleItemRow(
                                sale = sale,
                                onDeleteClick = { onDeleteSale(sale.id) }
                            )
                        }
                    }
                }

                // Floating Action Button
                FloatingActionButton(
                    onClick = { showAddSaleDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .testTag("add_sale_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Penjualan")
                }
            }
        } else {
            // Inventory & Web Sync Panel
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Next.js API configuration section
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) DarkSurface else Color.White),
                        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Sinkronisasi POS Kasir Arum Seduh",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Hubungkan aplikasi ini ke panel admin Next.js Anda untuk otomatisasi penghitungan ketersediaan bahan, pengelolaan stok, dan penjualan.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Base URL input
                            OutlinedTextField(
                                value = baseUrlState,
                                onValueChange = { baseUrlState = it },
                                label = { Text("URL Web Admin (API Base URL)") },
                                placeholder = { Text("contoh: https://arumseduh.com/api/pos") },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary),
                                modifier = Modifier.fillMaxWidth().testTag("api_url_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // API Key input
                            OutlinedTextField(
                                value = apiKeyState,
                                onValueChange = { apiKeyState = it },
                                label = { Text("Kunci Proteksi (API Auth Token)") },
                                placeholder = { Text("Masukkan API key rahasia") },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary),
                                modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Auto Sync Trigger checkbox
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { autoSyncState = !autoSyncState }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = autoSyncState,
                                    onCheckedChange = { autoSyncState = it },
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text("Sinkronisasi Otomatis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Kirim penjualan otomatis ke cloud Next.js setelah dicatat", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action buttons: Save & Sync
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onUpdateConfig(baseUrlState, apiKeyState, autoSyncState) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary),
                                    modifier = Modifier.weight(1f).testTag("save_config_button")
                                ) {
                                    Text("Simpan Setelan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onSyncSales() },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier.weight(1f).testTag("sync_web_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync ke Web", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Sync Status Banner
                            if (syncStatus != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (syncStatus.contains("Gagal") || syncStatus.contains("Peringatan")) Color(0xFFFEF2F2) else Color(0xFFECFDF5)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (syncStatus.contains("Menghubungkan")) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp).weight(0.1f, false), strokeWidth = 2.dp, color = EmeraldPrimary)
                                        } else {
                                            Icon(
                                                imageVector = if (syncStatus.contains("Berhasil") || syncStatus.contains("Sync Berhasil") || syncStatus.contains("Selesai")) Icons.Default.Check else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (syncStatus.contains("Gagal") || syncStatus.contains("Peringatan")) Color.Red else EmeraldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = syncStatus,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (syncStatus.contains("Gagal") || syncStatus.contains("Peringatan")) Color(0xFF991B1B) else Color(0xFF065F46),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = onClearSyncStatus,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("✕", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Inventory Stocks Monitor Header
                item {
                    Text(
                        "Ketersediaan Stok Bahan Baku",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B)
                    )
                }

                if (allMasterIngredients.isEmpty()) {
                    item {
                        EmptyStatePlaceholder(
                            title = "Belum Ada Bahan Baku",
                            subtitle = "Silakan daftarkan bahan baku resep Anda di tab HPP untuk memulai pengawasan stok."
                        )
                    }
                } else {
                    items(allMasterIngredients) { master ->
                        val isWarning = master.currentStock <= master.minimumStock
                        // Assume standard target scale is packageSize * 5 (e.g., 5 packs threshold)
                        val percent = if (master.packageSize > 0) {
                            (master.currentStock / (master.packageSize * 5.0)).coerceIn(0.0, 1.0)
                        } else 1.0

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) DarkSurface else Color.White),
                            border = BorderStroke(
                                1.dp,
                                if (isWarning) Color.Red.copy(alpha = 0.5f) else (if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("master_stock_card_${master.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            master.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "ID Bahan #${master.id} | Ukuran Kemasan: ${Utils.formatDouble(master.packageSize)}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    // Warning Badges
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (master.currentStock == 0.0) Color(0xFFFEF2F2)
                                                else if (isWarning) Color(0xFFFFFBEB)
                                                else Color(0xFFF0FDF4)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (master.currentStock == 0.0) "HABIS"
                                                   else if (isWarning) "STOK MENIPIS"
                                                   else "STOK AMAN",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (master.currentStock == 0.0) Color.Red
                                                   else if (isWarning) Color(0xFFD97706)
                                                   else EmeraldPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Visual progress tracking
                                LinearProgressIndicator(
                                    progress = { percent.toFloat() },
                                    color = if (master.currentStock == 0.0) Color.Red
                                            else if (isWarning) Color(0xFFF59E0B)
                                            else EmeraldPrimary,
                                    trackColor = if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Tersedia: ${Utils.formatDouble(master.currentStock)} Satuan",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWarning) Color.Red else (if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B))
                                        )
                                        Text(
                                            text = "Limit Warning: ${Utils.formatDouble(master.minimumStock)} Satuan",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    // Action buttons for local stock tracking
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                limitIngredientId = master.id
                                                limitInputAmount = master.minimumStock.toString()
                                                showLimitDialog = true
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Set Limit", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Button(
                                            onClick = {
                                                restockIngredientId = master.id
                                                restockInputAmount = "1000"
                                                showRestockDialog = true
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            modifier = Modifier.height(28.dp).testTag("restock_btn_${master.id}")
                                        ) {
                                            Text("+ Isi Stok", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogs
    if (showAddSaleDialog) {
        AddSaleDialog(
            allProducts = allProducts,
            onDismiss = { showAddSaleDialog = false },
            onConfirm = { prodId, qty, isOnline ->
                onAddSale(prodId, qty, isOnline)
                showAddSaleDialog = false
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Reset Semua Data Penjualan?", fontWeight = FontWeight.Bold) },
            text = { Text("Tindakan ini akan menghapus seluruh rekaman riwayat transaksi penjualan secara permanen. Apakah Anda yakin?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllSales()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Restock & Limit Dialog templates
    if (showRestockDialog && restockIngredientId != null) {
        val targetIngredient = allMasterIngredients.find { it.id == restockIngredientId }
        AlertDialog(
            onDismissRequest = { showRestockDialog = false },
            title = { Text("Isi Ulang Stok Bahan Baku", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Bahan Baku: ${targetIngredient?.name ?: ""}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Masukkan jumlah gram/ml/kemasan untuk ditambahkan ke ketersediaan saat ini:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = restockInputAmount,
                        onValueChange = { restockInputAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("restock_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val added = restockInputAmount.toDoubleOrNull() ?: 0.0
                        if (added > 0.0) {
                            onRestockIngredient(restockIngredientId!!, added)
                        }
                        showRestockDialog = false
                        restockInputAmount = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Isi Stok", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestockDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showLimitDialog && limitIngredientId != null) {
        val targetIngredient = allMasterIngredients.find { it.id == limitIngredientId }
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text("Atur Batas Minimum Warning", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Bahan Baku: ${targetIngredient?.name ?: ""}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Aplikasi akan memicu status 'STOK MENIPIS' jika persediaan berada di bawah limit ini:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = limitInputAmount,
                        onValueChange = { limitInputAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("limit_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitInputAmount.toDoubleOrNull() ?: 0.0
                        if (limit >= 0.0) {
                            onUpdateThreshold(limitIngredientId!!, limit)
                        }
                        showLimitDialog = false
                        limitInputAmount = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan Limit", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SimpleBarChart(omset: Double, hpp: Double) {
    val maxVal = maxOf(omset, hpp, 1000.0)
    val omsetRatio = (omset / maxVal).toFloat()
    val hppRatio = (hpp / maxVal).toFloat()
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) DarkSurface else Color.White),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Grafik Sederhana: Omset Bersih vs HPP Bahan",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Omset
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(fraction = omsetRatio.coerceIn(0.06f, 1f))
                            .width(44.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(EmeraldPrimary, EmeraldSecondary)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Omset", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSystemInDarkTheme()) Color.White else Color(0xFF475569))
                    Text(Utils.formatRupiahShort(omset), fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                }
                
                // HPP
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(fraction = hppRatio.coerceIn(0.06f, 1f))
                            .width(44.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("HPP Resep", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSystemInDarkTheme()) Color.White else Color(0xFF475569))
                    Text(Utils.formatRupiahShort(hpp), fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SaleItemRow(
    sale: Sale,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) DarkSurface else Color.White),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth().testTag("sale_item_${sale.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sale.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Channel Badge (Online vs Offline)
                    val badgeColor = if (sale.isOnline) Color(0xFFEFF6FF) else Color(0xFFF0FDF4)
                    val badgeText = if (sale.isOnline) "OL" else "Direct"
                    val badgeTextColor = if (sale.isOnline) Color(0xFF2563EB) else Color(0xFF16A34A)
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSystemInDarkTheme()) Color(0xFF1E293B) else badgeColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color.LightGray else badgeTextColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatTimestamp(sale.timestamp),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${sale.quantity} Porsi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                val totalPortionHpp = sale.quantity * sale.hppPerUnit
                val brutoTotal = sale.quantity * sale.sellingPrice
                val comm = if (sale.isOnline) brutoTotal * 0.20 else 0.0
                val netTotal = brutoTotal - comm
                val profit = netTotal - totalPortionHpp

                Text(
                    text = "Omset Bersih: ${Utils.formatRupiah(netTotal)} | HPP: ${Utils.formatRupiahShort(totalPortionHpp)}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Laba Kotor: ${Utils.formatRupiah(profit)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp).testTag("delete_sale_${sale.id}")
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus Penjualan",
                    tint = ErrorRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleDialog(
    allProducts: List<ProductSettings>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Boolean) -> Unit
) {
    var selectedProductIdx by remember { mutableStateOf(0) }
    var quantityStr by remember { mutableStateOf("1") }
    var isOnline by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Catat Transaksi Penjualan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (allProducts.isEmpty()) {
                    Text(
                        "Belum ada menu produk aktif. Silakan tambahkan menu terlebih dahulu di Tab HPP.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Oke")
                    }
                } else {
                    val currentProduct = allProducts.getOrNull(selectedProductIdx) ?: allProducts.first()

                    // Product Dropdown Selection
                    Text("Pilih Produk", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeutralGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentProduct.productName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            allProducts.forEachIndexed { index, product ->
                                DropdownMenuItem(
                                    text = { Text(product.productName, fontSize = 12.sp) },
                                    onClick = {
                                        selectedProductIdx = index
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quantity Input
                    Text("Jumlah Porsi Terjual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeutralGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_sale_qty_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sale channel selection (Direct vs Online Delivery)
                    Text("Saluran Penjualan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeutralGray)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                            .padding(4.dp)
                    ) {
                        // Direct / Offline
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isOnline) EmeraldPrimary else Color.Transparent)
                                .clickable { isOnline = false }
                                .padding(vertical = 8.dp)
                                .testTag("channel_offline")
                        ) {
                            Text(
                                "Makan di tempat/Direct",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isOnline) Color.White else Color.Gray
                            )
                        }

                        // Online Delivery
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isOnline) EmeraldPrimary else Color.Transparent)
                                .clickable { isOnline = true }
                                .padding(vertical = 8.dp)
                                .testTag("channel_online")
                        ) {
                            Text(
                                "Online Delivery (Komisi 20%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color.White else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Batal", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val qtyInt = Utils.parseInt(quantityStr).coerceAtLeast(1)
                                onConfirm(currentProduct.id, qtyInt, isOnline)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.testTag("add_sale_confirm_button")
                        ) {
                            Text("Simpan Transaksi", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        timestamp.toString()
    }
}
