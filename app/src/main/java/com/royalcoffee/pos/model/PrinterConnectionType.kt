package com.royalcoffee.pos.model

/**
 * PrinterConnectionType defines how the POS app connects to a receipt printer.
 *
 * Version 1 will support Bluetooth thermal printers.
 *
 * Wi-Fi is included here as a future option so that the app design is already
 * prepared for later expansion.
 */
enum class PrinterConnectionType {
    /**
     * Bluetooth thermal printer.
     *
     * This will be the main supported printer type in Version 1.
     */
    BLUETOOTH,

    /**
     * Wi-Fi / network printer.
     *
     * This is not implemented in Version 1.
     * It is included for future expansion.
     */
    WIFI,

    /**
     * USB printer.
     *
     * This is not implemented in Version 1.
     * It is included for future expansion.
     */
    USB
}