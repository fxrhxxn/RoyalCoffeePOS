# Dashboard UI/UX Enhancement Plan

Improve the Royal Coffee POS dashboard to be visually stunning, interactive, and data-rich using modern Jetpack Compose techniques.

## User Review Required

> [!IMPORTANT]
> This plan introduces custom-drawn charts (Line, Donut, Bar) using `androidx.compose.ui.graphics.Canvas` to maintain the "Royal Coffee" aesthetic without adding external heavy libraries.

## Proposed Changes

### UI Theme & Components

#### [MODIFY] [Color.kt](file:///C:/AndroidProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/ui/theme/Color.kt)
- Add gradient definitions for metric cards and chart fills.
- Add specific colors for "Success" (Green) and "Neutral" (Gold/Brown) trends.

#### [NEW] [DashboardComponents.kt](file:///C:/AndroidProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/ui/dashboard/DashboardComponents.kt)
- Create `DashboardLineChart`: A smooth Bezier curve for sales trends.
- Create `DashboardDonutChart`: A proportional chart for payment methods.
- Create `EnhancedDashboardCard`: A reusable glassmorphic-style container.
- Create `MetricCardWithTrend`: Metric card with icons and "percentage change" indicators.

---

### Dashboard Screen Refactor

#### [MODIFY] [DashboardScreen.kt](file:///C:/AndroidProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/ui/dashboard/DashboardScreen.kt)
- Replace the simple `DashboardSummarySection` with the new `MetricCardWithTrend`.
- Replace `HorizontalBarChartCard` with the new interactive charts.
- Implement enter-animations for all data components using `AnimatedVisibility` and `Modifier.graphicsLayer`.
- Improve layout spacing and responsive design for tablets and phones.

---

### Data Models

#### [MODIFY] [DashboardScreen.kt](file:///C:/AndroidProjects/RoyalCoffeePOS/app/src/main/java/com/royalcoffee/pos/ui/dashboard/DashboardScreen.kt)
- Update `DashboardSummary` to include trend percentages (e.g., `salesChange: Double`).
- Add `SalesTrendPoint` data class to support the line chart.

## Verification Plan

### Manual Verification
- Deploy to an Android Tablet/Phone.
- Verify chart animations trigger correctly on screen entry.
- Test interactivity: tap on Donut segments and Line chart points to see value popups.
- Check "Royal Coffee" theme consistency (Golds and Greens).
