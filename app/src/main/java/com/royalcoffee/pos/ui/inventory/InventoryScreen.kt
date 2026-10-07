package com.royalcoffee.pos.ui.inventory

import com.royalcoffee.pos.model.Money
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.royalcoffee.pos.data.local.entity.CategoryEntity
import com.royalcoffee.pos.data.local.entity.InventoryTransactionType
import com.royalcoffee.pos.data.local.repository.CategoryRepository
import com.royalcoffee.pos.data.local.repository.ProductRepository
import com.royalcoffee.pos.model.InventoryProduct
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold
import java.util.Locale
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
    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var showCategoryDialog by remember {
        mutableStateOf(false)
    }

    var editingProduct by remember {
        mutableStateOf<InventoryProduct?>(null)
    }

    var adjustingProduct by remember {
        mutableStateOf<InventoryProduct?>(null)
    }

    var adjustmentType by remember {
        mutableStateOf(InventoryTransactionType.RESTOCK)
    }


    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------------------------------------------------------
            // HEADER
            // ---------------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                /*
                 * Empty spacer helps keep the title visually centred.
                 */
                Spacer(
                    modifier = Modifier.width(160.dp)
                )

                Text(
                    text = "Inventory",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                /*
                 * Only Admin users can manage inventory.
                 */
                if (userRole == UserRole.ADMIN) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Manage Categories button.
                        OutlinedButton(
                            onClick = {
                                showCategoryDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            )
                        ) {

                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text(
                                text = "Manage Categories"
                            )
                        }


                        // Add Product button.
                        Button(
                            onClick = {
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            )
                        ) {

                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text(
                                text = "Add Product"
                            )
                        }
                    }

                } else {

                    Spacer(
                        modifier = Modifier.width(160.dp)
                    )
                }
            }


            Text(
                text = "Manage products, stock levels, categories, and audit history.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.7f
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    top = 8.dp
                )
            )


            Spacer(
                modifier = Modifier.height(28.dp)
            )


            Column(
                modifier = Modifier.widthIn(
                    max = 1000.dp
                )
            ) {

                // ---------------------------------------------------------
                // INVENTORY SUMMARY
                // ---------------------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp
                    )
                ) {

                    SummaryStatCard(
                        title = "Total Products",
                        value = totalProducts.toString(),
                        isSelected = stockFilter == StockFilter.ALL,
                        onClick = {
                            viewModel.updateStockFilter(
                                StockFilter.ALL
                            )
                        },
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
                        isSelected =
                            stockFilter == StockFilter.LOW_STOCK,
                        onClick = {
                            viewModel.updateStockFilter(
                                StockFilter.LOW_STOCK
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )


                    SummaryStatCard(
                        title = "Out of Stock",
                        value = outOfStockCount.toString(),
                        isWarning = outOfStockCount > 0,
                        isSelected =
                            stockFilter == StockFilter.OUT_OF_STOCK,
                        onClick = {
                            viewModel.updateStockFilter(
                                StockFilter.OUT_OF_STOCK
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }


                Spacer(
                    modifier = Modifier.height(24.dp)
                )


                // ---------------------------------------------------------
                // SEARCH AND CATEGORY FILTERS
                // ---------------------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp
                    )
                ) {

                    val focusManager =
                        LocalFocusManager.current


                    OutlinedTextField(
                        value = searchText,

                        onValueChange = {
                            viewModel.updateSearch(it)
                        },

                        placeholder = {
                            Text("Search products...")
                        },

                        modifier = Modifier.weight(1f),

                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },

                        shape = RoundedCornerShape(14.dp),

                        singleLine = true,

                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),

                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                            }
                        )
                    )


                    /*
                     * Category filter values come from Room.
                     *
                     * "All" is added by the ViewModel.
                     */
                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        items(
                            filterCategories
                        ) { category ->

                            FilterChip(
                                selected =
                                    selectedCategory == category,

                                onClick = {
                                    viewModel.updateCategory(
                                        category
                                    )
                                },

                                label = {
                                    Text(category)
                                }
                            )
                        }
                    }
                }


                Spacer(
                    modifier = Modifier.height(24.dp)
                )


                // ---------------------------------------------------------
                // PRODUCT LIST
                // ---------------------------------------------------------

                InventoryList(
                    products = products,
                    userRole = userRole,

                    onEdit = {
                        editingProduct = it
                    },

                    onAdjust = {
                            product,
                            type ->

                        adjustingProduct =
                            product

                        adjustmentType =
                            type
                    },

                    onReactivate = { viewModel.reactivateProduct(it.productId) },
                    onDeactivate = { viewModel.requestRemoval(it) }
                )
            }
        }
    }


    // ---------------------------------------------------------
    // INVENTORY ERROR
    // ---------------------------------------------------------

    viewModel.removalCandidate?.let { product ->
        AlertDialog(onDismissRequest = { viewModel.cancelRemoval() },
            title = { Text(if (viewModel.removalPermanent) "Permanently delete product?" else "Deactivate product?") },
            text = { Text(if (viewModel.removalPermanent) "Delete ${product.name}? This cannot be undone. If related history is added before confirmation, it will be deactivated instead."
                else "${product.name} has stock or related history, or legacy history cannot be ruled out. It will remain in inventory and can be reactivated.") },
            confirmButton = { TextButton(onClick = { viewModel.confirmRemoval() }) { Text(if (viewModel.removalPermanent) "Delete permanently" else "Deactivate") } },
            dismissButton = { TextButton(onClick = { viewModel.cancelRemoval() }) { Text("Cancel") } })
    }
    viewModel.lifecycleMessage?.let { message ->
        AlertDialog(onDismissRequest = { viewModel.clearLifecycleMessage() }, title = { Text("Product updated") },
            text = { Text(message) }, confirmButton = { TextButton(onClick = { viewModel.clearLifecycleMessage() }) { Text("OK") } })
    }

    if (inventoryError != null) {

        AlertDialog(
            onDismissRequest = {
                viewModel.clearError()
            },

            title = {
                Text("Inventory Error")
            },

            text = {
                Text(inventoryError)
            },

            confirmButton = {

                Button(
                    onClick = {
                        viewModel.clearError()
                    }
                ) {

                    Text("OK")
                }
            }
        )
    }


    // ---------------------------------------------------------
    // ADD PRODUCT DIALOG
    // ---------------------------------------------------------

    if (showAddDialog) {

        ProductFormDialog(
            userRole = userRole,

            /*
             * Categories now come from Room.
             */
            categories = productCategories,

            onDismiss = {
                showAddDialog = false
            },

            onConfirm = {
                    name,
                    category,
                    price,
                    cost,
                    stock,
                    reorder ->

                viewModel.addProduct(
                    name = name,
                    category = category,
                    price = price,
                    cost = cost,
                    stock = stock,
                    reorder = reorder
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

            onDismiss = {
                editingProduct = null
            },

            onConfirm = {
                    name,
                    category,
                    price,
                    cost,
                    _,
                    reorder ->

                viewModel.updateProduct(
                    productId =
                        editingProduct!!.productId,

                    name = name,

                    category = category,

                    price = price,

                    cost = cost,

                    reorder = reorder,

                    active = editingProduct!!.isActive
                )

                editingProduct = null
            }
        )
    }


    // ---------------------------------------------------------
    // STOCK ADJUSTMENT DIALOG
    // ---------------------------------------------------------

    if (adjustingProduct != null) {

        StockActionDialog(
            product =
                adjustingProduct!!,

            type =
                adjustmentType,

            onDismiss = {
                adjustingProduct = null
            },

            onConfirm = {
                    quantity,
                    reason, expiryDay ->

                val change =
                    if (
                        adjustmentType ==
                        InventoryTransactionType.RESTOCK
                    ) {

                        quantity

                    } else {

                        -quantity
                    }


                viewModel.adjustStock(
                    productId =
                        adjustingProduct!!.productId,

                    change =
                        change,

                    type =
                        adjustmentType,

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
            categories =
                categoryEntities,

            errorMessage =
                viewModel.categoryError,

            onAddCategory = {
                    categoryName ->

                viewModel.addCategory(
                    categoryName
                )
            },

            onRemoveCategory = {
                    category ->

                viewModel.removeCategory(
                    category
                )
            },

            onClearError = {
                viewModel.clearCategoryError()
            },

            onDismiss = {
                showCategoryDialog = false
            }
        )
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
            if (onClick != null) {

                Modifier.clickable {
                    onClick()
                }

            } else {

                Modifier
            }
        ),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                when {

                    isSelected ->
                        RoyalGold.copy(
                            alpha = 0.15f
                        )

                    isWarning ->
                        MaterialTheme.colorScheme.error.copy(
                            alpha = 0.05f
                        )

                    else ->
                        MaterialTheme.colorScheme.surface
                }
        ),

        border = BorderStroke(
            width =
                if (isSelected) {
                    2.dp
                } else {
                    1.dp
                },

            color =
                when {

                    isSelected ->
                        RoyalGold

                    isWarning ->
                        MaterialTheme.colorScheme.error.copy(
                            alpha = 0.3f
                        )

                    else ->
                        Color.LightGray.copy(
                            alpha = 0.2f
                        )
                }
        )
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Text(
                text = value,
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color =
                    if (isWarning) {

                        MaterialTheme.colorScheme.error

                    } else {

                        MaterialTheme.colorScheme.primary
                    }
            )
        }
    }
}


