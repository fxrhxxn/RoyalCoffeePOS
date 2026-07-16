package com.royalcoffee.pos.ui.sales

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.model.Product
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.payment.CashPaymentDialog
import com.royalcoffee.pos.ui.payment.ConfirmPaymentDialog
import com.royalcoffee.pos.ui.payment.PaymentMethodDialog
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreenDark
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SalesScreen(
    userRole: UserRole,
    onLogout: () -> Unit
) {
    val products = remember {
        sampleProducts()
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    val categories = remember {
        listOf("All") + products.map { it.category }.distinct()
    }

    val filteredProducts = if (selectedCategory == "All") {
        products
    } else {
        products.filter { it.category == selectedCategory }
    }

    val cartItems = remember {
        mutableStateListOf<CartItem>()
    }

    var showPaymentDialog by remember {
        mutableStateOf(false)
    }

    var activePaymentMethod by remember {
        mutableStateOf<PaymentMethod?>(null)
    }

    var confirmedPaymentMethod by remember {
        mutableStateOf<PaymentMethod?>(null)
    }

    var amountPaid by remember {
        mutableStateOf<Int?>(null)
    }

    var changeGiven by remember {
        mutableStateOf<Int?>(null)
    }

    val subtotal = cartItems.sumOf { it.lineTotal }
    val discount = 0
    val tax = (subtotal * 0.15).toInt()
    val total = subtotal - discount + tax

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val sideNavWidth = (maxWidth * 0.08f).coerceIn(
                minimumValue = 104.dp,
                maximumValue = 132.dp
            )

            val ticketPanelWidth = (maxWidth * 0.26f).coerceIn(
                minimumValue = 290.dp,
                maximumValue = 390.dp
            )

            val productCardMinWidth = (maxWidth * 0.14f).coerceIn(
                minimumValue = 150.dp,
                maximumValue = 210.dp
            )

            val productAreaPadding = (maxWidth * 0.015f).coerceIn(
                minimumValue = 14.dp,
                maximumValue = 24.dp
            )

            val ticketPanelPadding = (maxWidth * 0.015f).coerceIn(
                minimumValue = 14.dp,
                maximumValue = 24.dp
            )

            val productImageVerticalPadding = when {
                maxHeight < 650.dp -> 22.dp
                maxHeight < 800.dp -> 28.dp
                else -> 32.dp
            }

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
                    productImageVerticalPadding = productImageVerticalPadding,
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
                    onIncreaseQuantity = { product ->
                        increaseQuantity(product)
                    },
                    onDecreaseQuantity = { product ->
                        decreaseQuantity(product)
                    },
                    onChargeClick = {
                        showPaymentDialog = true
                    },
                    modifier = Modifier
                        .width(ticketPanelWidth)
                        .fillMaxHeight()
                )
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
            onDismiss = {
                showPaymentDialog = false
            }
        )
    }

    when (activePaymentMethod) {
        PaymentMethod.CASH -> {
            CashPaymentDialog(
                total = total,
                onConfirmPayment = { method, paid, change ->
                    confirmedPaymentMethod = method
                    amountPaid = paid
                    changeGiven = change
                    activePaymentMethod = null
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
                style = MaterialTheme.typography.bodySmall
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
    productImageVerticalPadding: Dp,
    onCategoryClick: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(contentPadding)
    ) {
        Text(
            text = "Sales",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())

        Text(
            text = "Select products from the menu",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = productCardMinWidth),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    imageVerticalPadding = productImageVerticalPadding,
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
    imageVerticalPadding: Dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = imageVerticalPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Image")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Rs ${product.price}",
                style = MaterialTheme.typography.bodyMedium
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
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    onChargeClick: () -> Unit,
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

        Button(
            onClick = onChargeClick,
            enabled = cartItems.isNotEmpty() && confirmedPaymentMethod == null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (confirmedPaymentMethod == null) {
                Text(
                    text = "Charge Rs $total",
                    style = MaterialTheme.typography.titleSmall
                )
            } else {
                Text(
                    text = "Payment Confirmed",
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun CartItemRow(
    cartItem: CartItem,
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
                        onClick = onDecreaseQuantity
                    )

                    QuantityButton(
                        text = "+",
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
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
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