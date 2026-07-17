package com.royalcoffee.pos.model

/**
 * PrinterConfig represents the printer settings saved by the POS app.
 *
 * Later, this model can become a Room database entity for a "printers" table.
 *
 * For now, it is only a normal Kotlin data class.
 */
data class PrinterConfig(
    /**
     * Local printer ID.
     *
     * For now, this can stay as 0 because we are not using a real database yet.
     * Later, Room can auto-generate this value.
     */
    val printerId: Int = 0,

    /**
     * Friendly printer name shown inside the app.
     *
     * Example:
     * "Main Counter Printer"
     */
    val printerName: String,

    /**
     * Defines whether this printer uses Bluetooth or Wi-Fi.
     *
     * Version 1 will use Bluetooth.
     */
    val connectionType: PrinterConnectionType,

    /**
     * Bluetooth device name.
     *
     * Example:
     * "MTP-II"
     * "POS-58"
     * "Thermal Printer"
     *
     * This is optional because Wi-Fi printers will not use it.
     */
    val bluetoothDeviceName: String? = null,

    /**
     * Bluetooth MAC address.
     *
     * Example:
     * "00:11:22:33:44:55"
     *
     * This is optional for now because no physical printer is connected yet.
     */
    val bluetoothMacAddress: String? = null,

    /**
     * Wi-Fi printer IP address.
     *
     * This is for a future version.
     *
     * Example:
     * "192.168.1.50"
     */
    val ipAddress: String? = null,

    /**
     * Wi-Fi printer port.
     *
     * Many network printers use port 9100, but this will only matter later
     * when Wi-Fi printing is implemented.
     */
    val port: Int? = null,

    /**
     * Thermal printer paper width in millimeters.
     *
     * Common values:
     * - 58mm
     * - 80mm
     *
     * Version 1 can default to 58mm because many small Bluetooth thermal
     * printers use 58mm paper.
     */
    val paperWidthMm: Int = 58,

    /**
     * Approximate number of printable characters per line.
     *
     * Common values:
     * - 58mm printer: around 32 characters
     * - 80mm printer: around 42 to 48 characters
     *
     * This will later help the ESC/POS formatter align receipt text correctly.
     */
    val charactersPerLine: Int = 32,

    /**
     * Whether this is the default printer.
     *
     * In a real café setup, the cashier should not select the printer every time.
     * The app should remember the default printer.
     */
    val isDefault: Boolean = false,

    /**
     * Whether this printer configuration is active.
     *
     * If a printer is no longer used, we can mark it inactive instead of deleting it.
     */
    val isActive: Boolean = true,

    /**
     * Last successful connection timestamp.
     *
     * For now, this is nullable because we are not actually connecting yet.
     * Later, this can store the last time the printer was successfully reached.
     */
    val lastConnectedAt: String? = null
) {
    /**
     * Returns true when this printer is a Bluetooth printer.
     */
    val isBluetoothPrinter: Boolean
        get() = connectionType == PrinterConnectionType.BLUETOOTH

    /**
     * Returns true when this printer is a Wi-Fi printer.
     *
     * This is for future use.
     */
    val isWifiPrinter: Boolean
        get() = connectionType == PrinterConnectionType.WIFI

    /**
     * Returns true when this printer is a USB printer.
     *
     * This is for future use.
     */
    val isUsbPrinter: Boolean
        get() = connectionType == PrinterConnectionType.USB

    /**
     * Returns a readable label for the paper size.
     *
     * Example:
     * "58mm / 32 chars"
     */
    val paperLabel: String
        get() = "${paperWidthMm}mm / $charactersPerLine chars"

    /**
     * Checks whether enough information exists to attempt a printer connection.
     *
     * For Bluetooth:
     * - Device name and MAC address should exist.
     *
     * For Wi-Fi:
     * - IP address and port should exist.
     *
     * Since we do not have a physical printer yet, this will normally return false
     * for the placeholder printer.
     */
    val canAttemptConnection: Boolean
        get() {
            return when (connectionType) {
                PrinterConnectionType.BLUETOOTH -> {
                    !bluetoothDeviceName.isNullOrBlank() &&
                            !bluetoothMacAddress.isNullOrBlank()
                }

                PrinterConnectionType.WIFI -> {
                    !ipAddress.isNullOrBlank() &&
                            port != null
                }

                PrinterConnectionType.USB -> {
                    // USB printing logic will be defined in a future version.
                    false
                }
            }
        }

    companion object {
        /**
         * Temporary placeholder printer for Version 1 development.
         *
         * This lets the app have a printer configuration structure even though
         * there is no physical printer connected yet.
         */
        fun placeholderBluetoothPrinter(): PrinterConfig {
            return PrinterConfig(
                printerId = 1,
                printerName = "Main Counter Printer",
                connectionType = PrinterConnectionType.BLUETOOTH,
                bluetoothDeviceName = null,
                bluetoothMacAddress = null,
                paperWidthMm = 58,
                charactersPerLine = 32,
                isDefault = true,
                isActive = true,
                lastConnectedAt = null
            )
        }
    }
}