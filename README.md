# Royal Coffee POS

Royal Coffee POS is a native Android point-of-sale application developed for a café environment as part of the **BSc (Hons) Computer Science and Artificial Intelligence – Mobile Application Development** module.

The application is designed primarily for a **single Android tablet in landscape orientation** and provides an end-to-end café workflow covering sales, payments, inventory, receipts, customers, notifications, and business reporting.

Royal Coffee POS follows a **local-first architecture** using Room and SQLite, allowing the core application to operate without depending on an internet connection.

---

## Overview

Royal Coffee POS was designed to provide a modern and simple alternative to a traditional café POS system.

The application supports two main user roles:

- **Admin**
- **Barista / Cashier**

The system provides functionality for:

- Processing customer orders
- Managing products and categories
- Handling stock and expiry dates
- Processing simulated payments
- Generating digital receipts
- Sharing receipts through WhatsApp
- Managing saved customers
- Monitoring stock alerts
- Reviewing historical transactions
- Viewing sales analytics
- Managing staff and application settings

---

## Key Features

### Authentication and Role-Based Access

- First-run Admin registration
- Username and PIN authentication
- Secure PIN storage
- Admin and Barista/Cashier roles
- Role-based screen access
- Session-specific authenticated user information

Admin users have access to management features such as:

- Dashboard
- Inventory
- Customer Management
- Staff Management
- Business Settings

Baristas primarily access operational features required to process sales.

---

## Sales / POS

The Sales screen provides the main point-of-sale workflow.

Features include:

- Product selection
- Category filtering
- Product search
- Shopping cart
- Quantity adjustment
- Remove item
- Clear ticket
- Stock-aware quantity limits
- Automatic subtotal calculation
- Role-based discounts
- VAT-inclusive pricing
- Automatic final total calculation
- Order locking after payment confirmation

The POS interface is optimized for touch interaction on landscape tablets.

---

## Payment Processing

Royal Coffee POS supports three payment methods:

### Cash

- Enter amount received
- Validate payment amount
- Automatically calculate customer change
- Confirm or cancel payment

### Card

- Payment confirmation workflow
- Confirm or cancel transaction

### Mobile Payment / MCB Juice

- Payment confirmation workflow
- Confirm or cancel transaction

> Card and mobile payments are simulated. The application does not connect to a real bank, card processor, or MCB Juice service.

---

## Receipt Management

After a successful payment, the application generates a digital receipt.

Receipt functionality includes:

- Receipt preview
- Unique receipt numbers
- Cashier information
- Transaction date and time
- Purchased items
- Discounts
- VAT-inclusive totals
- Payment information
- Amount paid and change
- Configurable receipt footer

Users can also:

- View receipt history
- Open historical receipts
- Download receipts as PDF
- Re-share previous receipts
- Reprint receipts using the current simulated printer implementation

Receipt PDFs are generated locally.

---

## WhatsApp Receipt Sharing

Receipts can be shared as PDF documents through WhatsApp.

The sharing workflow allows the cashier to:

- Search saved customers by name
- Search saved customers by phone number
- Select an existing customer
- Enter a new phone number
- Share without saving the customer
- Save a customer and immediately share the receipt

The application attempts **best-effort recipient targeting** when a customer is selected.

If direct WhatsApp recipient targeting is unavailable or unsupported, the app falls back to the normal WhatsApp sharing flow.

The application prepares the:

- Recipient where supported
- PDF receipt
- Receipt message

The user remains responsible for confirming and sending the message inside WhatsApp.

---

## Customer Management

Royal Coffee POS includes a local customer directory stored using Room.

Each customer contains:

- Customer name
- Phone number
- Date created

Admin users can:

- View saved customers
- Search customers
- Add customers
- Edit customers
- Delete customers

Customer search updates automatically while typing.

### Phone Number Handling

Mauritian phone numbers are normalized into international format.

Example:

```text
52581234
→ +23052581234
```

Formatting such as:

```text
5258 1234
```

