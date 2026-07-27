package com.royalcoffee.pos.ui.sales

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.model.PrinterConfig
import com.royalcoffee.pos.model.Product
import com.royalcoffee.pos.model.Receipt
import com.royalcoffee.pos.model.ReceiptItem
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.printer.FakeReceiptPrinter
import com.royalcoffee.pos.ui.payment.CashPaymentDialog
import com.royalcoffee.pos.ui.payment.ConfirmPaymentDialog
import com.royalcoffee.pos.ui.payment.PaymentMethodDialog
import com.royalcoffee.pos.ui.receipt.ReceiptPreviewDialog
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreenDark
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily

@Composable
fun SalesScreen(
    userRole: UserRole,
    onLogout: () -> Unit
) {
    /**
     * Context is used for showing short POS-style messages.
     *
     * Example:
     * "Printer not available. Check connection."
     */
    val context = LocalContext.current

    /**
     * Temporary product list.
     *
     * Later, this should come from the Products table in Room database.
     */
    val products = remember {
        sampleProducts()
    }

    /**
     * Temporary default printer configuration.
     *
     * Later, this should come from the Printers table.
     * For now, it represents a Bluetooth printer placeholder.
     */
    val defaultPrinter = remember {
        PrinterConfig.placeholderBluetoothPrinter()
    }

    /**
     * Temporary printer service.
     *
     * This does not connect to real hardware yet.
     * Later, this can be replaced with BluetoothReceiptPrinter.
     */
    val receiptPrinter = remember {
        FakeReceiptPrinter()
    }

    /**
     * Currently selected category in the POS menu.
     */
    var selectedCategory by remember {
        mutableStateOf("All")
    }

    /**
     * Builds the category list from product data.
     */
    val categories = remember {
        listOf("All") + products.map { it.category }.distinct()
    }

    /**
     * Filters products based on selected category.
     */
    val filteredProducts = if (selectedCategory == "All") {
        products
    } else {
        products.filter { it.category == selectedCategory }
    }

    /**
     * Live cart for the current order.
     */
    val cartItems = remember {
        mutableStateListOf<CartItem>()
    }

    /**
     * Controls the payment method selection dialog.
     */
    var showPaymentDialog by remember {
        mutableStateOf(false)
    }

    /**
     * Stores the selected payment method while its confirmation dialog is open.
     */
    var activePaymentMethod by remember {
        mutableStateOf<PaymentMethod?>(null)
    }

    /**
     * Stores the payment method after payment is confirmed.
     *
     * When this is not null:
     * - The ticket button changes from Charge to View Receipt.
     * - The order becomes locked so products and quantities cannot be changed.
     */
    var confirmedPaymentMethod by remember {
        mutableStateOf<PaymentMethod?>(null)
    }

    /**
     * Amount received from customer.
     *
     * Mainly useful for cash payments.
     */
    var amountPaid by remember {
        mutableStateOf<Int?>(null)
    }

    /**
     * Change given back to the customer.
     *
     * Mainly useful for cash payments.
     */
    var changeGiven by remember {
        mutableStateOf<Int?>(null)
    }

    /**
     * Controls whether the receipt preview dialog is visible.
     */
    var showReceiptPreviewDialog by remember {
        mutableStateOf(false)
    }

    /**
     * Temporary in-memory receipt number sequence.
     *
     * Later, this should come from the database so receipt numbers do not reset
     * when the app restarts.
     */
    var nextReceiptSequenceNumber by remember {
        mutableIntStateOf(1)
    }

    /**
     * Holds the currently generated receipt.
     *
     * This prevents the receipt number and timestamp from changing when
     * the user closes and reopens the receipt preview.
     */
    var currentReceipt by remember {
        mutableStateOf<Receipt?>(null)
    }

    /**
     * Controls the cancel order confirmation dialog.
     *
     * We ask for confirmation because cancelling an order removes all current
     * cart, payment, and receipt data for the active order.
     */
    var showCancelOrderDialog by remember {
        mutableStateOf(false)
    }

    /**
     * Basic order calculations.
     *
     * For now:
     * - Discount is fixed at 0.
     * - Tax is 15% of subtotal.
     */
    val subtotal = cartItems.sumOf { it.lineTotal }
    val discount = 0
    val tax = (subtotal * 0.15).toInt()
    val total = subtotal - discount + tax

    /**
     * Once payment is confirmed, the order should not be edited anymore.
     *
     * This prevents the cashier from accidentally changing a paid order.
     */
    val isOrderLocked = confirmedPaymentMethod != null

    /**
     * Adds a product to the cart.
     *
     * If the product is already in the cart, its quantity increases.
     * Otherwise, a new cart item is added.
     */
    fun addProductToCart(product: Product) {
        val existingItemIndex = cartItems.indexOfFirst {
            it.product.id == product.id
        }

        if (existingItemIndex >= 0) {
            val existingItem = cartItems[existingItemIndex]

            cartItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + 1
            )
        } else {
            cartItems.add(
                CartItem(
                    product = product,
                    quantity = 1
                )
            )
        }
    }

    /**
     * Increases the quantity of a cart item.
     */
    fun increaseQuantity(product: Product) {
        val itemIndex = cartItems.indexOfFirst {
            it.product.id == product.id
        }

        if (itemIndex >= 0) {
            val item = cartItems[itemIndex]

            cartItems[itemIndex] = item.copy(
                quantity = item.quantity + 1
            )
        }
    }

    /**
     * Decreases the quantity of a cart item.
     *
     * If quantity reaches 0, the item is removed.
     */
    fun decreaseQuantity(product: Product) {
        val itemIndex = cartItems.indexOfFirst {
            it.product.id == product.id
        }

        if (itemIndex >= 0) {
            val item = cartItems[itemIndex]

            if (item.quantity > 1) {
                cartItems[itemIndex] = item.copy(
                    quantity = item.quantity - 1
                )
            } else {
                cartItems.removeAt(itemIndex)
            }
        }
    }

    /**
     * Clears only the ticket/cart before payment.
     *
     * This is useful when the cashier selected the wrong products
     * but has not charged the customer yet.
     */
    fun clearTicketBeforePayment() {
        cartItems.clear()
    }

    /**
     * Returns true when there is anything active in the current order.
     *
     * This helps us decide whether Cancel Order should be enabled.
     */
    fun hasActiveOrder(): Boolean {
        return cartItems.isNotEmpty() ||
                confirmedPaymentMethod != null ||
                currentReceipt != null
    }

    /**
     * Creates the receipt object for the current paid order and opens the receipt preview.
     *
     * Important:
     * - This function receives the payment details directly.
     * - This avoids waiting for Compose state updates before generating the receipt.
     * - The receipt is created only once per order.
     */
    fun prepareReceiptPreview(
        paymentMethod: PaymentMethod,
        paid: Int?,
        change: Int?
    ) {
        if (currentReceipt == null) {
            val now = LocalDateTime.now()

            /**
             * Convert live cart items into finalized receipt items.
             *
             * ReceiptItem is separate from CartItem because a receipt should
             * represent the final transaction exactly as it happened.
             */
            val receiptItems = cartItems.map { cartItem ->
                ReceiptItem(
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.product.price
                )
            }

            currentReceipt = Receipt(
                receiptNumber = generateReceiptNumber(
                    sequenceNumber = nextReceiptSequenceNumber,
                    year = now.year
                ),
                receiptDateTime = formatReceiptDateTime(now),

                /**
                 * Company information shown on the receipt.
                 *
                 * Later, these values will come from the Company table.
                 */
                storeName = "Royal Coffee",
                storeAddressLines = listOf(
                    "Phoenix",
                    "Mauritius"
                ),
                storePhone = "+230 0000 0000",

                /**
                 * Temporary cashier name.
                 *
                 * Later, this should come from the logged-in user record.
                 */
                cashierName = when (userRole) {
                    UserRole.ADMIN -> "Admin"
                    UserRole.BARISTA -> "Barista"
                },

                items = receiptItems,
                subtotal = subtotal,
                discount = discount,
                tax = tax,
                total = total,
                paymentMethod = paymentMethod,
                amountPaid = paid,
                changeGiven = change,
                footerMessage = "Thank you for your purchase."
            )
        }

        /**
         * Open the receipt preview immediately.
         */
        showReceiptPreviewDialog = true
    }

    /**
     * Clears the current order and prepares the POS screen for the next customer.
     *
     * This is called only when the user presses Next Order.
     */
    fun clearCurrentOrder() {
        cartItems.clear()

        confirmedPaymentMethod = null
        amountPaid = null
        changeGiven = null
        activePaymentMethod = null
        showPaymentDialog = false

        showReceiptPreviewDialog = false
        showCancelOrderDialog = false
        currentReceipt = null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                /**
                 * Keeps the POS UI away from Android status bar and navigation bar.
                 */
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            /**
             * Dynamic layout sizing for different tablet sizes.
             */
            val sideNavWidth = (maxWidth * 0.08f).coerceIn(
                minimumValue = 104.dp,
                maximumValue = 132.dp
            )
            // Ticket panel
            val ticketPanelWidth = (maxWidth * 0.26f).coerceIn(
                minimumValue = 290.dp,
                maximumValue = 390.dp
            )
            // Product Card
            val productCardMinWidth = (maxWidth * 0.12f).coerceIn(
                minimumValue = 130.dp,
                maximumValue = 180.dp
            )

            val productAreaPadding = (maxWidth * 0.015f).coerceIn(
                minimumValue = 14.dp,
                maximumValue = 24.dp
            )

            val ticketPanelPadding = (maxWidth * 0.015f).coerceIn(
                minimumValue = 14.dp,
                maximumValue = 24.dp
            )

            val navLogoSize = when {
                maxHeight < 650.dp -> 46.dp
                else -> 56.dp
            }

            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                SideNavigation(
                    userRole = userRole,
                    onLogout = onLogout,
                    logoSize = navLogoSize,
                    modifier = Modifier.width(sideNavWidth)
                )

                ProductArea(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    products = filteredProducts,
                    productCardMinWidth = productCardMinWidth,
                    contentPadding = productAreaPadding,
                    canCancelOrder = hasActiveOrder(),
                    isOrderLocked = isOrderLocked,
                    onCancelOrderClick = {
                        showCancelOrderDialog = true
                    },
                    onCategoryClick = { category ->
                        selectedCategory = category
                    },
                    onProductClick = { product ->
                        addProductToCart(product)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
                    onIncreaseQuantity = { product ->
                        increaseQuantity(product)
                    },
                    onDecreaseQuantity = { product ->
                        decreaseQuantity(product)
                    },
                    onClearTicketClick = {
                        clearTicketBeforePayment()
                    },
                    onChargeClick = {
                        showPaymentDialog = true
                    },
                    onViewReceiptClick = {
                        val paymentMethod = confirmedPaymentMethod

                        if (paymentMethod != null) {
                            prepareReceiptPreview(
                                paymentMethod = paymentMethod,
                                paid = amountPaid,
                                change = changeGiven
                            )
                        }
                    },
                    modifier = Modifier
                        .width(ticketPanelWidth)
                        .fillMaxHeight()
                )
            }
        }
    }

    /**
     * Payment method selection dialog.
     */
    if (showPaymentDialog) {
        PaymentMethodDialog(
            total = total,
            onPaymentMethodSelected = { method ->
                activePaymentMethod = method
                showPaymentDialog = false
            },
            onDismiss = {
                showPaymentDialog = false
            }
        )
    }

    /**
     * Payment confirmation dialogs.
     */
    when (activePaymentMethod) {
        PaymentMethod.CASH -> {
            CashPaymentDialog(
                total = total,
                onConfirmPayment = { method, paid, change ->
                    confirmedPaymentMethod = method
                    amountPaid = paid
                    changeGiven = change
                    activePaymentMethod = null

                    /*
                    * Open the receipt preview immediately after payment confirmation
                     */
                    prepareReceiptPreview(
                        paymentMethod = method,
                        paid = paid,
                        change = change
                    )
                },
                onCancel = {
                    activePaymentMethod = null
                }
            )
        }

        PaymentMethod.CARD,
        PaymentMethod.MOBILE -> {
            ConfirmPaymentDialog(
                paymentMethod = activePaymentMethod!!,
                total = total,
                onConfirmPayment = { method, paid, change ->
                    confirmedPaymentMethod = method
                    amountPaid = paid
                    changeGiven = change
                    activePaymentMethod = null

                    /*
                    * Open the receipt preview immediately after payment confirmation
                     */
                    prepareReceiptPreview(
                        paymentMethod = method,
                        paid = paid,
                        change = change
                    )
                },
                onCancel = {
                    activePaymentMethod = null
                }
            )
        }

        null -> {
            // No payment dialog is currently open.
        }
    }

    /**
     * Receipt preview dialog.
     *
     * The dialog receives one Receipt object and delegates printing back
     * to this screen using onPrintReceipt.
     */
    val receiptToPreview = currentReceipt

    /**
     * Cancel order confirmation dialog.
     *
     * This prevents the cashier from accidentally clearing an active order.
     */
    if (showCancelOrderDialog) {
        AlertDialog(
            onDismissRequest = {
                showCancelOrderDialog = false
            },
            title = {
                Text(text = "Cancel Order?")
            },
            text = {
                Text(
                    text = "This will clear the current ticket and cancel the active order."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        clearCurrentOrder()
                        showCancelOrderDialog = false
                    }
                ) {
                    Text(text = "Cancel Order")

                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCancelOrderDialog = false
                    }
                ) {
                    Text(text = "Keep Order")
                }
            }
        )
    }

    if (showReceiptPreviewDialog && receiptToPreview != null) {
        ReceiptPreviewDialog(
            receipt = receiptToPreview,
            onClose = {
                /**
                 * Closing the preview does not clear the order.
                 */
                showReceiptPreviewDialog = false
            },
            onNextOrder = {
                /**
                 * Receipt sequence increases only when the order is finalized.
                 */
                nextReceiptSequenceNumber += 1
                clearCurrentOrder()
            },
            onPrintReceipt = { receipt ->
                /**
                 * Send the receipt to the printer service layer.
                 *
                 * FakeReceiptPrinter currently returns:
                 * "Printer not available. Check connection."
                 */
                val printResult = receiptPrinter.printReceipt(
                    receipt = receipt,
                    printerConfig = defaultPrinter
                )

                Toast.makeText(
                    context,
                    printResult.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
private fun SideNavigation(
    userRole: UserRole,
    onLogout: () -> Unit,
    logoSize: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(RoyalGreenDark)
            .padding(horizontal = 10.dp, vertical = 12.dp),
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
                modifier = Modifier
                    .size(logoSize)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = "RC",
                    color = RoyalGold,
                    fontFamily = CinzelFontFamily,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            NavigationItem(text = "Sales")

            if (userRole == UserRole.ADMIN) {
                NavigationItem(text = "Dashboard")
                NavigationItem(text = "Inventory")
            }

            NavigationItem(text = "Settings")
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Logout",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun NavigationItem(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = CinzelFontFamily,
                )
        }
    }
}

@Composable
private fun ProductArea(
    categories: List<String>,
    selectedCategory: String,
    products: List<Product>,
    productCardMinWidth: Dp,
    contentPadding: Dp,
    canCancelOrder: Boolean,
    isOrderLocked: Boolean,
    onCancelOrderClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(contentPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(140.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "sales",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Select products from the menu",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedButton(
                onClick = onCancelOrderClick,
                enabled = canCancelOrder,
                modifier = Modifier
                    .width(140.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Cancel Order",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        /**
         * Categories scroll horizontally when there is not enough space.
         */
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            lazyRowItems(categories) { category ->
                CategoryChip(
                    text = category,
                    isSelected = category == selectedCategory,
                    onClick = {
                        onCategoryClick(category)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        /**
         * Adaptive grid automatically changes number of product columns
         * depending on available width.
         */
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = productCardMinWidth),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    enabled = !isOrderLocked,
                    onClick = {
                        onProductClick(product)
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (isSelected) {
        Button(
            onClick = onClick
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick
        ) {
            Text(text = text)
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            /**
             * Visually dims product cards after payment confirmation.
             *
             * This shows the cashier that the paid order can no longer be edited.
             */
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(
                enabled = enabled
            ) {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /**
             * Product name is now the main focus of the card.
             *
             * Product images have been removed in Version 1 to keep the
             * Sales screen simpler and easier to maintain.
             */
            Text(
                text = product.name,
                textAlign = TextAlign.Center,
                fontFamily = CinzelFontFamily,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                /*color = MaterialTheme.colorScheme.onSurface,*/
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            /**
             * Category gives the cashier context when many products are shown.
             */
            Text(
                text = product.category,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            /**
             * Price is shown clearly at the bottom of the card.
             */
            Text(
                text = "Rs ${product.price}",
                textAlign = TextAlign.Center,
                fontFamily = CinzelFontFamily,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
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
    onClearTicketClick: () -> Unit,
    onChargeClick: () -> Unit,
    onViewReceiptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(contentPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            TicketHeader(
                itemCount = cartItems.sumOf { it.quantity }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (cartItems.isEmpty()) {
                EmptyTicketCard()
            } else {
                cartItems.forEach { item ->
                    CartItemRow(
                        cartItem = item,
                        isOrderLocked = isOrderLocked,
                        onIncreaseQuantity = {
                            onIncreaseQuantity(item.product)
                        },
                        onDecreaseQuantity = {
                            onDecreaseQuantity(item.product)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OrderSummaryCard(
                subtotal = subtotal,
                discount = discount,
                tax = tax,
                total = total
            )

            if (confirmedPaymentMethod != null) {
                Spacer(modifier = Modifier.height(16.dp))

                PaymentSummaryCard(
                    confirmedPaymentMethod = confirmedPaymentMethod,
                    amountPaid = amountPaid,
                    changeGiven = changeGiven
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (confirmedPaymentMethod == null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClearTicketClick,
                    enabled = cartItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Clear Ticket",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Button(
                    onClick = onChargeClick,
                    enabled = cartItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Charge Rs $total",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        } else {
            Button(
                onClick = onViewReceiptClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "View Receipt",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun TicketHeader(
    itemCount: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ticket",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Current Order",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = RoyalGold.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = "$itemCount item(s)",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    }
}

@Composable
private fun EmptyTicketCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No items added yet",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Select products from the menu to start an order.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CartItemRow(
    cartItem: CartItem,
    isOrderLocked: Boolean,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.55f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cartItem.product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Rs ${cartItem.product.price} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                Text(
                    text = "Rs ${cartItem.lineTotal}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Qty ${cartItem.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuantityButton(
                        text = "-",
                        enabled = !isOrderLocked,
                        onClick = onDecreaseQuantity
                    )

                    QuantityButton(
                        text = "+",
                        enabled = !isOrderLocked,
                        onClick = onIncreaseQuantity
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun OrderSummaryCard(
    subtotal: Int,
    discount: Int,
    tax: Int,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = RoyalGold.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(
                label = "Subtotal",
                value = "Rs $subtotal"
            )

            SummaryRow(
                label = "Discount",
                value = "Rs $discount"
            )

            SummaryRow(
                label = "Tax",
                value = "Rs $tax"
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Rs $total",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentSummaryCard(
    confirmedPaymentMethod: PaymentMethod,
    amountPaid: Int?,
    changeGiven: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment Confirmed",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            SummaryRow(
                label = "Method",
                value = confirmedPaymentMethod.name
            )

            if (amountPaid != null) {
                SummaryRow(
                    label = "Amount Paid",
                    value = "Rs $amountPaid"
                )
            }

            if (changeGiven != null && changeGiven > 0) {
                SummaryRow(
                    label = "Change",
                    value = "Rs $changeGiven"
                )
            }
        }
    }
}

/**
 * Temporary hardcoded products for development.
 *
 * Later, these products will come from the local Room database.
 */
private fun sampleProducts(): List<Product> {
    return listOf(
        Product(
            id = 1,
            name = "Latte",
            category = "Coffee",
            price = 120
        ),
        Product(
            id = 2,
            name = "Espresso",
            category = "Coffee",
            price = 95
        ),
        Product(
            id = 3,
            name = "Croissant",
            category = "Pastries",
            price = 80
        ),
        Product(
            id = 4,
            name = "Mocha",
            category = "Coffee",
            price = 135
        ),
        Product(
            id = 5,
            name = "Iced Coffee",
            category = "Cold",
            price = 140
        ),
        Product(
            id = 6,
            name = "Muffin",
            category = "Pastries",
            price = 75
        ),
        Product(
            id = 7,
            name = "Hot Chocolate",
            category = "Hot",
            price = 110
        )
    )
}

/**
 * Generates a temporary receipt number.
 *
 * Example:
 * sequenceNumber = 1
 * year = 2026
 * result = RC-2026-000001
 *
 * Later, this should be generated from the database.
 */
private fun generateReceiptNumber(
    sequenceNumber: Int,
    year: Int
): String {
    val paddedNumber = sequenceNumber.toString().padStart(6, '0')
    return "RC-$year-$paddedNumber"
}

/**
 * Formats the current date/time for the receipt.
 *
 * Example:
 * 16/07/2026 14:35
 */
private fun formatReceiptDateTime(
    dateTime: LocalDateTime
): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return dateTime.format(formatter)
}