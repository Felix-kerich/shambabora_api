# Patch / Plot (per-year) support

This file explains the new patch/plot concept added to the recordskeeping module and recommended next steps.

Overview
--------

The patch (aka plot) represents a planted area for a specific season and year. By linking activities, expenses, inputs and yields to a patch we enable clear per-patch accounting and comparisons across years.

Key backend changes included
- `MaizePatch` entity (table `maize_patches`) to store patch metadata.
- `MaizePatchRepository` with basic finders.
- `MaizePatchDTO`, `MaizePatchService` and `MaizePatchController` for basic CRUD/list operations.
- `patchId` (and optional `patchName`) were added to: `FarmActivity`, `FarmExpense`, `YieldRecord`, `InputUsageRecord`.
- DTOs updated so responses return `patchId` and `patchName`.

Why this is useful (maize focus)
- Group whole-season events (planting -> harvest) for a patch.
- Connect inputs (seed/fertilizer/pesticide) directly to patches and yields.
- Compute per-patch cost breakdown and profit (cost-per-kg, profit-per-kg).
- Enable apples-to-apples comparisons across years to find best seed/fertilizer combinations.

Recommended next steps
----------------------
1. Create DB migration scripts:
   - Create table `maize_patches`.
   - Add nullable `patch_id` bigint columns to `farm_activities`, `farm_expenses`, `yield_records`, `input_usage_records`.
2. Update services:
   - Accept `patchId` in create/update requests and store `patchName` (cached) for quick reads.
   - When creating a YieldRecord, compute and store `totalInputCost`, `costPerKgProduced`, and `profitPerKg` using linked expenses and inputs filtered by `patchId`.
3. Analytics endpoints:
   - `/api/analytics/patches/{id}/summary` — returns activities, inputs, expenses, yield, cost, profit for a patch.
   - `/api/analytics/patches/compare` — compare multiple patches (same farmer) returning a table of seed/fertilizer combos vs yield/cost/profit.
4. Frontend changes:
   - UI to create patches and select patch when recording activities/expenses/yields.
   - Comparison view with charts (yield vs cost, ROI, input effectiveness).
5. Data migration/backfill:
   - Provide a helper script to map existing records to new patches using date ranges and manual mapping when ambiguous.

If you want, I can generate the SQL migration and a backfill script next.
