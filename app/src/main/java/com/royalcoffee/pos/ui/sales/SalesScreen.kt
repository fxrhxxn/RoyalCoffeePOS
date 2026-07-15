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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.model.CartItem
import com.royalcoffee.pos.model.Product
import com.royalcoffee.pos.model.UserRole
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreenDark

@Composable
fun SalesScreen(
    userRole: UserRole,
    onLogout: () -> Unit
) {
    val products = remember {
        sampleProducts()
    }

    val cartItems = remember {
        mutableStateListOf<CartItem>()
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
                products = products,
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
                onIncreaseQuantity = { product ->
                    increaseQuantity(product)
                },
                onDecreaseQuantity = { product ->
                    decreaseQuantity(product)
                },
                modifier = Modifier
                    .width(340.dp)
                    .fillMaxHeight()
            )
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
            Text(
                text = "RC",
                color = RoyalGold,
                style = MaterialTheme.typography.headlineSmall
            )

            NavigationItem(text = "Sales")

            if (userRole == UserRole.ADMIN) {
                NavigationItem(text = "Dashboard")
                NavigationItem(text = "Inventory")
            }

            NavigationItem(text = "Settings")
        }

        OutlinedButton(
            onClick = onLogout
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
    products: List<Product>,
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
            CategoryChip("Coffee")
            CategoryChip("Cold")
            CategoryChip("Hot")
            CategoryChip("Pastries")
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
private fun CategoryChip(text: String) {
    OutlinedButton(
        onClick = {
            // Category filtering will be added later.
        }
    ) {
        Text(text = text)
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
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
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
        }

        Button(
            onClick = {
                // Payment screen will be added later.
            },
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Charge Rs $total")
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

// sample products to test
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
        )
    )
}