is also supported.

Local Mauritian numbers must contain exactly **8 digits**.

International phone numbers beginning with `+` or `00` are also supported.

Duplicate customer phone numbers are prevented.

Customers are currently used to simplify receipt sharing and are **not linked directly to stored receipts**.

---

## Inventory Management

Admin users can manage products, stock, and categories.

Inventory functionality includes:

- Add products
- Edit products
- Product activation/deactivation
- Product categories
- Cost price
- Selling price
- Stock quantity
- Reorder levels
- Restocking
- Stock adjustments
- Inventory transaction history
- Stock batches
- Expiry-date management

Stock levels are automatically reduced when sales are completed.

---

## Stock Batches and FEFO

Inventory stock can be divided into individual stock batches.

Each stock batch records:

- Product
- Remaining quantity
- Expiry date
- Date received

The application uses **FEFO — First Expired, First Out** when consuming stock.

This means stock with the nearest expiry date is consumed before stock with a later expiry date.

This helps reduce product wastage and improves expiry management.

---

## Material 3 Expiry Date Picker

Expiry dates are selected using a reusable **Material 3 DatePicker** rather than manually entering dates.

The date picker is used when:

- Adding a product
- Restocking a product
- Adding a stock batch
- Editing a stock batch expiry date

Features include:

- Calendar-based selection
- Friendly date display such as `20 Nov 2026`
- Optional expiry dates
- Clear / No Expiry option
- Remove Expiry for existing batches
- Past-date restriction for newly selected expiry dates
- Existing historical expiry dates remain viewable

Expiry dates are stored internally using `LocalDate.toEpochDay()`.

UTC-safe conversion is used between Material DatePicker milliseconds and stored epoch-day values to avoid timezone-related date shifts.

---

## Notifications and Alerts

Royal Coffee POS includes inventory monitoring and alert functionality.

Alerts are generated for:

- Low-stock products
- Products or stock batches approaching expiry

The app maintains an in-app alert history.

Android notifications are used when notification permission has been granted.

Stock alert checks can also be scheduled using Android background job functionality.

---

## Dashboard and Analytics

Admin users have access to a dashboard built from locally stored sales and inventory information.

Dashboard functionality includes summaries such as:

- Sales information
- Revenue information
- Payment breakdown
- Product performance
- Inventory-related information

The dashboard provides management with a quick overview of café performance.

---

## Settings and Staff Management

Settings include functionality for:

- Staff management
- Business information
- Receipt configuration
- Receipt footer
- Application language

Admin users can manage staff accounts and application-level configuration.

---

## English and French Localization

Royal Coffee POS includes English and French localization support.

French translations are available across several important application areas including:

- Login
- Navigation
- Sales
- Payment interfaces

Localization is **partially implemented** in the current version, and some screens/messages remain in English.

---

## Responsive Tablet Interface

The application was designed primarily for Android tablets in landscape orientation.

Responsive improvements were implemented across:

- Login
- Registration
- Sales / POS
- Dashboard
- Inventory
- Notifications
- Receipt History
- Receipt Preview
- Customer Management
- Settings
- Payment dialogs
- WhatsApp customer selection

Layouts adapt to different tablet widths and narrower landscape configurations.

Keyboard-aware layouts use techniques such as scrolling and `imePadding()` where necessary to prevent fields and controls from being hidden by the software keyboard.

---

## Technology Stack

### Language

- Kotlin

### Android UI

- Jetpack Compose
- Material 3

### Architecture

- MVVM
- ViewModels
- Repository pattern
- DAO pattern

### Local Data

- Room
- SQLite

### Android APIs and Integrations

- FileProvider
- Android Document Picker
- Android Notifications
- JobScheduler
- WhatsApp sharing intents

### Development Tools

- Android Studio
- Gradle
- Git
- GitHub

---

## Architecture

Royal Coffee POS follows an MVVM-style architecture.

