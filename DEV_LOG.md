# Royal Coffee POS - Development Log

## Project Overview

Royal Coffee POS is a native Android tablet Point-of-Sale application designed for a café environment.

The application is being developed using:

- Kotlin
- Jetpack Compose
- Android Studio
- Material Design 3
- Room/SQLite later for local database storage
- Firebase later for cloud backup and synchronization

The app is designed for landscape orientation only and follows a local-first architecture. The goal is to allow café staff to process orders quickly, confirm payments, generate receipts, and later manage inventory, reports, settings, and cloud synchronization.

---

## Current Development Status

Last updated: After completing the receipt preview flow, fake printer service layer, ESC/POS formatter, and Bluetooth printer support skeleton.

The project currently builds and runs successfully on:

- Pixel Tablet emulator
- Android tablet test device

The main working module is currently the Sales/POS workflow.

Current working flow:

```text
Login
↓
Sales/POS Screen
↓
Add products to ticket
↓
Charge order
↓
Select payment method
↓
Confirm payment
↓
Receipt Preview
↓
Print / Download / Share / Next Order actions
Important Project Note

No actual database has been created yet.

The following database-related items have only been planned or designed conceptually:

Users table
Products table
Categories table
Inventory table
Orders / Receipt Header table
Order Items / Receipt Details table
Payments table
Notifications table
Settings table
Company table
Printers table

When database implementation begins, the app will be guided step by step using the proper Android tools, most likely:

Room + SQLite inside Android Studio

For now, the app uses temporary in-memory data and hardcoded sample products.

Completed Features
1. Android Project Setup

Completed:

Created Android Studio project
Configured project package as com.royalcoffee.pos
Used Kotlin as the programming language
Used Jetpack Compose for UI development
Configured Material Design 3
App successfully builds and runs
2. Landscape Orientation

Completed:

Configured the app to run in landscape orientation
Updated AndroidManifest.xml
App is intended for Android tablet POS usage
UI is being designed for a wide-screen POS layout

File involved:

app/src/main/AndroidManifest.xml
3. App Theme and Branding

Completed:

Created a custom Royal Coffee POS visual theme
Added royal green and gold color styling
Added softer background color for better visual comfort
Added premium café-style visual identity
Added Cinzel font for brand styling
Used Royal Coffee branding on login screen and receipt preview

Theme files involved:

app/src/main/java/com/royalcoffee/pos/ui/theme/Color.kt
app/src/main/java/com/royalcoffee/pos/ui/theme/Theme.kt
app/src/main/java/com/royalcoffee/pos/ui/theme/Type.kt

Current key colors include:

RoyalGreen
RoyalGreenDark
RoyalGold
RoyalGoldDark
CoffeeBrown
SoftCream
LightGoldBackground
ErrorRed
4. Login Screen

Completed:

Created login screen using Jetpack Compose
Added username and PIN input fields
Added fake/demo authentication for development
Added role-based login behavior
Added Admin and Barista demo users
Added premium login layout with Royal Coffee branding
Added coffee icon visual element
Added Cinzel font styling for the brand title
Improved responsiveness for tablet screens
Improved keyboard handling
Added numeric PIN entry behavior

Demo users currently used:

admin / 1234
barista / 1234

Login files involved:

app/src/main/java/com/royalcoffee/pos/ui/login/LoginScreen.kt
5. User Roles

Completed:

Created UserRole enum
Added two roles:
ADMIN
BARISTA

Current role behavior:

Admin can see Sales, Dashboard, Inventory, and Settings navigation items
Barista can see Sales and Settings navigation items only

File involved:

app/src/main/java/com/royalcoffee/pos/model/UserRole.kt
6. Main App Flow

Completed:

Created basic app flow using RoyalCoffeeApp
User starts at Login Screen
After login, user is redirected to Sales/POS Screen
Logout returns user to Login Screen

Current flow:

LoginScreen
↓
SalesScreen
↓
Logout
↓
LoginScreen

File involved:

app/src/main/java/com/royalcoffee/pos/ui/RoyalCoffeeApp.kt
7. Sales/POS Screen

Completed:

Created Sales/POS screen
Created tablet-style POS layout
Added left-side navigation menu
Added product area
Added ticket/cart panel
Added responsive sizing for different tablet screens
Added safe drawing insets to avoid system UI overlap
Added sample products for testing
Added category filtering
Added product grid using adaptive columns

Current sample products:

Latte
Espresso
Croissant
Mocha
Iced Coffee
Muffin
Hot Chocolate

Main file involved:

app/src/main/java/com/royalcoffee/pos/ui/sales/SalesScreen.kt
8. Product and Cart Models

Completed:

Created Product model
Created CartItem model
Added calculated line total for cart items

Files involved:

app/src/main/java/com/royalcoffee/pos/model/Product.kt
app/src/main/java/com/royalcoffee/pos/model/CartItem.kt

Current cart behavior:

Tap product to add to cart
If product already exists, quantity increases
Quantity can be increased
Quantity can be decreased
Item is removed when quantity reaches zero
9. Ticket Panel

Completed:

Created professional ticket panel
Added current order display
Added cart item rows
Added quantity controls
Added empty ticket state
Added order summary card
Added subtotal, discount, tax, and total
Added payment summary after payment confirmation

Current calculation behavior:

Subtotal = sum of cart item totals
Discount = 0
Tax = 15% of subtotal
Total = Subtotal - Discount + Tax

Current ticket states:

Before payment:
- Shows cart items
- Shows order summary
- Shows Charge button

After payment:
- Shows payment confirmation summary
- Shows receipt-related action
10. Payment Workflow

Completed:

Created payment method selection dialog
Created Cash payment dialog
Created Card payment confirmation dialog
Created Mobile/Juice payment confirmation dialog
Added payment confirmation state
Added amount paid and change calculation for cash payments
Added cancel option for payment dialogs
Improved payment method dialog UI

Payment methods currently supported:

Cash
Card
Mobile Payment / Juice

Payment files involved:

app/src/main/java/com/royalcoffee/pos/model/PaymentMethod.kt
app/src/main/java/com/royalcoffee/pos/ui/payment/PaymentMethodDialog.kt
app/src/main/java/com/royalcoffee/pos/ui/payment/CashPaymentDialog.kt
app/src/main/java/com/royalcoffee/pos/ui/payment/ConfirmPaymentDialog.kt

Current payment behavior:

Cash:
- Cashier enters amount received
- App validates amount
- App calculates change

Card:
- App asks for confirmation
- Amount paid equals total

Mobile Payment:
- App asks for confirmation
- Amount paid equals total
11. Receipt Models

Completed:

Created Receipt model
Created ReceiptItem model
Separated finalized receipt data from live cart data
Added receipt number
Added receipt date/time
Added company/store information
Added cashier name
Added item list
Added subtotal, discount, tax, and total
Added payment method
Added amount paid and change given
Added footer message
Added helper properties such as total item count and change availability

Files involved:

app/src/main/java/com/royalcoffee/pos/model/Receipt.kt
app/src/main/java/com/royalcoffee/pos/model/ReceiptItem.kt

Receipt number format currently used:

RC-2026-000001

Important note:

Receipt numbering is currently temporary and in-memory. It will later be generated and stored using the local database.

12. Receipt Preview Flow

Completed:

Added receipt preview screen/dialog
Added thermal receipt-style visual layout
Added receipt paper preview
Added receipt header
Added receipt item section
Added summary section
Added payment section
Added footer message
Removed barcode placeholder because barcode/QR support is not required in Version 1
Added right-side action panel
Added Close button
Added Download Receipt button placeholder
Added Share via WhatsApp button placeholder
Added Print Receipt button
Added Next Order button
Prevented accidental dismiss by outside tap/back behavior during receipt preview
Updated company name from Royal Coffee POS to Royal Coffee
Updated receipt company title to use Cinzel brand font

File involved:

app/src/main/java/com/royalcoffee/pos/ui/receipt/ReceiptPreviewDialog.kt

Current receipt actions:

Close:
- Closes receipt preview only

Download Receipt:
- Placeholder message

Share via WhatsApp:
- Placeholder message

Print Receipt:
- Calls printer service layer

Next Order:
- Clears current order and prepares POS for the next customer

Current placeholder messages:

Receipt download is not available yet.
WhatsApp sharing is not available yet.
Printer not available. Check connection.
13. Improved Receipt Workflow

Completed:

Receipt flow was improved to reduce unnecessary cashier steps
Receipt Preview should open directly after payment confirmation
The old extra Generate Receipt step is being replaced by a faster workflow
A View Receipt action can remain available after closing the preview so the cashier can reopen the generated receipt

Target workflow:

Add products
↓
Charge
↓
Select payment method
↓
Confirm payment
↓
Receipt Preview opens automatically

Important note:

This improvement should be fully tested before moving to Step 13 navigation.

14. Printer Configuration Models

Completed:

Created PrinterConnectionType
Created PrinterConfig
Prepared printer settings structure for future printer support
Added support for Bluetooth printer configuration
Added Wi-Fi as a future connection type
Added default paper width setting
Added characters-per-line setting
Added default printer flag
Added active/inactive printer setting
Added placeholder Bluetooth printer configuration

Files involved:

app/src/main/java/com/royalcoffee/pos/model/PrinterConnectionType.kt
app/src/main/java/com/royalcoffee/pos/model/PrinterConfig.kt

Current printer configuration behavior:

PrinterConfig.placeholderBluetoothPrinter()

This is temporary and will later be replaced by printer data from the local database.

15. Fake Printer Service Layer

Completed:

Created printer package
Created ReceiptPrinter interface
Created ReceiptPrintResult
Created FakeReceiptPrinter
Removed printer logic from the receipt UI
Connected Print Receipt button to the printer service layer
Added professional printer unavailable message
Prepared the architecture so a real Bluetooth printer can be added later

Files involved:

app/src/main/java/com/royalcoffee/pos/printer/ReceiptPrinter.kt
app/src/main/java/com/royalcoffee/pos/printer/ReceiptPrintResult.kt
app/src/main/java/com/royalcoffee/pos/printer/FakeReceiptPrinter.kt

Current printer flow:

ReceiptPreviewDialog
↓
onPrintReceipt(receipt)
↓
SalesScreen
↓
FakeReceiptPrinter
↓
ReceiptPrintResult
↓
Toast message

Current message:

Printer not available. Check connection.
16. ESC/POS Receipt Formatter

Completed:

Created EscPosReceiptFormatter
Added plain text receipt formatting
Added ESC/POS-compatible byte formatting
Added printer initialization command
Added line feed after receipt
Added optional paper cut command
Added formatting based on printer characters-per-line
Used Rs instead of currency symbols to avoid printer encoding issues
Connected formatter to FakeReceiptPrinter

File involved:

app/src/main/java/com/royalcoffee/pos/printer/EscPosReceiptFormatter.kt

Current formatter flow:

Receipt object
↓
EscPosReceiptFormatter
↓
Formatted text
↓
ESC/POS byte array

Important note:

The formatter prepares printer-ready content, but no real printer connection is implemented yet.

17. Bluetooth Printer Support Skeleton

Completed:

Created BluetoothReceiptPrinter
Created PrinterConnectionStatus
Created PrinterConnectionCheckResult
Added Bluetooth printer support structure
Added safe skeleton for future real Bluetooth printing
Added Bluetooth-related permissions in AndroidManifest.xml
Kept FakeReceiptPrinter as the active printer implementation for now

Files involved:

app/src/main/java/com/royalcoffee/pos/printer/BluetoothReceiptPrinter.kt
app/src/main/java/com/royalcoffee/pos/printer/PrinterConnectionStatus.kt
app/src/main/java/com/royalcoffee/pos/printer/PrinterConnectionCheckResult.kt
app/src/main/AndroidManifest.xml

Bluetooth permissions prepared:

BLUETOOTH
BLUETOOTH_ADMIN
BLUETOOTH_SCAN
BLUETOOTH_CONNECT

Important note:

Real Bluetooth printing is not implemented yet because there is no physical printer available at this stage.

Current active printer implementation:

FakeReceiptPrinter

Future printer implementation:

BluetoothReceiptPrinter
18. Responsive Tablet UI Improvements

Completed:

Improved Sales/POS layout for different tablet screen sizes
Added dynamic side navigation width
Added dynamic ticket panel width
Added adaptive product card sizing
Added responsive product grid
Added safe drawing padding
Tested UI on emulator and tablet device

Important responsive layout areas:

Side Navigation
Product Grid
Ticket Panel
Receipt Preview Dialog
Payment Dialogs
Login Screen
19. Code Quality Improvements

Completed:

Code was organized into packages
UI files separated by feature
Models separated into model package
Printer logic separated into printer package
Added helpful comments to generated code
Improved naming for better readability
Started using cleaner architecture by separating UI from service logic

Current package structure:

com.royalcoffee.pos.model
com.royalcoffee.pos.printer
com.royalcoffee.pos.ui
com.royalcoffee.pos.ui.login
com.royalcoffee.pos.ui.payment
com.royalcoffee.pos.ui.receipt
com.royalcoffee.pos.ui.sales
com.royalcoffee.pos.ui.theme
Current File Structure Summary

Important files currently created or updated:

app/src/main/java/com/royalcoffee/pos/MainActivity.kt

app/src/main/java/com/royalcoffee/pos/ui/RoyalCoffeeApp.kt

app/src/main/java/com/royalcoffee/pos/ui/login/LoginScreen.kt

app/src/main/java/com/royalcoffee/pos/ui/sales/SalesScreen.kt

app/src/main/java/com/royalcoffee/pos/ui/payment/PaymentMethodDialog.kt
app/src/main/java/com/royalcoffee/pos/ui/payment/CashPaymentDialog.kt
app/src/main/java/com/royalcoffee/pos/ui/payment/ConfirmPaymentDialog.kt

app/src/main/java/com/royalcoffee/pos/ui/receipt/ReceiptPreviewDialog.kt

app/src/main/java/com/royalcoffee/pos/model/UserRole.kt
app/src/main/java/com/royalcoffee/pos/model/Product.kt
app/src/main/java/com/royalcoffee/pos/model/CartItem.kt
app/src/main/java/com/royalcoffee/pos/model/PaymentMethod.kt
app/src/main/java/com/royalcoffee/pos/model/Receipt.kt
app/src/main/java/com/royalcoffee/pos/model/ReceiptItem.kt
app/src/main/java/com/royalcoffee/pos/model/PrinterConnectionType.kt
app/src/main/java/com/royalcoffee/pos/model/PrinterConfig.kt

app/src/main/java/com/royalcoffee/pos/printer/ReceiptPrinter.kt
app/src/main/java/com/royalcoffee/pos/printer/ReceiptPrintResult.kt
app/src/main/java/com/royalcoffee/pos/printer/FakeReceiptPrinter.kt
app/src/main/java/com/royalcoffee/pos/printer/EscPosReceiptFormatter.kt
app/src/main/java/com/royalcoffee/pos/printer/BluetoothReceiptPrinter.kt
app/src/main/java/com/royalcoffee/pos/printer/PrinterConnectionStatus.kt
app/src/main/java/com/royalcoffee/pos/printer/PrinterConnectionCheckResult.kt

app/src/main/java/com/royalcoffee/pos/ui/theme/Color.kt
app/src/main/java/com/royalcoffee/pos/ui/theme/Theme.kt
app/src/main/java/com/royalcoffee/pos/ui/theme/Type.kt

app/src/main/AndroidManifest.xml
Important Design Decisions Made
1. Sales Screen as Home Screen

After login, users go directly to the Sales/POS screen.

Reason:

Faster workflow
Better for café staff
Reduces unnecessary navigation
Matches real POS usage
2. Role-Based Navigation

Admin and Barista users do not see the same menu items.

Current behavior:

Admin:
- Sales
- Dashboard
- Inventory
- Settings

Barista:
- Sales
- Settings

Reason:

Keeps the interface simple
Prevents cashier/barista users from accessing management features
Supports better security and usability
3. Local-First Architecture

The app is planned to work locally first.

Reason:

Café POS systems should continue working during internet issues
Sales should not depend fully on cloud availability
Firebase synchronization can happen later

Current status:

Local database not implemented yet.
Firebase not implemented yet.
4. Receipt Header/Details Concept

Receipt structure follows a header/details design.

Planned future database structure:

Receipt Header / Orders table
Receipt Details / Order Items table
Payments table

Reason:

One order can contain many order items
Cleaner reporting
Better receipt history
Easier sales analytics later
5. Printer Layer Separation

Printing logic is separated from the UI.

Reason:

UI should not handle hardware logic directly
Easier to test
Easier to replace fake printer with real Bluetooth printer later
Cleaner architecture

Current printer architecture:

ReceiptPreviewDialog
↓
SalesScreen
↓
ReceiptPrinter interface
↓
FakeReceiptPrinter
↓
EscPosReceiptFormatter
Pending / Not Yet Implemented

The following features are not completed yet:

Room database
SQLite tables
Firebase synchronization
Real Bluetooth printer connection
Download receipt to local storage
Share receipt through WhatsApp
Dashboard screen
Inventory screen
Settings screen
Functional main navigation between screens
User management
Product management
Inventory alerts
Sales reports
Language switching
Receipt footer setting
Cloud backup on Next Order
Current Pending UI Adjustment

Before starting Step 13, the following adjustment was planned:

Add Clear Ticket button
Add Cancel Order button

Planned behavior:

Clear Ticket:
- Clears the current cart before payment

Cancel Order:
- Cancels the entire active order
- Clears cart, payment state, receipt state, and closes dialogs
- Shows confirmation dialog before clearing

This should be implemented and tested before starting full main screen navigation.

Next Development Steps
Step 12 Final UI Cleanup

Before moving to navigation:

1. Add Clear Ticket button
2. Add Cancel Order button
3. Test order clearing behavior
4. Commit changes

Suggested commit message:

git commit -m "Add clear ticket and cancel order actions"
Step 13 - Main Screen Navigation

Next major step:

Step 13 - Add Main Screen Navigation

Goal:

Make the side navigation menu functional.

Planned screens:

Sales
Dashboard
Inventory
Settings

Expected behavior:

Login
↓
Sales screen by default
↓
Click Dashboard → Dashboard placeholder
Click Inventory → Inventory placeholder
Click Settings → Settings placeholder
Click Sales → return to POS screen
Later Steps

Planned future development order:

Step 14 - Settings Screen
Step 15 - Inventory Screen
Step 16 - Dashboard Screen
Step 17 - Room Database Setup
Step 18 - Create database entities and DAOs
Step 19 - Replace hardcoded products with database products
Step 20 - Save orders and receipts locally
Step 21 - Add receipt download
Step 22 - Add WhatsApp sharing
Step 23 - Add real Bluetooth printer support
Step 24 - Add Firebase backup/sync
Suggested Recent Commit History

Recent completed commits may include:

Improve login screen responsiveness and keyboard handling
Improve ticket panel UI design
Improve payment method dialog UI
Add generate receipt action after payment confirmation
Add receipt preview dialog and next order flow
Add dynamic receipt number and timestamp
Improve receipt preview UI and add future action buttons
Refactor receipt preview to use receipt model
Improve thermal receipt preview action layout
Add fake receipt printer service layer
Add ESC POS receipt formatter
Add Bluetooth printer support skeleton
Improve receipt workflow and company branding

Pending commit:

Add clear ticket and cancel order actions
Current Development Checkpoint

The current application has a working POS sales flow using temporary in-memory data.

Completed core workflow:

Login
↓
Sales/POS
↓
Add products
↓
View ticket
↓
Confirm payment
↓
Generate receipt data
↓
Preview receipt
↓
Use receipt actions
↓
Start next order