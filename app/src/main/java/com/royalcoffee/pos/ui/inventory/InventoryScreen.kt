package com.royalcoffee.pos.ui.inventory

import com.royalcoffee.pos.model.Money
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.royalcoffee.pos.data.local.entity.CategoryEntity
import com.royalcoffee.pos.data.local.entity.InventoryTransactionType
import com.royalcoffee.pos.data.local.repository.CategoryRepository
import com.royalcoffee.pos.data.local.repository.ProductRepository
import com.royalcoffee.pos.model.InventoryProduct
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.notifications.NamedBatch
import com.royalcoffee.pos.ui.common.ExpiryDatePickerField
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold
import java.util.Locale
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.ExposedDropdownMenuAnchorType

/**
 * Main Inventory screen.
 *
 * Admin users can:
 * - Add and edit products
 * - Manage categories
 * - Adjust stock
 * - Restock products
 * - Record waste
 * - Deactivate products
 */
@Composable
fun InventoryScreen(
    userRole: UserRole,
    productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    modifier: Modifier = Modifier
) {
    // Create the ViewModel with both repositories.
    val viewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModelFactory(
            productRepository = productRepository,
            categoryRepository = categoryRepository,
            userRole = userRole
        )
    )

    // Observe inventory data.
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val filterCategories by viewModel.filterCategories.collectAsStateWithLifecycle()
    val productCategories by viewModel.productCategories.collectAsStateWithLifecycle()
    val categoryEntities by viewModel.categoryEntities.collectAsStateWithLifecycle()

    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val stockFilter by viewModel.stockFilter.collectAsStateWithLifecycle()

    val totalProducts by viewModel.totalProducts.collectAsStateWithLifecycle()
    val totalStock by viewModel.totalStock.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val outOfStockCount by viewModel.outOfStockCount.collectAsStateWithLifecycle()

    val inventoryError = viewModel.inventoryError

    // Dialog states.
    var showAddDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<InventoryProduct?>(null) }

    var batchEditorProduct by remember { mutableStateOf<InventoryProduct?>(null) }
    val batchEditorProductId = batchEditorProduct?.productId
    val batchFlow = remember(batchEditorProductId) {
        batchEditorProductId?.let(productRepository::observeBatchesForProduct)
            ?: flowOf(emptyList())
    }
    val batchesForEditing by batchFlow.collectAsStateWithLifecycle(emptyList())
    val screenScope = rememberCoroutineScope()

    var adjustingProduct by remember { mutableStateOf<InventoryProduct?>(null) }
    var adjustmentType by remember { mutableStateOf(InventoryTransactionType.RESTOCK) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight

            val horizontalPadding = when {
                availableWidth < 700.dp -> 12.dp
                availableWidth < 1000.dp -> 18.dp
                else -> 28.dp
            }
            val verticalPadding = if (availableHeight < 600.dp) 12.dp else 24.dp
            val headerSpacing = if (availableHeight < 600.dp) 14.dp else 24.dp
            val sectionSpacing = if (availableHeight < 600.dp) 14.dp else 24.dp

            val isCompactHeader = availableWidth < 700.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ---------------------------------------------------------
                // HEADER
                // ---------------------------------------------------------
                if (isCompactHeader) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Inventory",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        if (userRole == UserRole.ADMIN) {
                            AdminActionButtons(
                                onManageCategories = { showCategoryDialog = true },
                                onAddProduct = { showAddDialog = true },
                                isCompact = true
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inventory",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (userRole == UserRole.ADMIN) {
                            AdminActionButtons(
                                onManageCategories = { showCategoryDialog = true },
                                onAddProduct = { showAddDialog = true },
                                isCompact = false
                            )
                        }
                    }
                }

                Text(
                    text = "Manage products, stock levels, categories, and audit history.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(headerSpacing))

                Column(
                    modifier = Modifier
                        .widthIn(max = 1100.dp)
                        .weight(1f)
                ) {
                    // ---------------------------------------------------------
                    // INVENTORY SUMMARY
                    // ---------------------------------------------------------
                    InventorySummaryCards(
                        totalProducts = totalProducts,
                        totalStock = totalStock,
                        lowStockCount = lowStockCount,
                        outOfStockCount = outOfStockCount,
                        stockFilter = stockFilter,
                        onUpdateStockFilter = { viewModel.updateStockFilter(it) },
                        availableWidth = availableWidth
                    )

                    Spacer(modifier = Modifier.height(sectionSpacing))

                    // ---------------------------------------------------------
                    // SEARCH AND CATEGORY FILTERS
                    // ---------------------------------------------------------
                    SearchAndCategoryFilters(
                        searchText = searchText,
                        onSearchChange = { viewModel.updateSearch(it) },
                        selectedCategory = selectedCategory,
                        filterCategories = filterCategories,
                        onCategorySelect = { viewModel.updateCategory(it) },
                        availableWidth = availableWidth
                    )

                    Spacer(modifier = Modifier.height(sectionSpacing))

                    // ---------------------------------------------------------
                    // PRODUCT LIST
                    // ---------------------------------------------------------
                    InventoryList(
                        products = products,
                        userRole = userRole,
                        availableWidth = availableWidth,
                        onEdit = { editingProduct = it },
                        onAdjust = { product, type ->
                            adjustingProduct = product
                            adjustmentType = type
                        },
                        onReactivate = { viewModel.reactivateProduct(it.productId) },
                        onDeactivate = { viewModel.requestRemoval(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------
    // REMOVAL CONFIRMATION & LIFECYCLE MESSAGES
    // ---------------------------------------------------------
    viewModel.removalCandidate?.let { product ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelRemoval() },
            title = { Text(if (viewModel.removalPermanent) "Permanently delete product?" else "Deactivate product?") },
            text = {
                Text(
                    if (viewModel.removalPermanent)
                        "Delete ${product.name}? This cannot be undone. If related history is added before confirmation, it will be deactivated instead."
                    else
                        "${product.name} has stock or related history, or legacy history cannot be ruled out. It will remain in inventory and can be reactivated."
                )
            },
            confirmButton = { TextButton(onClick = { viewModel.confirmRemoval() }) { Text(if (viewModel.removalPermanent) "Delete permanently" else "Deactivate") } },
            dismissButton = { TextButton(onClick = { viewModel.cancelRemoval() }) { Text("Cancel") } }
        )
    }

    viewModel.lifecycleMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearLifecycleMessage() },
            title = { Text("Product updated") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { viewModel.clearLifecycleMessage() }) { Text("OK") } }
        )
    }

    if (inventoryError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Inventory Error") },
            text = { Text(inventoryError) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) { Text("OK") }
            }
        )
    }

    // ---------------------------------------------------------
    // ADD PRODUCT DIALOG
    // ---------------------------------------------------------
    if (showAddDialog) {
        ProductFormDialog(
            userRole = userRole,
            categories = productCategories,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, price, cost, stock, reorder, expiryDay ->
                viewModel.addProduct(
                    name = name,
                    category = category,
                    price = price,
                    cost = cost,
                    stock = stock,
                    reorder = reorder,
                    expiryDay = expiryDay
                )
                showAddDialog = false
            }
        )
    }

    // ---------------------------------------------------------
    // EDIT PRODUCT DIALOG
    // ---------------------------------------------------------
    if (editingProduct != null) {
        ProductFormDialog(
            userRole = userRole,
            product = editingProduct,
            categories = productCategories,
            onDismiss = { editingProduct = null },
            onConfirm = { name, category, price, cost, _, reorder, _ ->
                viewModel.updateProduct(
                    productId = editingProduct!!.productId,
                    name = name,
                    category = category,
                    price = price,
                    cost = cost,
                    reorder = reorder,
                    active = editingProduct!!.isActive
                )
                editingProduct = null
            },
            onManageBatches = {
                batchEditorProduct = editingProduct
                editingProduct = null
            }
        )
    }

    batchEditorProduct?.let { product ->
        StockBatchManagementDialog(
            product = product,
            batches = batchesForEditing,
            onDismiss = { batchEditorProduct = null },
            onUpdateExpiry = { batchId, expiryDay ->
                screenScope.launch {
                    productRepository.updateBatchExpiry(
                        batchId = batchId, expiryDay = expiryDay
                    )
                }
            },
            onAddBatch = { quantity, expiryDay ->
                viewModel.adjustStock(
                    productId = product.productId,
                    change = quantity,
                    type = InventoryTransactionType.RESTOCK,
                    reason = "Stock batch added from product editor",
                    expiryDay = expiryDay
                )
            }
        )
    }

    // ---------------------------------------------------------
    // STOCK ADJUSTMENT DIALOG
    // ---------------------------------------------------------
    if (adjustingProduct != null) {
        StockActionDialog(
            product = adjustingProduct!!,
            type = adjustmentType,
            onDismiss = { adjustingProduct = null },
            onConfirm = { quantity, reason, expiryDay ->
                val change = if (adjustmentType == InventoryTransactionType.RESTOCK) {
                    quantity
                } else {
                    -quantity
                }
                viewModel.adjustStock(
                    productId = adjustingProduct!!.productId,
                    change = change,
                    type = adjustmentType,
                    reason = reason,
                    expiryDay = expiryDay
                )
                adjustingProduct = null
            }
        )
    }

    // ---------------------------------------------------------
    // CATEGORY MANAGEMENT DIALOG
    // ---------------------------------------------------------
    if (showCategoryDialog) {
        ManageCategoriesDialog(
            categories = categoryEntities,
            errorMessage = viewModel.categoryError,
            onAddCategory = { categoryName -> viewModel.addCategory(categoryName) },
            onRemoveCategory = { category -> viewModel.removeCategory(category) },
            onClearError = { viewModel.clearCategoryError() },
            onDismiss = { showCategoryDialog = false }
        )
    }
}

