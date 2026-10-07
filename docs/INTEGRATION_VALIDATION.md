# POS improvements integration validation

The scoped improvement set is implemented on `develop/pos-improvements`: category management, exact cent pricing and role-limited discounts, stock-limit warning, authenticated cashier receipt snapshots, batch stock/expiry alerts, native permissions, WhatsApp PDF sharing, historical receipt actions, and product lifecycle protection.

## Evidence

- Five Python SQLite tests execute the actual monetary, batch, and lifecycle migration SQL. They verify monetary conversion, stock preservation with unknown expiry, and history-preserving foreign-key deletion behavior.
- Pricing's six standalone Kotlin/JUnit tests passed during its implementation. Full Android unit-test execution still requires the Android build toolchain.
- `git diff --check` passes.
- Local `bash gradlew :app:compileDebugKotlin --no-daemon` stops before compilation: downloading Gradle 9.6.1 fails with `java.net.SocketException: Network is unreachable`. No Android build success is claimed.
- GitHub's Android validation workflow attempts the unchanged project toolchain on a hosted runner. Its result must be reviewed before release.

## Android checks required before main integration

Run `gradlew.bat :app:assembleDebug :app:testDebugUnitTest` in Android Studio's terminal on Windows (or `bash gradlew` on Unix). Then test on an emulator/tablet:

1. Upgrade a copy of a v9 database through v12; verify old receipts, amounts, categories, stock and inventory history. Verify fresh installation. Existing missing v6/v7 migration paths fail safely; do not uninstall or clear real business data as a workaround.
2. Log in as two different users, including the same role, and log out between them. Confirm fresh carts, role limits and receipt full-name snapshots. Editing/deleting an account must not change old receipt names.
3. Test cent prices and half-up discount rounding; Admin 0/5/10/15%, Barista 0/5%; new VAT-inclusive receipts and unchanged historical tax.
4. Add exactly available stock, acknowledge once, attempt another increase, reduce and add again. Complete payment at the stock limit; try checkout after stock decreases or a product is deactivated.
5. Restock dated and undated batches; sell/waste across batches. Confirm aggregate stock equals batch units, dated batches deplete by earliest expiry, alerts begin at 14 days and expired batches remain sellable.
6. Reach reorder level, repeat checks, restock above it and fall below again. Confirm one notification per episode and persistent resolved history. Test expiry change, process restart and background job execution; OS scheduling is approximate.
7. On Android 13+, allow/deny notifications and later change system settings. In-app history and sales must remain usable. Test contacts permission denial and manual phone entry.
8. Download new and historical receipts using the system picker; cancel once and retry. Check PDF layout and monetary text with long product/cashier names and multiple items.
9. With standard WhatsApp installed, verify local +230 normalization, live contact search, content-URI PDF attachment, prewritten text and recipient selection. Cancel sharing and return; never show a false sent confirmation. Test absent WhatsApp and Business-only installation (unavailable modal, no fallback).
10. Return from sharing an unknown contact and save via the native contacts app; denied contacts access must not be treated as proof the customer is absent.
11. Open a historical receipt while a new cart exists; download/share/reprint without clearing the cart. Reprint currently uses the fake printer/unavailable-hardware path.
12. Confirm deletion of a newly created empty product without history. A stocked or referenced product must deactivate; Reactivate returns it to sales. Editing inactive metadata must not reactivate it. Legacy unlinked receipts conservatively protect existing products from deletion.

## Boundaries

WhatsApp's documented click-to-chat supports number/text, but does not document attaching a PDF directly to that number. The dialog provides Open chat and explicit PDF recipient selection within standard WhatsApp. Sending always requires the user in WhatsApp.

Older design documents describe broader future work such as Firebase sync, suppliers and additional notification types. Those are not implemented by this scoped improvement set. This change retains integer cents and per-batch expiry from the agreed implementation requirements, rather than the older REAL/product-wide expiry schema sketches.

References: https://faq.whatsapp.com/5913398998672934 ; https://developer.android.com/develop/ui/compose/sharing/send ; https://developer.android.com/training/data-storage/shared/documents-files