/**
 * Displays the inventory products.
 */
@Composable
private fun InventoryList(
    products: List<InventoryProduct>,
    userRole: UserRole,
    onEdit: (InventoryProduct) -> Unit,
    onAdjust: (
        InventoryProduct,
        InventoryTransactionType
    ) -> Unit,
    onDeactivate: (InventoryProduct) -> Unit,
    onReactivate: (InventoryProduct) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),

        border = BorderStroke(
            1.dp,
            Color.LightGray.copy(
                alpha = 0.1f
            )
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Table header.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 12.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Product",
                    modifier = Modifier.weight(1.5f),
                    fontWeight = FontWeight.Bold,
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "Stock",
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "Reorder",
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "Status",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style =
                        MaterialTheme.typography.labelLarge
                )

                if (userRole == UserRole.ADMIN) {

                    Text(
                        text = "Actions",
                        modifier = Modifier.weight(1.2f),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold,
                        style =
                            MaterialTheme.typography.labelLarge
                    )
                }
            }


            HorizontalDivider(
                color =
                    Color.LightGray.copy(
                        alpha = 0.2f
                    )
            )


            LazyColumn {

                items(
                    products,
                    key = {
                        it.productId
                    }
                ) { product ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 12.dp
                            )
                            .alpha(
                                if (product.isActive) {
                                    1f
                                } else {
                                    0.5f
                                }
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column(
                            modifier =
                                Modifier.weight(1.5f)
                        ) {

                            Text(
                                text = product.name,
                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Text(
                                text = product.category,
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }


                        Text(
                            text =
                                product.stockQuantity.toString(),
                            modifier =
                                Modifier.weight(0.8f),
                            textAlign =
                                TextAlign.Center
                        )


                        Text(
                            text =
                                product.reorderLevel.toString(),
                            modifier =
                                Modifier.weight(0.8f),
                            textAlign =
                                TextAlign.Center
                        )


                        Box(
                            modifier =
                                Modifier.weight(1f),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            StatusChip(
                                product
                            )
                        }


                        if (
                            userRole ==
                            UserRole.ADMIN
                        ) {

                            Row(
                                modifier =
                                    Modifier.weight(1.2f),

                                horizontalArrangement =
                                    Arrangement.End
                            ) {

                                IconButton(
                                    onClick = {
                                        onEdit(
                                            product
                                        )
                                    }
                                ) {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Edit,
                                        contentDescription =
                                            "Edit",
                                        tint =
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                    )
                                }


                                ProductActionMenu(
                                    isActive = product.isActive,
                                    onReactivate = { onReactivate(product) },
                                    onRestock = {
                                        onAdjust(
                                            product,
                                            InventoryTransactionType
                                                .RESTOCK
                                        )
                                    },

                                    onAdjust = {
                                        onAdjust(
                                            product,
                                            InventoryTransactionType
                                                .ADJUSTMENT
                                        )
                                    },

                                    onWaste = {
                                        onAdjust(
                                            product,
                                            InventoryTransactionType
                                                .WASTE
                                        )
                                    },

                                    onDeactivate = {
                                        onDeactivate(
                                            product
                                        )
                                    }
                                )
                            }
                        }
                    }


                    HorizontalDivider(
                        color =
                            Color.LightGray.copy(
                                alpha = 0.1f
                            )
                    )
                }
            }
        }
    }
}


