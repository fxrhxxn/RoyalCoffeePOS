package com.royalcoffee.pos.model

/**
 * AppScreen represents the main screens available after login.
 *
 * This enum helps the app know which screen should be displayed
 * when the user taps an item in the side navigation menu.
 */
enum class AppScreen(
    val title: String
) {
    /**
     * Main POS screen where products are selected and orders are processed.
     */
    NOTIFICATIONS("Notifications"),
    SALES("Sales"),

    /**
     * Admin dashboard for future sales summaries and analytics.
     */
    DASHBOARD("Dashboard"),

    /**
     * Admin inventory screen for future product and stock management.
     */
    INVENTORY("Inventory"),

    /**
     * Admin receipt history screen for past transactions.
     */
   RECEIPTS("Receipts"),

    /**
     * Settings screen for language, receipt footer, and future app settings.
     */
    SETTINGS("Settings")
}
