package com.royalcoffee.pos.ui.sales

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.royalcoffee.pos.R
import com.royalcoffee.pos.data.local.database.RoyalCoffeeDatabaseProvider
import com.royalcoffee.pos.data.local.repository.ProductRepository
import com.royalcoffee.pos.data.local.repository.ReceiptRepository
import com.royalcoffee.pos.model.*
import com.royalcoffee.pos.printer.FakeReceiptPrinter
import com.royalcoffee.pos.ui.SettingsViewModel
import com.royalcoffee.pos.ui.SettingsViewModelFactory
import com.royalcoffee.pos.ui.dashboard.DashboardScreen
import com.royalcoffee.pos.ui.dashboard.DashboardViewModel
import com.royalcoffee.pos.ui.dashboard.DashboardViewModelFactory
import com.royalcoffee.pos.ui.inventory.InventoryScreen
import com.royalcoffee.pos.ui.payment.CashPaymentDialog
import com.royalcoffee.pos.ui.payment.ConfirmPaymentDialog
import com.royalcoffee.pos.ui.payment.PaymentMethodDialog
import com.royalcoffee.pos.ui.product.ProductViewModel
import com.royalcoffee.pos.ui.product.ProductViewModelFactory
import com.royalcoffee.pos.ui.receipt.ReceiptHistoryScreen
import com.royalcoffee.pos.ui.receipt.ReceiptHistoryViewModel
import com.royalcoffee.pos.ui.receipt.ReceiptHistoryViewModelFactory
import com.royalcoffee.pos.ui.receipt.ReceiptPreviewDialog
import com.royalcoffee.pos.util.ReceiptPdfGenerator
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.royalcoffee.pos.ui.settings.SettingsScreen
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreenDark
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.royalcoffee.pos.data.local.repository.CategoryRepository
import com.royalcoffee.pos.data.local.dao.CategoryDao

