package com.royalcoffee.pos.printer

/**
 * PrinterConnectionStatus represents the possible states of a printer connection.
 *
 * We are creating this now so that future Bluetooth printing can report clear,
 * professional status messages to the app.
 */
enum class PrinterConnectionStatus {
    /**
     * No printer has been configured yet.
     */
    NOT_CONFIGURED,

    /**
     * The Android device does not support Bluetooth.
     */
    BLUETOOTH_NOT_SUPPORTED,

    /**
     * Bluetooth exists on the device, but it is currently turned off.
     */
    BLUETOOTH_DISABLED,

    /**
     * The app does not yet have the required Bluetooth permission.
     */
    PERMISSION_REQUIRED,

    /**
     * The app is attempting to connect to the printer.
     */
    CONNECTING,

    /**
     * The printer is connected and ready.
     */
    CONNECTED,

    /**
     * The app failed to connect to the printer.
     */
    CONNECTION_FAILED,

    /**
     * Receipt printing completed successfully.
     */
    PRINT_SUCCESS,

    /**
     * Receipt printing failed.
     */
    PRINT_FAILED
}