```text
┌──────────────────────────────┐
│       Jetpack Compose UI     │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│          ViewModels          │
│      UI State / Logic        │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│         Repositories         │
│      Business Operations     │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│          Room DAOs           │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│        Room / SQLite         │
│      Local Source of Truth   │
└──────────────────────────────┘
```

The application uses a **local-first architecture**.

Room/SQLite acts as the primary source of operational data.

Cloud synchronization and Firebase backup are not implemented in the current version.

---

## Local Database

The Room database currently contains data for areas including:

- Products
- Categories
- Users
- Business information
- Receipt headers
- Receipt details
- Inventory history
- Stock batches
- Stock alerts
- Customers

The current Room database version is:

```text
13
```

Database migrations are used to preserve data between supported schema versions.

---

## Application Navigation

Main application areas include:

```text
Sales
Dashboard
Inventory
Customers
Notifications
Receipts
Settings
```

Access to individual screens depends on the authenticated user's role.

---

## Permissions and Android Integration

The application currently declares permissions related to:

- Android notifications
- Background stock-alert scheduling
- Contacts
- Bluetooth / future printer support

The current customer directory itself is stored internally using Room and does **not depend on Android Contacts**.

Receipt PDFs are shared securely using Android `FileProvider`.

Broad storage permissions are not required for saving or sharing receipt PDFs.

Bluetooth-related permissions remain in the project for printer-related integration; however, real physical printer discovery and printing are not implemented in the current version.

---

## Requirements

To build and run the project:

- Android Studio
- Compatible Android SDK
- Compatible Gradle JDK
- Android tablet, phone, or emulator

The application is optimized for **landscape tablet use**.

WhatsApp must be installed on the target device to test WhatsApp receipt sharing.

---

## Running the Application

1. Clone the repository.

```bash
git clone <repository-url>
```

2. Open the project in Android Studio.

3. Allow Gradle synchronization to complete.

4. Connect an Android device or launch an emulator.

5. Run the `app` configuration.

---

## Building the Debug APK

### Windows PowerShell

```powershell
.\gradlew assembleDebug
```

### macOS / Linux

```bash
./gradlew assembleDebug
```

The generated APK is normally located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## Testing

The project contains Kotlin/JUnit and database-related tests covering selected functionality such as:

- Business logic
- Database migrations
- Customer phone normalization
- Phone-number formatting
- WhatsApp recipient helper logic
- Expiry-date conversion
- Date-selection restrictions

The final development build completed successfully with:

```text
15 tests passed
0 failed
```

The project was also validated through manual testing on Android devices and emulators for major workflows including:

- Authentication
- Sales
- Payments
- Inventory
- Receipts
- Customer management
- WhatsApp sharing
- Tablet responsiveness

---

## Current Limitations

The current academic version has several deliberate limitations:

- Designed primarily for a single Android tablet
- No cloud synchronization
- No Firebase backup
- Card payments are simulated
- Mobile / Juice payments are simulated
- No live payment gateway integration
- Physical thermal-printer integration is not complete
- WhatsApp controls the final message sending process
- Direct WhatsApp recipient targeting is best-effort
- Full English/French localization is not complete
- Customers are not linked to historical receipts

---

## Future Improvements

Possible future development includes:

- Firebase or cloud synchronization
- Multi-device operation
- Cloud backup and restore
- Real card-payment integration
- MCB Juice / mobile-payment integration
- Physical Bluetooth thermal-printer support
- Complete French localization
- Customer purchase history
- Customer-to-receipt relationships
- Enhanced analytics and reporting
- Additional automated UI and integration tests
- Support for additional Android device sizes and orientations

---

## Academic Project

Royal Coffee POS was developed by:

**Hassam Mahomed Farhaan Sede**  
**Christa Mungada**

for the **Mobile Application Development** module of the:

**BSc (Hons) Computer Science and Artificial Intelligence (Top-Up)**

The project demonstrates the design and development of a native Android business application using Kotlin, Jetpack Compose, Material 3, Room, MVVM architecture, and Android platform integrations.
