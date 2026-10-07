# Pricing and discounts

Implemented on `feature/pricing-discounts`, based on integration commit `7ed5b14`.

## Money contract

All monetary `Long` properties (including existing names such as `unitPrice`, `total`,
`costPrice`, and DAO aggregates) are **integer cents**. Rs 99.50 is 9950.
SQLite columns keep INTEGER affinity. Do not pass whole rupees to these properties.
`Money.parse` converts decimal user input without a floating-point intermediate;
`Money.format` always renders two decimal places. Inputs accept up to two decimal
places, reject negatives and overflow, and cap a single entered amount at Rs 1 billion.
Quantities, IDs, and percentages remain integers. Float/Double use in chart geometry
and percentage trends does not feed transaction amounts.

Selling prices already include VAT. New orders add no tax. Whole-order discounts
are 0/5% for BARISTA and 0/5/10/15% for ADMIN. The ViewModel and repository enforce
the same policy. Discounts are rounded HALF_UP once to the nearest cent, then
subtracted from subtotal. Rs 99.50 at 5% => discount Rs 4.98, total Rs 94.52.
Discount selection resets when the cart is cleared and is disabled on the paid order.

Receipts snapshot the percentage and VAT-inclusive flag. A cumulative proportional
allocation distributes the rounded order discount over receipt lines; allocations
sum exactly to the header discount. Gross line totals remain unchanged for item
presentation. Dashboard line revenue/profit subtracts the allocated discount.
This is an internal reporting allocation, not an individual-product discount feature.

## Database upgrade

Room version 10 registers migration 9 -> 10. Product and receipt monetary columns
are multiplied by 100, including historical tax, payments and change. Nullable
historical amounts/costs stay NULL. Historical totals are not recalculated using
the new pricing policy. Existing receipts receive NULL discount percentage and
`vat_included = false`, so their original tax presentation remains available.
New receipts receive the selected percentage and `vat_included = true`.
Receipt detail `discount_amount` defaults to zero for existing receipts (the prior
application always used zero order discount).

Destructive fallback has been removed. Version 8 can use 8 -> 9 -> 10. Existing
migration gaps 6 -> 7 and 7 -> 8 are not invented or patched in this feature.
Installations without a complete migration path will fail to open instead of
silently losing data. Inspect historical schemas and add tested migrations before
upgrading such installations. Do not clear app data or uninstall as a migration fix.

## Validation performed

- Six JUnit tests compiled/run with Kotlin 2.2.10 outside Gradle: exact decimal
  conversion, VAT-inclusive totals and HALF_UP rounding, role restrictions, exact
  allocations, values beyond Int/overflow, and new/legacy thermal receipt output.
- Two host SQLite tests execute migration SQL and DAO query SQL extracted directly
  from the Kotlin source. They verify preservation of amounts and NULLs, untouched
  fixture identity data, and discounted sales/category/profit aggregates.
- `git diff --check`.
- Attempted `bash gradlew :app:assembleDebug :app:testDebugUnitTest --console=plain`.
  It stopped downloading Gradle 9.6.1 with `java.net.SocketException: Network is
  unreachable`. No full Android compilation, APK, emulator or device result is claimed.

The host SQL tests are not Room generated-schema validation. The standalone Kotlin
run compiles the models and thermal formatter, not Compose or Android-specific code.

## Required before merge

Run the Gradle build/tests in Android Studio with the project's SDK installed.
On a backed-up version-9 test database, upgrade without clearing data and verify
Room opens, existing login/business/product/category/history data survives, and
historical receipts display their original rupee amounts. Repeat a fresh install
and first-run registration/login. Test sales, inventory add/edit (99.50), all four
Admin discounts, both Barista discounts, cash change, card/Juice, cancel/next order
reset, receipt history, PDF download, fake printing and dashboard totals. Check
landscape tablet layout at the supported sizes. Record results before marking the
PR ready. Category-to-product analytics still join by product name, an existing
limitation outside this pricing feature.