/**
 * Admin action buttons (Manage Categories & Add Product).
 */
@Composable
private fun AdminActionButtons(
    onManageCategories: () -> Unit,
    onAddProduct: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    if (isCompact) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onManageCategories,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Categories", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(
                onClick = onAddProduct,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add Product", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onManageCategories,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Category, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Manage Categories")
            }
            Button(
                onClick = onAddProduct,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Add Product")
            }
        }
    }
}

/**
 * Summary cards container. Switches between 1 row and 2x2 grid based on available width.
 */
@Composable
private fun InventorySummaryCards(
    totalProducts: Int,
    totalStock: Int,
    lowStockCount: Int,
    outOfStockCount: Int,
    stockFilter: StockFilter,
    onUpdateStockFilter: (StockFilter) -> Unit,
    availableWidth: Dp,
    modifier: Modifier = Modifier
) {
    val compactSummary = availableWidth < 800.dp

    if (compactSummary) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatCard(
                    title = "Total Products",
                    value = totalProducts.toString(),
                    isSelected = stockFilter == StockFilter.ALL,
                    onClick = { onUpdateStockFilter(StockFilter.ALL) },
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Total Stock",
                    value = totalStock.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatCard(
                    title = "Low Stock",
                    value = lowStockCount.toString(),
                    isWarning = lowStockCount > 0,
                    isSelected = stockFilter == StockFilter.LOW_STOCK,
                    onClick = { onUpdateStockFilter(StockFilter.LOW_STOCK) },
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Out of Stock",
                    value = outOfStockCount.toString(),
                    isWarning = outOfStockCount > 0,
                    isSelected = stockFilter == StockFilter.OUT_OF_STOCK,
                    onClick = { onUpdateStockFilter(StockFilter.OUT_OF_STOCK) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryStatCard(
                title = "Total Products",
                value = totalProducts.toString(),
                isSelected = stockFilter == StockFilter.ALL,
                onClick = { onUpdateStockFilter(StockFilter.ALL) },
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                title = "Total Stock",
                value = totalStock.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                title = "Low Stock",
                value = lowStockCount.toString(),
                isWarning = lowStockCount > 0,
                isSelected = stockFilter == StockFilter.LOW_STOCK,
                onClick = { onUpdateStockFilter(StockFilter.LOW_STOCK) },
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                title = "Out of Stock",
                value = outOfStockCount.toString(),
                isWarning = outOfStockCount > 0,
                isSelected = stockFilter == StockFilter.OUT_OF_STOCK,
                onClick = { onUpdateStockFilter(StockFilter.OUT_OF_STOCK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Search field and category filter dropdown. Switches between side-by-side and stacked layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndCategoryFilters(
    searchText: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    filterCategories: List<String>,
    onCategorySelect: (String) -> Unit,
    availableWidth: Dp,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var categoryFilterExpanded by remember { mutableStateOf(false) }
    val isStacked = availableWidth < 650.dp

    if (isStacked) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchChange,
                placeholder = { Text("Search products...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            ExposedDropdownMenuBox(
                expanded = categoryFilterExpanded,
                onExpandedChange = { categoryFilterExpanded = !categoryFilterExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    placeholder = { Text("Category") },
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryFilterExpanded) },
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryFilterExpanded,
                    onDismissRequest = { categoryFilterExpanded = false }
                ) {
                    filterCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategorySelect(category)
                                categoryFilterExpanded = false
                            }
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchChange,
                placeholder = { Text("Search products...") },
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 280.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            ExposedDropdownMenuBox(
                expanded = categoryFilterExpanded,
                onExpandedChange = { categoryFilterExpanded = !categoryFilterExpanded },
                modifier = Modifier.width(220.dp)
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    placeholder = { Text("Category") },
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryFilterExpanded) },
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryFilterExpanded,
                    onDismissRequest = { categoryFilterExpanded = false }
                ) {
                    filterCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                onCategorySelect(category)
                                categoryFilterExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Small card used for inventory summary statistics.
 */
@Composable
private fun SummaryStatCard(
    title: String,
    value: String,
    isSelected: Boolean = false,
    isWarning: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(18.dp),
        colors = cardColors(
            containerColor = when {
                isSelected -> RoyalGold.copy(alpha = 0.15f)
                isWarning -> MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = when {
                isSelected -> RoyalGold
                isWarning -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                else -> Color.LightGray.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Displays the inventory products list.
 */
@Composable
private fun InventoryList(
    products: List<InventoryProduct>,
    userRole: UserRole,
    availableWidth: Dp,
    onEdit: (InventoryProduct) -> Unit,
    onAdjust: (InventoryProduct, InventoryTransactionType) -> Unit,
    onDeactivate: (InventoryProduct) -> Unit,
    onReactivate: (InventoryProduct) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompactList = availableWidth < 650.dp

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No products found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else if (isCompactList) {
                // Compact list layout (Cards per item)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.productId }) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (product.isActive) 1f else 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = product.category,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusChip(product)
                                    if (userRole == UserRole.ADMIN) {
                                        IconButton(onClick = { onEdit(product) }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        ProductActionMenu(
                                            isActive = product.isActive,
                                            onReactivate = { onReactivate(product) },
                                            onRestock = { onAdjust(product, InventoryTransactionType.RESTOCK) },
                                            onAdjust = { onAdjust(product, InventoryTransactionType.ADJUSTMENT) },
                                            onWaste = { onAdjust(product, InventoryTransactionType.WASTE) },
                                            onDeactivate = { onDeactivate(product) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Stock: ${product.stockQuantity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Reorder: ${product.reorderLevel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Rs ${Money.format(product.unitPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Table layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Product",
                        modifier = Modifier.weight(1.5f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Stock",
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "Reorder",
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "Status",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (userRole == UserRole.ADMIN) {
                        Text(
                            text = "Actions",
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(products, key = { it.productId }) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .alpha(if (product.isActive) 1f else 0.5f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = product.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = product.stockQuantity.toString(),
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = product.reorderLevel.toString(),
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                StatusChip(product)
                            }
                            if (userRole == UserRole.ADMIN) {
                                Row(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { onEdit(product) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    ProductActionMenu(
                                        isActive = product.isActive,
                                        onReactivate = { onReactivate(product) },
                                        onRestock = { onAdjust(product, InventoryTransactionType.RESTOCK) },
                                        onAdjust = { onAdjust(product, InventoryTransactionType.ADJUSTMENT) },
                                        onWaste = { onAdjust(product, InventoryTransactionType.WASTE) },
                                        onDeactivate = { onDeactivate(product) }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

/**
 * Shows whether a product is In stock, Low stock, or Out of stock.
 */
@Composable
fun StatusChip(product: InventoryProduct) {
    val color = when {
        product.stockQuantity <= 0 -> MaterialTheme.colorScheme.error
        product.stockQuantity <= product.reorderLevel -> Color(0xFFFFA000)
        else -> Color(0xFF4CAF50)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = product.stockStatus.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Menu containing stock and product actions.
 */
@Composable
fun ProductActionMenu(
    onRestock: () -> Unit,
    onAdjust: () -> Unit,
    onWaste: () -> Unit,
    onDeactivate: () -> Unit,
    isActive: Boolean,
    onReactivate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Product actions"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Restock") },
                onClick = {
                    onRestock()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Adjust Stock") },
                onClick = {
                    onAdjust()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Record Waste") },
                onClick = {
                    onWaste()
                    expanded = false
                }
            )
            if (!isActive) {
                DropdownMenuItem(
                    text = { Text("Reactivate") },
                    onClick = {
                        onReactivate()
                        expanded = false
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete / Deactivate",
                        color = Color.Red
                    )
                },
                onClick = {
                    onDeactivate()
                    expanded = false
                }
            )
        }
    }
}

/**
 * Dialog used to add or edit products.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    userRole: UserRole,
    categories: List<String>,
    product: InventoryProduct? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        category: String,
        price: Long,
        cost: Long,
        stock: Int,
        reorder: Int,
        expiryDay: Long?
    ) -> Unit,
    onManageBatches: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var price by remember { mutableStateOf(product?.unitPrice?.let(Money::format) ?: "") }
    var cost by remember { mutableStateOf(product?.costPrice?.let(Money::format) ?: "0") }
    var stock by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "0") }
    var reorder by remember { mutableStateOf(product?.reorderLevel?.toString() ?: "5") }

    var initialExpiryDay by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "Add Product" else "Edit Product",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Product name.
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category dropdown.
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = {
                        if (categories.isNotEmpty()) {
                            categoryDropdownExpanded = !categoryDropdownExpanded
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        placeholder = { Text("Select a category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        supportingText = {
                            if (categories.isEmpty()) {
                                Text("Create a category first.")
                            } else {
                                Text("Select from saved categories.")
                            }
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption) },
                                onClick = {
                                    category = categoryOption
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Selling price.
                OutlinedTextField(
                    value = price,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() || it == '.' }) {
                            price = value
                        }
                    },
                    label = { Text("Selling Price (Rs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Cost price.
                if (userRole == UserRole.ADMIN) {
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() || it == '.' }) {
                                cost = value
                            }
                        },
                        label = { Text("Cost Price (Rs)") },
                        supportingText = { Text("Used for gross profit calculations.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Initial stock.
                if (product == null) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() }) {
                                stock = value
                            }
                        },
                        label = { Text("Initial Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    ExpiryDatePickerField(
                        expiryDay = initialExpiryDay,
                        onExpiryChanged = { initialExpiryDay = it },
                        label = "Initial stock expiry (optional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Reorder level.
                OutlinedTextField(
                    value = reorder,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) {
                            reorder = value
                        }
                    },
                    label = { Text("Reorder Level") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (product != null && onManageBatches != null) {
                    OutlinedButton(
                        onClick = onManageBatches,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage stock batches and expiry dates")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() &&
                        category.isNotBlank() &&
                        Money.parse(price) != null &&
                        Money.parse(cost) != null &&
                        reorder.toIntOrNull() != null &&
                        (product != null || (stock.toIntOrNull() ?: 0) >= 0),
                onClick = {
                    onConfirm(
                        name.trim(),
                        category,
                        Money.parse(price) ?: 0,
                        Money.parse(cost) ?: 0,
                        stock.toIntOrNull() ?: 0,
                        reorder.toIntOrNull() ?: 5,
                        initialExpiryDay
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog used by Admin users to create and remove categories.
 */
@Composable
fun ManageCategoriesDialog(
    categories: List<CategoryEntity>,
    errorMessage: String?,
    onAddCategory: (String) -> Unit,
    onRemoveCategory: (CategoryEntity) -> Unit,
    onClearError: () -> Unit,
    onDismiss: () -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            onClearError()
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Category, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manage Categories",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            BoxWithConstraints {
                val isNarrowDialog = maxWidth < 380.dp
                Column(
                    modifier = Modifier.imePadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create categories used when adding products.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (isNarrowDialog) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("New Category") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                enabled = newCategoryName.isNotBlank(),
                                onClick = {
                                    onAddCategory(newCategoryName)
                                    newCategoryName = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("New Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                enabled = newCategoryName.isNotBlank(),
                                onClick = {
                                    onAddCategory(newCategoryName)
                                    newCategoryName = ""
                                }
                            ) {
                                Text("Add")
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HorizontalDivider()

                    if (categories.isEmpty()) {
                        Text(
                            text = "No categories have been created yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(items = categories, key = { it.categoryId }) { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.categoryName,
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(onClick = { onRemoveCategory(category) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove ${category.categoryName}",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onClearError()
                    onDismiss()
                }
            ) {
                Text("Done")
            }
        }
    )
}

/** Batch expiry maintenance. */
@Composable
private fun StockBatchManagementDialog(
    product: InventoryProduct,
    batches: List<NamedBatch>,
    onDismiss: () -> Unit,
    onUpdateExpiry: (batchId: Long, expiryDay: Long?) -> Unit,
    onAddBatch: (quantity: Int, expiryDay: Long?) -> Unit
) {
    var editingBatch by remember { mutableStateOf<NamedBatch?>(null) }
    var showAddBatchDialog by remember { mutableStateOf(false) }
    var newBatchQuantity by remember { mutableStateOf("") }
    var newBatchExpiryDay by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Stock Batches · ${product.name}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Manage the remaining stock batches and their expiry dates.",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (batches.isEmpty()) {
                    Text(
                        text = "There are no remaining stock batches. Add a stock batch or restock the product.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = batches, key = { it.id }) { batch ->
                            val formattedExpiry = remember(batch.expiryDay) {
                                batch.expiryDay?.let { day ->
                                    val date = java.time.LocalDate.ofEpochDay(day)
                                    date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
                                }
                            }
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Batch ${batch.id}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(text = "Remaining quantity: ${batch.remaining}")
                                    Text(
                                        text = if (formattedExpiry != null) "Expiry: $formattedExpiry" else "Expiry: Not set"
                                    )
                                    TextButton(
                                        onClick = {
                                            editingBatch = batch
                                        }
                                    ) {
                                        Text(text = "Edit Expiry")
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                OutlinedButton(
                    onClick = {
                        newBatchQuantity = ""
                        newBatchExpiryDay = null
                        showAddBatchDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add Stock Batch")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Done")
            }
        }
    )

    // Edit batch expiry dialog
    editingBatch?.let { batch ->
        var editingBatchExpiryDay by remember(batch) { mutableStateOf(batch.expiryDay) }

        AlertDialog(
            onDismissRequest = { editingBatch = null },
            title = { Text(text = "Edit Batch Expiry", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Batch ${batch.id} · ${batch.remaining} units")
                    ExpiryDatePickerField(
                        expiryDay = editingBatchExpiryDay,
                        onExpiryChanged = { editingBatchExpiryDay = it },
                        label = "Expiry Date (optional)",
                        restrictPastDates = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateExpiry(batch.id, editingBatchExpiryDay)
                        editingBatch = null
                    }
                ) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBatch = null }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    // Add stock batch dialog
    if (showAddBatchDialog) {
        AlertDialog(
            onDismissRequest = { showAddBatchDialog = false },
            title = { Text(text = "Add Stock Batch", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Add stock for ${product.name}. A new batch will be created.")

                    OutlinedTextField(
                        value = newBatchQuantity,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() }) {
                                newBatchQuantity = value
                            }
                        },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    ExpiryDatePickerField(
                        expiryDay = newBatchExpiryDay,
                        onExpiryChanged = { newBatchExpiryDay = it },
                        label = "Expiry Date (optional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = (newBatchQuantity.toIntOrNull() ?: 0) > 0,
                    onClick = {
                        val quantity = newBatchQuantity.toIntOrNull() ?: 0
                        onAddBatch(quantity, newBatchExpiryDay)
                        showAddBatchDialog = false
                    }
                ) {
                    Text(text = "Add Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBatchDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

/**
 * Dialog for stock operations such as restocking, manual adjustment and waste.
 */
@Composable
fun StockActionDialog(
    product: InventoryProduct,
    type: InventoryTransactionType,
    onDismiss: () -> Unit,
    onConfirm: (
        qty: Int,
        reason: String,
        expiryDay: Long?
    ) -> Unit
) {
    var expiryDay by remember { mutableStateOf<Long?>(null) }
    var qty by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val actionName = type.name.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            Text(
                text = "$actionName - ${product.name}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Current Stock: ${product.stockQuantity}",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = qty,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) {
                            qty = value
                        }
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (type == InventoryTransactionType.RESTOCK) {
                    ExpiryDatePickerField(
                        expiryDay = expiryDay,
                        onExpiryChanged = { expiryDay = it },
                        label = "Expiry Date (optional)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (type != InventoryTransactionType.RESTOCK) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = qty.toIntOrNull() != null &&
                        (qty.toIntOrNull() ?: 0) > 0 &&
                        (type == InventoryTransactionType.RESTOCK || reason.isNotBlank()),
                onClick = {
                    onConfirm(
                        qty.toIntOrNull() ?: 0,
                        reason.trim(),
                        expiryDay
                    )
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
