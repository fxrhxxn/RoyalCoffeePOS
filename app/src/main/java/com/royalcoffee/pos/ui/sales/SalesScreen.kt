package com.royalcoffee.pos.ui.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.PaymentMethod
import com.royalcoffee.pos.model.Product
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.payment.PaymentMethodDialog
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreenDark
import com.royalcoffee.pos.ui.payment.CashPaymentDialog
import com.royalcoffee.pos.ui.payment.ConfirmPaymentDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp

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
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            SideNavigation(
                userRole = userRole,
                onLogout = onLogout
            )

            ProductArea(
                categories = categories,
                selectedCategory = selectedCategory,
                products = filteredProducts,
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
                    .width(340.dp)
                    .fillMaxHeight()
            )
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
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .fillMaxHeight()
            .background(RoyalGreenDark)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp) // Total size of the circle
                    .border(width = 2.dp, color = Color.White, shape = CircleShape) // White border
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
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text(text = "Logout")

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
                .padding(vertical = 12.dp),
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
    onCategoryClick: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = "Sales / POS",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Select products from the menu",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                CategoryChip(
                    text = category,
                    isSelected = category == selectedCategory,
                    onClick = {
                        onCategoryClick(category)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
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
                    .padding(vertical = 32.dp),
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
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    onChargeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Ticket",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (cartItems.isEmpty()) {
                Text(text = "No items added yet")
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

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Subtotal: Rs $subtotal")
            Text(text = "Discount: Rs $discount")
            Text(text = "Tax: Rs $tax")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total: Rs $total",
                style = MaterialTheme.typography.titleLarge
            )

            if (confirmedPaymentMethod != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Payment confirmed: ${confirmedPaymentMethod.name}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (amountPaid != null) {
                    Text(
                        text = "Amount paid: Rs $amountPaid",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (changeGiven != null && changeGiven > 0) {
                    Text(
                        text = "Change: Rs $changeGiven",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Button(
            onClick = onChargeClick,
            enabled = cartItems.isNotEmpty() && confirmedPaymentMethod == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (confirmedPaymentMethod == null) {
                Text(text = "Charge Rs $total")
            } else {
                Text(text = "Payment Confirmed")
            }
        }

    }
}

@Composable
private fun CartItemRow(
    cartItem: CartItem,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = cartItem.product.name,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = "Qty ${cartItem.quantity} x Rs ${cartItem.product.price}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Rs ${cartItem.lineTotal}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onDecreaseQuantity
            ) {
                Text(text = "-")
            }

            TextButton(
                onClick = onIncreaseQuantity
            ) {
                Text(text = "+")
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