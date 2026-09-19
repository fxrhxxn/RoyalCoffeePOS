# Implementation Plan - Sales/POS Screen Improvement

Improve the Sales/POS screen to include search, category filtering, better product grid with stock information, and improved cart management.

## User Review Required

> [!IMPORTANT]
> The cart logic and filtering state will be moved from `SalesScreen.kt` to `SalesViewModel.kt` for better state management and to support the new requirements (search, category + search interaction, stock limits).

## Proposed Changes

### Data Models

#### [MODIFY] [Product.kt](file:///C:/AndroidProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/model/Product.kt)
- Add `stockQuantity: Int` and `reorderLevel: Int` to the `Product` data class to support stock display and low-stock warnings on product cards.

### UI Layers

#### [MODIFY] [ProductViewModel.kt](file:///C:/AndroidProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/ui/product/ProductViewModel.kt)
- Update `toProduct()` mapping to include `stockQuantity` and `reorderLevel`.

#### [MODIFY] [SalesViewModel.kt](file:///C:/AndroidProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/ui/sales/SalesViewModel.kt)
- Add `searchText` and `selectedCategory` states.
- Move `cartItems` from `SalesScreen` to `SalesViewModel`.
- Implement filtering logic combining search and category.
- Implement stock-aware cart operations:
    - `addProduct(product)`: Check if `stockQuantity > 0` and current quantity in cart < `stockQuantity`.
    - `increaseQuantity(product)`: Limit to `stockQuantity`.
    - `decreaseQuantity(product)`: Remove if quantity becomes 0.
    - `removeItem(product)`: Remove item immediately.
    - `clearCart()`: Clear all items.
- Expose calculated totals (`subtotal`, `tax`, `total`).
- Ensure compatibility with existing `processCompletedOrder` flow.

#### [MODIFY] [SalesScreen.kt](file:///C:/AndroidProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/ui/sales/SalesScreen.kt)
- Add a Search Bar above the product grid.
- Update `ProductArea` to use the filtered products from `SalesViewModel`.
- Update `ProductCard`:
    - Add fixed image placeholder area.
    - Display stock status (available, low stock, out of stock).
    - Disable clicks if out of stock.
- Update `TicketPanel` & `CartItemRow`:
    - Add "Remove" button with trash icon.
    - Improve layout for readability.
    - Show empty state message when cart is empty.
- Add "Clear Ticket" confirmation dialog.
- Integrate with the improved `SalesViewModel`.

## Verification Plan

### Automated Tests
- Build and run the app to ensure no compilation errors.

### Manual Verification
- **Product display**: Confirm products from Room are visible.
- **Category filtering**: Test switching categories.
- **Search**: Test dynamic search (case-insensitive).
- **Search + Category**: Test both working together.
- **Cart operations**: Test add, +, -, remove.
- **Stock limits**: Verify quantity cannot exceed `stockQuantity`.
- **Out of stock**: Verify 0 stock products show "OUT OF STOCK" and are disabled.
- **Clear ticket**: Verify confirmation dialog and clearing functionality.
- **Payment flow**: Complete an order and verify it reaches the existing print/preview stage.
- **Tablet UI**: Ensure layout remains responsive and fits tablet dimensions without clipping.
