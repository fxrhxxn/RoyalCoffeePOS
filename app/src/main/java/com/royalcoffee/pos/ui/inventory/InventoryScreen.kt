package com.royalcoffee.pos.ui.inventory

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.royalcoffee.pos.data.local.entity.InventoryTransactionType
import com.royalcoffee.pos.data.local.repository.ProductRepository
import com.royalcoffee.pos.model.InventoryProduct
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold

@Composable
fun InventoryScreen(
    userRole: UserRole,
    productRepository: ProductRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModelFactory(productRepository, userRole)
    )

    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val stockFilter by viewModel.stockFilter.collectAsStateWithLifecycle()
    val totalProducts by viewModel.totalProducts.collectAsStateWithLifecycle()
    val totalStock by viewModel.totalStock.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val outOfStockCount by viewModel.outOfStockCount.collectAsStateWithLifecycle()

    val inventoryError = viewModel.inventoryError

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<InventoryProduct?>(null) }
    var adjustingProduct by remember { mutableStateOf<InventoryProduct?>(null) }
    var adjustmentType by remember { mutableStateOf(InventoryTransactionType.RESTOCK) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(160.dp)) // Offset for button symmetry
                
                Text(
                    text = "Inventory",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (userRole == UserRole.ADMIN) {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Product")
                    }
                } else {
                    Spacer(modifier = Modifier.width(160.dp))
                }
            }

            Text(
                text = "Manage products, stock levels, and audit history.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(modifier = Modifier.widthIn(max = 1000.dp)) {
                // Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryStatCard(
                        title = "Total Products",
                        value = totalProducts.toString(),
                        isSelected = stockFilter == StockFilter.ALL,
                        onClick = { viewModel.updateStockFilter(StockFilter.ALL) },
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
                        onClick = { viewModel.updateStockFilter(StockFilter.LOW_STOCK) },
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Out of Stock",
                        value = outOfStockCount.toString(),
                        isWarning = outOfStockCount > 0,
                        isSelected = stockFilter == StockFilter.OUT_OF_STOCK,
                        onClick = { viewModel.updateStockFilter(StockFilter.OUT_OF_STOCK) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Search and Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val focusManager = LocalFocusManager.current
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { viewModel.updateSearch(it) },
                        placeholder = { Text("Search products...") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        )
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { viewModel.updateCategory(category) },
                                label = { Text(category) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Product List
                InventoryList(
                    products = products,
                    userRole = userRole,
                    onEdit = { editingProduct = it },
                    onAdjust = { p, type -> 
                        adjustingProduct = p
                        adjustmentType = type
                    },
                    onDeactivate = { viewModel.deactivateProduct(it.productId) }
                )
            }
        }
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

    if (showAddDialog) {
        ProductFormDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cat, price, stock, reorder ->
                viewModel.addProduct(name, cat, price, stock, reorder)
                showAddDialog = false
            }
        )
    }

    if (editingProduct != null) {
        ProductFormDialog(
            product = editingProduct,
            onDismiss = { editingProduct = null },
            onConfirm = { name, cat, price, _, reorder ->
                viewModel.updateProduct(editingProduct!!.productId, name, cat, price, reorder, true)
                editingProduct = null
            }
        )
    }

    if (adjustingProduct != null) {
        StockActionDialog(
            product = adjustingProduct!!,
            type = adjustmentType,
            onDismiss = { adjustingProduct = null },
            onConfirm = { qty, reason ->
                val change = if (adjustmentType == InventoryTransactionType.RESTOCK) qty else -qty
                viewModel.adjustStock(adjustingProduct!!.productId, change, adjustmentType, reason)
                adjustingProduct = null
            }
        )
    }
}

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
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
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
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun InventoryList(
    products: List<InventoryProduct>,
    userRole: UserRole,
    onEdit: (InventoryProduct) -> Unit,
    onAdjust: (InventoryProduct, InventoryTransactionType) -> Unit,
    onDeactivate: (InventoryProduct) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Product", Modifier.weight(1.5f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text("Stock", Modifier.weight(0.8f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text("Reorder", Modifier.weight(0.8f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text("Status", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                if (userRole == UserRole.ADMIN) {
                    Text("Actions", Modifier.weight(1.2f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            
            LazyColumn {
                items(products) { product ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp).alpha(if (product.isActive) 1f else 0.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1.5f)) {
                            Text(product.name, fontWeight = FontWeight.SemiBold)
                            Text(product.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(product.stockQuantity.toString(), Modifier.weight(0.8f), textAlign = TextAlign.Center)
                        Text(product.reorderLevel.toString(), Modifier.weight(0.8f), textAlign = TextAlign.Center)
                        
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            StatusChip(product)
                        }

                        if (userRole == UserRole.ADMIN) {
                            Row(Modifier.weight(1.2f), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { onEdit(product) }) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary) }
                                ProductActionMenu(
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

@Composable
fun StatusChip(p: InventoryProduct) {
    val color = when {
        p.stockQuantity <= 0 -> MaterialTheme.colorScheme.error
        p.stockQuantity <= p.reorderLevel -> Color(0xFFFFA000)
        else -> Color(0xFF4CAF50)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = p.stockStatus.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProductActionMenu(
    onRestock: () -> Unit,
    onAdjust: () -> Unit,
    onWaste: () -> Unit,
    onDeactivate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, null) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Restock") }, onClick = { onRestock(); expanded = false })
            DropdownMenuItem(text = { Text("Adjust Stock") }, onClick = { onAdjust(); expanded = false })
            DropdownMenuItem(text = { Text("Record Waste") }, onClick = { onWaste(); expanded = false })
            HorizontalDivider()
            DropdownMenuItem(text = { Text("Deactivate", color = Color.Red) }, onClick = { onDeactivate(); expanded = false })
        }
    }
}

@Composable
fun ProductFormDialog(
    product: InventoryProduct? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, cat: String, price: Int, stock: Int, reorder: Int) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var price by remember { mutableStateOf(product?.unitPrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "0") }
    var reorder by remember { mutableStateOf(product?.reorderLevel?.toString() ?: "5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (Rs)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                if (product == null) {
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Initial Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = reorder, onValueChange = { reorder = it }, label = { Text("Reorder Level") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && price.toIntOrNull() != null,
                onClick = { onConfirm(name, category, price.toIntOrNull() ?: 0, stock.toIntOrNull() ?: 0, reorder.toIntOrNull() ?: 0) }
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun StockActionDialog(
    product: InventoryProduct,
    type: InventoryTransactionType,
    onDismiss: () -> Unit,
    onConfirm: (qty: Int, reason: String) -> Unit
) {
    var qty by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${type.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} - ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Current Stock: ${product.stockQuantity}", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
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
                enabled = qty.toIntOrNull() != null && qty.toInt() > 0 && (type == InventoryTransactionType.RESTOCK || reason.isNotBlank()),
                onClick = { onConfirm(qty.toIntOrNull() ?: 0, reason) }
            ) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun Modifier.fillMaxWidth(value: Boolean): Modifier = if (value) this.fillMaxWidth() else this
