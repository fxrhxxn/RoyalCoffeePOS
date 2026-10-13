# Royal Coffee POS

Royal Coffee POS is a native Android point-of-sale application developed as a Mobile App Development assignment. It is designed for a café using a single Android tablet in landscape orientation.

The app supports the main café workflow: manage products and stock, record a sale, create a receipt, and review sales information. Operational data is stored locally using Room and SQLite, so core functions do not depend on an internet connection.

## Features

- Admin and Barista roles with role-based access
- Product and category management
- Sales screen with product selection and cart controls
- Cart quantity limits based on available stock
- Cash payment flow with amount validation and change calculation
- Card and Juice payment confirmation flows
- Receipt creation and local receipt history
- Receipt PDF download using Android’s document picker
- Receipt sharing through WhatsApp
- Historical receipt preview, download, sharing, and reprint actions
- Inventory history and stock batches with optional expiry dates
- Low-stock and approaching-expiry alerts
- Android notifications when permission is granted
- Dashboard summaries based on locally stored data

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- Room
- SQLite
- Android notification and document picker APIs
- Android FileProvider for sharing receipt PDFs

## Architecture

The app separates UI, state management, and data access using Compose screens, ViewModels, repositories, and Room DAOs.

Room/SQLite is the local source of truth for operational data. Firebase synchronization and cloud backup are **not currently implemented**.

## Requirements

- Android Studio
- An Android device or emulator
- A compatible Android SDK and Gradle JDK, as configured by the project

Android Studio can download the required Gradle and Android dependencies during the first project sync.

## Run the Application

1. Clone or download this repository.
2. Open the project in Android Studio.
3. Allow Gradle sync to complete.
4. Select an Android emulator or connect an Android device.
5. Run the `app` configuration.

To build a debug APK from a terminal:

**Windows PowerShell**

powershell
`.\gradlew.bat assembleDebug`

macOS or Linux
`./gradlew assembleDebug`

The debug APK is normally generated at:
`app/build/outputs/apk/debug/app-debug.apk`

## Permissions and Android Integrations
- Notification permission is requested where required by the Android version. Denying it does not remove in-app alert history.
- Contacts permission is requested only when the user chooses to search contacts while sharing a receipt. Manual phone-number entry remains available.
- Receipt downloads use Android’s document picker; broad storage permissions are not required.
- WhatsApp must be installed to use the WhatsApp sharing flow.

## Payment and Printing Notes
Card and Juice payments are simulated. The app does not connect to a bank, payment gateway, or Juice service.
Receipt reprint currently uses a simulated printer path. Physical printer discovery and printing have not been configured.
Current Limitations
- The application is designed for one Android tablet.
- Firebase synchronization and cloud backup are not implemented.
- Card and Juice payments are not processed by live payment services.
- Physical receipt-printer integration is not configured.
- WhatsApp requires the user to confirm the recipient and send the receipt in WhatsApp.
- Full English and French localization is not complete.

## Testing
The project includes recorded Kotlin/JUnit and SQLite migration tests for selected business logic and database changes. Android build, emulator, and physical-device verification should be run in the project environment before relying on a release build.
For WhatsApp sharing, notification permissions, PDF saving, and database migrations, test the relevant workflows on an Android device as well as an emulator where practical.

## Future Improvements
- Add Firebase synchronization and cloud backup
- Integrate real payment services
- Add physical thermal-printer support
- Complete English and French localization
- Expand automated and user-focused testing
- Test across additional Android devices and screen sizes

## Academic Project
This application was developed by Hassam Farhaan and Christa Mungada for a Mobile App Development assignment.