@Composable
fun SalesScreen(
    userRole: UserRole,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val database = remember(context) {
        RoyalCoffeeDatabaseProvider.getDatabase(context)
    }

    val productRepository = remember(database) {
        ProductRepository(
            database = database,
            productDao = database.productDao(),
            inventoryHistoryDao = database.inventoryHistoryDao()
        )
    }

    val categoryRepository = remember(database) {
        CategoryRepository(
            database.CategoryDao()
        )
    }

    val userRepository = remember(database) {
        com.royalcoffee.pos.data.local.repository.UserRepository(database.userDao())
    }

    val businessRepository = remember(database) {
        com.royalcoffee.pos.data.local.repository.BusinessRepository(database.businessDao())
    }

    val receiptRepository = remember(database) {
        ReceiptRepository(
            database = database,
            receiptDao = database.receiptDao(),
            productDao = database.productDao(),
            inventoryHistoryDao = database.inventoryHistoryDao()
        )
    }

    val receiptHistoryViewModel: ReceiptHistoryViewModel = viewModel(
        factory = ReceiptHistoryViewModelFactory(receiptRepository = receiptRepository)
    )
    val receiptHistoryItems by receiptHistoryViewModel.receiptHistory.collectAsStateWithLifecycle()

    val salesViewModel: SalesViewModel = viewModel(
        factory = SalesViewModelFactory(receiptRepository = receiptRepository)
    )

    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(productRepository = productRepository)
    )
    val products by productViewModel.products.collectAsStateWithLifecycle()

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            receiptRepository = receiptRepository,
            productRepository = productRepository
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            userRepository = userRepository,
            businessRepository = businessRepository
        )
    )

    val defaultPrinter = remember { PrinterConfig.placeholderBluetoothPrinter() }
    val receiptPrinter = remember { FakeReceiptPrinter() }

    var currentScreen by remember { mutableStateOf(AppScreen.SALES) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var receiptFooterText by remember { mutableStateOf("Thank you for your purchase.") }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }

    val searchText by salesViewModel.searchText.collectAsStateWithLifecycle()
    val selectedCategory by salesViewModel.selectedCategory.collectAsStateWithLifecycle()
    val filteredProducts = remember(products, searchText, selectedCategory) {
        salesViewModel.filterProducts(products)
    }

    val cartItems = salesViewModel.cartItems
    val subtotal = salesViewModel.subtotal
    val discount = salesViewModel.discount
    val tax = salesViewModel.tax
    val total = salesViewModel.total

    var showPaymentDialog by remember { mutableStateOf(false) }
    var activePaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var confirmedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var amountPaid by remember { mutableStateOf<Int?>(null) }
    var changeGiven by remember { mutableStateOf<Int?>(null) }

    val salesError = salesViewModel.salesError
    LaunchedEffect(salesError) {
        if (salesError != null) {
            confirmedPaymentMethod = null
            amountPaid = null
            changeGiven = null
        }
    }

    var showCancelOrderDialog by remember { mutableStateOf(false) }
    var showClearTicketConfirmDialog by remember { mutableStateOf(false) }
    val isOrderLocked = confirmedPaymentMethod != null

    fun clearTicketBeforePayment() {
        salesViewModel.clearCart()
    }

    fun hasActiveOrder(): Boolean {
        return cartItems.isNotEmpty() || confirmedPaymentMethod != null || salesViewModel.currentReceipt != null
    }

    fun prepareReceiptPreview(paymentMethod: PaymentMethod, paid: Int?, change: Int?) {
        salesViewModel.processCompletedOrder(
            cartItems = cartItems,
            subtotal = subtotal,
            discount = discount,
            tax = tax,
            total = total,
            paymentMethod = paymentMethod,
            amountPaid = paid,
            changeGiven = change,
            cashierName = if (userRole == UserRole.ADMIN) "Admin" else "Barista",
            footerMessage = receiptFooterText
        )
    }

    fun clearCurrentOrder() {
        salesViewModel.clearCart()
        confirmedPaymentMethod = null
        amountPaid = null
        changeGiven = null
        activePaymentMethod = null
        showPaymentDialog = false
        salesViewModel.clearReceiptState()
        showCancelOrderDialog = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val sideNavWidth = (maxWidth * 0.08f).coerceIn(104.dp, 132.dp)
            val ticketPanelWidth = (maxWidth * 0.26f).coerceIn(290.dp, 390.dp)
            val productCardMinWidth = (maxWidth * 0.12f).coerceIn(130.dp, 180.dp)
            val productAreaPadding = (maxWidth * 0.015f).coerceIn(14.dp, 24.dp)
            val ticketPanelPadding = (maxWidth * 0.015f).coerceIn(14.dp, 24.dp)
            val navLogoSize = if (maxHeight < 650.dp) 46.dp else 56.dp

            Row(modifier = Modifier.fillMaxSize()) {
                SideNavigation(
                    userRole = userRole,
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    onLogout = onLogout,
                    logoSize = navLogoSize,
                    modifier = Modifier.width(sideNavWidth)
                )

                when (currentScreen) {
                    AppScreen.SALES -> {
                        ProductArea(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            searchText = searchText,
                            products = filteredProducts,
                            productCardMinWidth = productCardMinWidth,
                            contentPadding = productAreaPadding,
                            canCancelOrder = hasActiveOrder(),
                            isOrderLocked = isOrderLocked,
                            onSearchChange = { salesViewModel.updateSearchText(it) },
                            onCancelOrderClick = { showCancelOrderDialog = true },
                            onCategoryClick = { salesViewModel.updateCategory(it) },
                            onProductClick = { salesViewModel.addProductToCart(it) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )

                        VerticalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            thickness = 1.dp
                        )

                        TicketPanel(
                            cartItems = cartItems,
                            subtotal = subtotal,
                            discount = discount,
                            tax = tax,
                            total = total,
                            confirmedPaymentMethod = confirmedPaymentMethod,
                            amountPaid = amountPaid,
                            changeGiven = changeGiven,
                            contentPadding = ticketPanelPadding,
                            isOrderLocked = isOrderLocked,
                            onIncreaseQuantity = { salesViewModel.increaseQuantity(it) },
                            onDecreaseQuantity = { salesViewModel.decreaseQuantity(it) },
                            onRemoveItem = { salesViewModel.removeItem(it) },
                            onClearTicketClick = { showClearTicketConfirmDialog = true },
                            onChargeClick = { showPaymentDialog = true },
                            onViewReceiptClick = {
                                confirmedPaymentMethod?.let {
                                    prepareReceiptPreview(it, amountPaid, changeGiven)
                                }
                            },
                            modifier = Modifier.width(ticketPanelWidth).fillMaxHeight()
                        )
                    }
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    AppScreen.INVENTORY -> {
                        InventoryScreen(
                            userRole = userRole,
                            productRepository = productRepository,
                            categoryRepository = categoryRepository,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    AppScreen.RECEIPTS -> {
                        ReceiptHistoryScreen(
                            receipts = receiptHistoryItems,
                            selectedReceipt = receiptHistoryViewModel.selectedReceipt,
                            selectedReceiptDetails = receiptHistoryViewModel.selectedReceiptDetails,
                            isDetailDialogVisible = receiptHistoryViewModel.isReceiptDetailDialogVisible,
                            currentFilter = receiptHistoryViewModel.currentFilter,
                            customDate = receiptHistoryViewModel.customDate,
                            onReceiptClick = { receiptHistoryViewModel.openReceiptDetails(it) },
                            onCloseDetailDialog = { receiptHistoryViewModel.closeReceiptDetails() },
                            onDownloadReceipt = { receiptId ->
                                scope.launch {
                                    val fullReceipt = receiptRepository.getFullReceipt(receiptId)
                                    if (fullReceipt != null) {
                                        val uri = ReceiptPdfGenerator.generateAndSavePdf(context, fullReceipt)
                                        val msg = if (uri != null) "Saved: Receipt_${fullReceipt.receiptNumber}.pdf" else "Unable to save PDF."
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Error: Receipt data not found.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onFilterChange = { receiptHistoryViewModel.updateFilter(it) },
                            onCustomDateChange = { receiptHistoryViewModel.updateCustomDate(it) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            userRole = userRole,
                            viewModel = settingsViewModel,
                            selectedLanguage = selectedLanguage,
                            receiptFooterText = receiptFooterText,
                            onLanguageChange = { selectedLanguage = it },
                            onReceiptFooterChange = { receiptFooterText = it },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        PaymentMethodDialog(
            total = total,
            onPaymentMethodSelected = { method ->
                activePaymentMethod = method
                showPaymentDialog = false
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    when (val method = activePaymentMethod) {
        PaymentMethod.CASH -> {
            CashPaymentDialog(
                total = total,
                onConfirmPayment = { m, p, c ->
                    confirmedPaymentMethod = m
                    amountPaid = p
                    changeGiven = c
                    activePaymentMethod = null
                    prepareReceiptPreview(m, p, c)
                },
                onCancel = { activePaymentMethod = null }
            )
        }
        PaymentMethod.CARD, PaymentMethod.MOBILE -> {
            ConfirmPaymentDialog(
                paymentMethod = method,
                total = total,
                onConfirmPayment = { m, p, c ->
                    confirmedPaymentMethod = m
                    amountPaid = p
                    changeGiven = c
                    activePaymentMethod = null
                    prepareReceiptPreview(m, p, c)
                },
                onCancel = { activePaymentMethod = null }
            )
        }
        null -> {}
    }

    if (salesError != null) {
        AlertDialog(
            onDismissRequest = { salesViewModel.clearSalesError() },
            title = { Text("Order Error") },
            text = { Text(salesError) },
            confirmButton = {
                Button(onClick = { salesViewModel.clearSalesError() }) { Text("OK") }
            }
        )
    }

    if (showCancelOrderDialog) {
        AlertDialog(
            onDismissRequest = { showCancelOrderDialog = false },
            title = { Text("Cancel Order?") },
            text = { Text("This will clear the current ticket and cancel the active order.") },
            confirmButton = {
                Button(onClick = { clearCurrentOrder() }) { Text("Cancel Order") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelOrderDialog = false }) { Text("Keep Order") }
            }
        )
    }

    if (showClearTicketConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearTicketConfirmDialog = false },
            title = { Text("Clear current order?") },
            text = { Text("All items will be removed from the current ticket.") },
            confirmButton = {
                Button(
                    onClick = {
                        clearTicketBeforePayment()
                        showClearTicketConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear Order") }
            },
            dismissButton = {
                TextButton(onClick = { showClearTicketConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (salesViewModel.showReceiptPreviewDialog && salesViewModel.currentReceipt != null) {
        ReceiptPreviewDialog(
            receipt = salesViewModel.currentReceipt!!,
            onClose = { salesViewModel.closeReceiptPreview() },
            onNextOrder = { clearCurrentOrder() },
            onPrintReceipt = { receipt ->
                val res = receiptPrinter.printReceipt(receipt, defaultPrinter)
                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
            },
            onDownloadPdf = { receipt ->
                val uri = ReceiptPdfGenerator.generateAndSavePdf(context, receipt)
                val msg = if (uri != null) "Saved: Receipt_${receipt.receiptNumber}.pdf" else "Unable to save PDF."
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
private fun SideNavigation(
    userRole: UserRole,
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    onLogout: () -> Unit,
    logoSize: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight().background(RoyalGreenDark).padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(logoSize).border(2.dp, Color.White, CircleShape)
            ) {
                Text("RC", color = RoyalGold, fontFamily = CinzelFontFamily, style = MaterialTheme.typography.headlineSmall)
            }
            NavigationItem("Sales", currentScreen == AppScreen.SALES) { onScreenSelected(AppScreen.SALES) }
            if (userRole == UserRole.ADMIN) {
                NavigationItem("Dashboard", currentScreen == AppScreen.DASHBOARD) { onScreenSelected(AppScreen.DASHBOARD) }
                NavigationItem("Inventory", currentScreen == AppScreen.INVENTORY) { onScreenSelected(AppScreen.INVENTORY) }
            }
            NavigationItem("Receipts", currentScreen == AppScreen.RECEIPTS) { onScreenSelected(AppScreen.RECEIPTS) }
            NavigationItem("Settings", currentScreen == AppScreen.SETTINGS) { onScreenSelected(AppScreen.SETTINGS) }
        }
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("Logout", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NavigationItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) RoyalGold else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) RoyalGreenDark else MaterialTheme.colorScheme.onSurface

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = textColor, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ProductArea(
    categories: List<String>,
    selectedCategory: String,
    searchText: String,
    products: List<Product>,
    productCardMinWidth: Dp,
    contentPadding: Dp,
    canCancelOrder: Boolean,
    isOrderLocked: Boolean,
    onSearchChange: (String) -> Unit,
    onCancelOrderClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    Column(modifier = modifier.fillMaxHeight().padding(contentPadding)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(140.dp))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("royal coffee", fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text("Point of Sale", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            OutlinedButton(onClick = onCancelOrderClick, enabled = canCancelOrder, modifier = Modifier.width(140.dp).height(44.dp), shape = RoundedCornerShape(14.dp)) {
                Text("Cancel Order", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchChange,
                placeholder = { Text("Search products...") },
                modifier = Modifier.weight(1f).height(56.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary.copy(0.6f)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End), modifier = Modifier.weight(1.5f)) {
                lazyRowItems(categories) { category ->
                    if (category == selectedCategory) {
                        Button(onClick = { onCategoryClick(category) }) { Text(category) }
                    } else {
                        OutlinedButton(onClick = { onCategoryClick(category) }) { Text(category) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(productCardMinWidth),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 60.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { product ->
                    ProductCard(product, !isOrderLocked) { onProductClick(product) }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, enabled: Boolean, onClick: () -> Unit) {
    val isOutOfStock = product.stockQuantity <= 0
    val isLowStock = !isOutOfStock && product.stockQuantity <= product.reorderLevel
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (enabled && !isOutOfStock) 1f else 0.45f).clickable(enabled && !isOutOfStock) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(product.name, textAlign = TextAlign.Center, fontFamily = CinzelFontFamily, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Rs ${product.price}", fontFamily = CinzelFontFamily, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                if (isOutOfStock) {
                    Text("OUT OF STOCK", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(if (isLowStock) Color(0xFFFFA000) else Color(0xFF4CAF50), CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isLowStock) "${product.stockQuantity} left" else "${product.stockQuantity} available", style = MaterialTheme.typography.labelSmall, color = if (isLowStock) Color(0xFFFFA000) else MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketPanel(
    cartItems: List<CartItem>,
    subtotal: Int,
    discount: Int,
    tax: Int,
    total: Int,
    confirmedPaymentMethod: PaymentMethod?,
    amountPaid: Int?,
    changeGiven: Int?,
    contentPadding: Dp,
    isOrderLocked: Boolean,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    onRemoveItem: (Product) -> Unit,
    onClearTicketClick: () -> Unit,
    onChargeClick: () -> Unit,
    onViewReceiptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface).padding(contentPadding), verticalArrangement = Arrangement.SpaceBetween) {
        Box(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
                TicketHeader(cartItems.sumOf { it.quantity })
                Spacer(Modifier.height(16.dp))
                if (cartItems.isEmpty()) {
                    EmptyTicketCard()
                } else {
                    cartItems.forEach { item ->
                        CartItemRow(item, isOrderLocked, { onIncreaseQuantity(item.product) }, { onDecreaseQuantity(item.product) }, { onRemoveItem(item.product) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                OrderSummaryCard(subtotal, discount, tax, total)
                confirmedPaymentMethod?.let {
                    Spacer(Modifier.height(16.dp))
                    PaymentSummaryCard(it, amountPaid, changeGiven)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (confirmedPaymentMethod == null) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onClearTicketClick,
                    enabled = cartItems.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Clear Ticket") }
                
                Button(
                    onClick = onChargeClick,
                    enabled = cartItems.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Charge Rs $total") }
            }
        } else {
            Button(onViewReceiptClick, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) { Text("View Receipt") }
        }
    }
}

@Composable
private fun TicketHeader(itemCount: Int) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column {
            Text("Ticket", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text("Current Order", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.65f))
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(0.25f), border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(0.7f))) {
            Text("$itemCount item(s)", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EmptyTicketCard() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(0.55f))) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No items added yet", style = MaterialTheme.typography.titleSmall)
            Text("Select products from the menu to start an order.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, locked: Boolean, onInc: () -> Unit, onDec: () -> Unit, onRem: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp), RoundedCornerShape(16.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.background.copy(0.55f)), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.08f))) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(item.product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Rs ${item.product.price} each", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(
                    onClick = { onRem() },
                    enabled = !locked,
                    modifier = Modifier.size(24.dp)
                ) { Icon(Icons.Default.Delete, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error.copy(0.7f)) }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Rs ${item.lineTotal}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { onDec() },
                        enabled = !locked,
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("-", style = MaterialTheme.typography.titleMedium) }
                    
                    Text("${item.quantity}", fontWeight = FontWeight.Bold)
                    
                    TextButton(
                        onClick = { onInc() },
                        enabled = !locked && item.quantity < item.product.stockQuantity,
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("+", style = MaterialTheme.typography.titleMedium) }
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(subtotal: Int, discount: Int, tax: Int, total: Int) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(0.65f)), border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold.copy(0.35f))) {
        Column(Modifier.padding(16.dp)) {
            Text("Order Summary", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            SummaryRow("Subtotal", "Rs $subtotal")
            SummaryRow("Discount", "Rs $discount")
            SummaryRow("Tax", "Rs $tax")
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.onSurface.copy(0.12f))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Rs $total", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.72f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PaymentSummaryCard(method: PaymentMethod, paid: Int?, change: Int?) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.primary.copy(0.10f)), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.35f))) {
        Column(Modifier.padding(16.dp)) {
            Text("Payment Confirmed", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            SummaryRow("Method", method.name)
            paid?.let { SummaryRow("Amount Paid", "Rs $it") }
            change?.let { if (it > 0) SummaryRow("Change", "Rs $it") }
        }
    }
}