/**
 * Shows whether a product is:
 * - In stock
 * - Low stock
 * - Out of stock
 */
@Composable
fun StatusChip(
    product: InventoryProduct
) {

    val color =
        when {

            product.stockQuantity <= 0 ->
                MaterialTheme.colorScheme.error

            product.stockQuantity <=
                    product.reorderLevel ->
                Color(0xFFFFA000)

            else ->
                Color(0xFF4CAF50)
        }


    Surface(
        color =
            color.copy(
                alpha = 0.1f
            ),

        shape =
            RoundedCornerShape(50),

        border =
            BorderStroke(
                1.dp,
                color.copy(
                    alpha = 0.4f
                )
            )
    ) {

        Text(
            text =
                product.stockStatus.uppercase(),

            modifier =
                Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),

            style =
                MaterialTheme.typography.labelSmall,

            color =
                color,

            fontWeight =
                FontWeight.Bold
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

    var expanded by remember {
        mutableStateOf(false)
    }


    Box {

        IconButton(
            onClick = {
                expanded = true
            }
        ) {

            Icon(
                imageVector =
                    Icons.Default.MoreVert,
                contentDescription =
                    "Product actions"
            )
        }


        DropdownMenu(
            expanded = expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            DropdownMenuItem(
                text = {
                    Text("Restock")
                },

                onClick = {
                    onRestock()
                    expanded = false
                }
            )


            DropdownMenuItem(
                text = {
                    Text("Adjust Stock")
                },

                onClick = {
                    onAdjust()
                    expanded = false
                }
            )


            DropdownMenuItem(
                text = {
                    Text("Record Waste")
                },

                onClick = {
                    onWaste()
                    expanded = false
                }
            )


            if (!isActive) DropdownMenuItem(text = { Text("Reactivate") }, onClick = { onReactivate(); expanded = false })
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
 *
 * Product categories are selected from the database.
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
        reorder: Int
    ) -> Unit
) {

    var name by remember {
        mutableStateOf(
            product?.name ?: ""
        )
    }


    var category by remember {
        mutableStateOf(
            product?.category ?: ""
        )
    }


    var categoryDropdownExpanded by remember {
        mutableStateOf(false)
    }


    var price by remember {
        mutableStateOf(
            product?.unitPrice?.let(Money::format)
                ?: ""
        )
    }


    var cost by remember {
        mutableStateOf(
            product?.costPrice?.let(Money::format)
                ?: "0"
        )
    }


    var stock by remember {
        mutableStateOf(
            product?.stockQuantity?.toString()
                ?: "0"
        )
    }


    var reorder by remember {
        mutableStateOf(
            product?.reorderLevel?.toString()
                ?: "5"
        )
    }


    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    if (product == null) {
                        "Add Product"
                    } else {
                        "Edit Product"
                    }
            )
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                // Product name.
                OutlinedTextField(
                    value =
                        name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text("Name")
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true
                )


                // Category dropdown.
                ExposedDropdownMenuBox(
                    expanded =
                        categoryDropdownExpanded,

                    onExpandedChange = {

                        if (
                            categories.isNotEmpty()
                        ) {

                            categoryDropdownExpanded =
                                !categoryDropdownExpanded
                        }
                    }
                ) {

                    OutlinedTextField(
                        value =
                            category,

                        onValueChange = {
                            // Read-only field.
                        },

                        readOnly = true,

                        label = {
                            Text("Category")
                        },

                        placeholder = {
                            Text(
                                "Select a category"
                            )
                        },

                        trailingIcon = {

                            ExposedDropdownMenuDefaults
                                .TrailingIcon(
                                    expanded =
                                        categoryDropdownExpanded
                                )
                        },

                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth(),

                        supportingText = {

                            if (
                                categories.isEmpty()
                            ) {

                                Text(
                                    "Create a category first."
                                )

                            } else {

                                Text(
                                    "Select from saved categories."
                                )
                            }
                        }
                    )


                    ExposedDropdownMenu(
                        expanded =
                            categoryDropdownExpanded,

                        onDismissRequest = {
                            categoryDropdownExpanded =
                                false
                        }
                    ) {

                        categories.forEach {
                                categoryOption ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        categoryOption
                                    )
                                },

                                onClick = {

                                    category =
                                        categoryOption

                                    categoryDropdownExpanded =
                                        false
                                }
                            )
                        }
                    }
                }


                // Selling price.
                OutlinedTextField(
                    value =
                        price,

                    onValueChange = {
                            value ->

                        if (
                            value.all {
                                it.isDigit() || it == '.'
                            }
                        ) {

                            price = value
                        }
                    },

                    label = {
                        Text(
                            "Selling Price (Rs)"
                        )
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Decimal
                        ),

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true
                )


                // Cost price.
                if (
                    userRole ==
                    UserRole.ADMIN
                ) {

                    OutlinedTextField(
                        value =
                            cost,

                        onValueChange = {
                                value ->

                            if (
                                value.all {
                                    it.isDigit() || it == '.'
                                }
                            ) {

                                cost = value
                            }
                        },

                        label = {
                            Text(
                                "Cost Price (Rs)"
                            )
                        },

                        supportingText = {
                            Text(
                                "Used for gross profit calculations."
                            )
                        },

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Decimal
                            ),

                        modifier =
                            Modifier.fillMaxWidth(),

                        singleLine = true
                    )
                }


                // Initial stock is shown only when adding a product.
                if (
                    product == null
                ) {

                    OutlinedTextField(
                        value =
                            stock,

                        onValueChange = {
                                value ->

                            if (
                                value.all {
                                    it.isDigit()
                                }
                            ) {

                                stock = value
                            }
                        },

                        label = {
                            Text(
                                "Initial Stock"
                            )
                        },

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            ),

                        modifier =
                            Modifier.fillMaxWidth(),

                        singleLine = true
                    )
                }


                // Reorder level.
                OutlinedTextField(
                    value =
                        reorder,

                    onValueChange = {
                            value ->

                        if (
                            value.all {
                                it.isDigit()
                            }
                        ) {

                            reorder = value
                        }
                    },

                    label = {
                        Text(
                            "Reorder Level"
                        )
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true
                )
            }
        },

        confirmButton = {

            Button(
                enabled =
                    name.isNotBlank() &&
                            category.isNotBlank() &&
                            Money.parse(price) != null &&
                            Money.parse(cost) != null &&
                            reorder.toIntOrNull() != null,

                onClick = {

                    onConfirm(
                        name.trim(),
                        category,
                        Money.parse(price)
                            ?: 0,
                        Money.parse(cost)
                            ?: 0,
                        stock.toIntOrNull()
                            ?: 0,
                        reorder.toIntOrNull()
                            ?: 5
                    )
                }
            ) {

                Text(
                    "Save"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancel"
                )
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

    var newCategoryName by remember {
        mutableStateOf("")
    }


    AlertDialog(
        onDismissRequest = {

            onClearError()
            onDismiss()
        },

        title = {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Category,
                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text =
                        "Manage Categories"
                )
            }
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text =
                        "Create categories used when adding products.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )


                // Add a new category.
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(
                        value =
                            newCategoryName,

                        onValueChange = {

                            newCategoryName =
                                it
                        },

                        label = {
                            Text(
                                "New Category"
                            )
                        },

                        singleLine =
                            true,

                        modifier =
                            Modifier.weight(1f)
                    )


                    Button(
                        enabled =
                            newCategoryName
                                .isNotBlank(),

                        onClick = {

                            onAddCategory(
                                newCategoryName
                            )

                            newCategoryName =
                                ""
                        }
                    ) {

                        Text(
                            "Add"
                        )
                    }
                }


                // Show category-management errors.
                if (
                    errorMessage != null
                ) {

                    Text(
                        text =
                            errorMessage,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }


                HorizontalDivider()


                // List existing categories.
                if (
                    categories.isEmpty()
                ) {

                    Text(
                        text =
                            "No categories have been created yet.",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                } else {

                    LazyColumn(
                        modifier =
                            Modifier.heightIn(
                                max = 300.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                4.dp
                            )
                    ) {

                        items(
                            items =
                                categories,

                            key = {
                                it.categoryId
                            }
                        ) {
                                category ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 6.dp
                                    ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(
                                    text =
                                        category.categoryName,

                                    modifier =
                                        Modifier.weight(1f),

                                    fontWeight =
                                        FontWeight.Medium
                                )


                                IconButton(
                                    onClick = {

                                        onRemoveCategory(
                                            category
                                        )
                                    }
                                ) {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Delete,

                                        contentDescription =
                                            "Remove ${category.categoryName}",

                                        tint =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
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

                Text(
                    "Done"
                )
            }
        }
    )
}


/**
 * Dialog for stock operations such as restocking,
 * manual adjustment and waste.
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

    var expiryText by remember { mutableStateOf("") }
    val expiryDay = runCatching { java.time.LocalDate.parse(expiryText.trim()).toEpochDay() }.getOrNull()
    var qty by remember {
        mutableStateOf("")
    }

    var reason by remember {
        mutableStateOf("")
    }


    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            val actionName =
                type.name
                    .lowercase()
                    .replaceFirstChar {

                        if (it.isLowerCase()) {

                            it.titlecase(
                                Locale.getDefault()
                            )

                        } else {

                            it.toString()
                        }
                    }


            Text(
                "$actionName - ${product.name}"
            )
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text =
                        "Current Stock: ${product.stockQuantity}",
                    style =
                        MaterialTheme.typography.bodyMedium
                )


                OutlinedTextField(
                    value =
                        qty,

                    onValueChange = {
                            value ->

                        if (
                            value.all {
                                it.isDigit()
                            }
                        ) {

                            qty =
                                value
                        }
                    },

                    label = {
                        Text(
                            "Quantity"
                        )
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine =
                        true
                )


                if (type == InventoryTransactionType.RESTOCK) {
                    OutlinedTextField(value = expiryText, onValueChange = { expiryText = it },
                        label = { Text("Expiry YYYY-MM-DD (optional)") }, singleLine = true,
                        isError = expiryText.isNotBlank() && expiryDay == null)
                }
                /*
                 * Restocking does not require an explanation.
                 *
                 * Manual adjustments and waste should include a reason.
                 */
                if (
                    type !=
                    InventoryTransactionType.RESTOCK
                ) {

                    OutlinedTextField(
                        value =
                            reason,

                        onValueChange = {
                            reason = it
                        },

                        label = {
                            Text(
                                "Reason"
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }
            }
        },

        confirmButton = {

            Button(
                enabled =
                    (expiryText.isBlank() || expiryDay != null) && qty.toIntOrNull() != null &&
                            (qty.toIntOrNull()
                                ?: 0) > 0 &&
                            (
                                    type ==
                                            InventoryTransactionType.RESTOCK ||
                                            reason.isNotBlank()
                                    ),

                onClick = {

                    onConfirm(
                        qty.toIntOrNull()
                            ?: 0,

                        reason.trim(),
                        expiryDay
                    )
                }
            ) {

                Text(
                    "Submit"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}
