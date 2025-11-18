# Records Keeping & Analytics — Frontend Guide

This document is a frontend-focused, practical guide to the Records Keeping and Analytics APIs in ShambaBora (maize-focused). It explains the API endpoints, request and response shapes, example calls, UI wireframes and component suggestions, and recommended flows so your frontend team can implement a friendly, mobile-first UI for farmers.

Target audience
- Frontend engineers building the web or mobile app
- Product designers producing UX for farm record entry and analytics

Authentication
- All endpoints in the recordskeeping module require an Authorization header with a Bearer token. Example:

  Authorization: Bearer <JWT_TOKEN>

Core concepts
- Patch (MaizePatch): Represents a planted plot/plot for a specific year & season. Everything (activities, expenses, inputs, yields) should be linked to a Patch to enable per-plot accounting and year-over-year comparisons.
- Activity: A recorded farm activity (planting, weeding, fertilizing, top-dressing, spraying, harvesting, etc.).
- Expense: Money spent (seeds, fertilizer, labor, transport, equipment, etc.).
- YieldRecord: Harvest result for a patch or harvest activity (amount, unit, revenue).
- InputUsage: Which inputs (seed variety, fertilizer, pesticide) were applied during an activity — link inputs to patches and yields for analytics.

Base URL
- Example: `https://api.your-domain.com` (adjust to your environment)

Important headers
- Content-Type: application/json
- Authorization: Bearer <token>

-------------------------
API Reference (condensed)
-------------------------

1) Patches (Maize plots)

- POST /api/patches
  - Create a patch for the authenticated farmer
  - Request body (MaizePatchDTO - partial):

  ```json
  {
    "year": 2025,
    "season": "LONG_RAIN",
    "name": "Block A - 2025",
    "cropType": "Maize",
    "area": 1.25,
    "areaUnit": "ha",
    "plantingDate": "2025-03-10",
    "expectedHarvestDate": "2025-08-20",
    "location": "Block A",
    "notes": "Demonstration plot"
  }
  ```

  - Response: full `MaizePatchDTO` with `id`, `createdAt`, `updatedAt`.

- GET /api/patches
  - Lists patches for authenticated farmer. No body. Returns array of `MaizePatchDTO`.

- GET /api/patches/{id}
  - Returns `MaizePatchDTO` for a single patch (validates farmer ownership).


2) Farm Activities

- POST /api/farm-activities
  - Create an activity
  - Request body: `FarmActivityRequest`

  Example:
  ```json
  {
    "activityType": "PLANTING",
    "cropType": "Maize",
    "activityDate": "2025-03-10",
    "description": "Planting H511 variety",
    "areaSize": 1.25,
    "units": "ha",
    "seedVarietyId": 12,
    "seedVarietyName": "H511",
    "fertilizerProductId": 5,
    "fertilizerProductName": "Urea",
    "patchId": 3,
    "notes": "Planted after good rains"
  }
  ```

  - Response: `FarmActivityResponse` (includes `id`, `createdAt`, `patchId`, `patchName`)

- GET /api/farm-activities/{id}
- PUT /api/farm-activities/{id}
- DELETE /api/farm-activities/{id}
- GET /api/farm-activities?activityType=&page=&size=


3) Expenses

- POST /api/farm-expenses
  - Request: `FarmExpenseRequest`
  - Example:
  ```json
  {
    "cropType": "Maize",
    "category": "FERTILIZER",
    "description": "Urea top-up",
    "amount": 1200.00,
    "expenseDate": "2025-04-10",
    "supplier": "Local agro-dealer",
    "farmActivityId": 45,
    "patchId": 3
  }
  ```

- GET /api/farm-expenses (list with filters)
- GET /api/farm-expenses/{id}
- PUT /api/farm-expenses/{id}
- DELETE /api/farm-expenses/{id}
- GET /api/farm-expenses/total?cropType=Maize
- GET /api/farm-expenses/breakdown/category?cropType=Maize
- GET /api/farm-expenses/breakdown/growth-stage?cropType=Maize


4) Yield Records

- POST /api/yield-records
  - Request body: `YieldRecordRequest`
  - Example:
  ```json
  {
    "cropType": "Maize",
    "harvestDate": "2025-08-20",
    "yieldAmount": 2000.00,
    "unit": "kg",
    "areaHarvested": 1.25,
    "marketPrice": 40.00,
    "farmActivityId": 78,
    "patchId": 3
  }
  ```

- GET /api/yield-records (list)
- GET /api/yield-records/{id}
- PUT /api/yield-records/{id}
- DELETE /api/yield-records/{id}
- GET /api/yield-records/total?cropType=Maize
- GET /api/yield-records/revenue?cropType=Maize
- GET /api/yield-records/average?cropType=Maize
- GET /api/yield-records/best?cropType=Maize
- GET /api/yield-records/trends?cropType=Maize&startDate=2025-01-01&endDate=2025-12-31


5) Input Usage (tracking seed/fert/chemicals)

- Currently the codebase stores `InputUsageRecord` entities and `InputUsageRecordDTO` but there is not yet a dedicated public controller endpoint in this branch. Options:
  - Record inputs as part of the `FarmActivity` creation (preferred short term): include `seedVarietyId`, `fertilizerProductId`, `pesticideProductId`, quantity and `unit`.
  - Add a dedicated endpoint `/api/input-usage` (recommended) that accepts `InputUsageRecordDTO` for detailed input tracking. I can add this endpoint if you want.


6) Analytics (Patch-level)

- GET /api/farm-analytics/patches/{patchId}/summary
  - Returns `PatchSummaryDTO` with aggregated totals for that patch:
    - totalExpenses, totalYield, totalRevenue, costPerKg, profit, profitPerKg, roiPercentage
    - activityTypes, inputSummaries and expenseSummaries (summary strings)

- POST /api/farm-analytics/patches/compare
  - Request body: JSON array of patch ids: `[3, 5]`
  - Returns: `PatchComparisonDTO` containing an array of `PatchSummaryDTO` for comparison.


Sample API client (TypeScript / axios)

```ts
import axios from 'axios';

const api = axios.create({ baseURL: 'https://api.your-domain.com', headers: { 'Content-Type': 'application/json' } });

api.interceptors.request.use(config => {
  // attach token from app state
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// create a patch
export const createPatch = (payload) => api.post('/api/patches', payload).then(r => r.data);

// create activity
export const createActivity = (payload) => api.post('/api/farm-activities', payload).then(r => r.data);

// get patch summary
export const getPatchSummary = (patchId) => api.get(`/api/farm-analytics/patches/${patchId}/summary`).then(r => r.data);

// compare patches
export const comparePatches = (patchIds: number[]) => api.post('/api/farm-analytics/patches/compare', patchIds).then(r => r.data);
```

UI pages & components (recommended structure)

- Pages
  - Patches (List) — shows patches created by the farmer, create button, search/filter by year/season.
  - Patch Detail — summary card (PatchSummaryDTO), lists activities, expenses, inputs, yields. Buttons to add Activity / Expense / Yield.
  - New Activity — form (activityType dropdown, date picker, patch selector, inputs section for seed/fert/pesticide, cost fields)
  - New Expense — form with category, amount, expenseDate, patch selector, optional linked activity
  - New Yield — form (harvestDate, yieldAmount, unit, marketPrice, patch selector)
  - Compare Patches — UI to select 2–4 patches and render comparison charts and table

- Components
  - PatchCard (summary at top)
  - ActivityList, ActivityForm
  - ExpenseList, ExpenseForm
  - YieldList, YieldForm
  - InputList, InputForm (if endpoint added)
  - Charts: BarChart (yield vs patch), StackedBar (expense breakdown), LineChart (yield trend), Table (ROI comparison)

UX / validation rules (important)

- When creating any record that belongs to a patch, require a `patchId` selection (unless using a default patch). This prevents orphaned records and improves analytics.
- Validate numeric inputs (amounts, quantities) to be positive. Use server validation messages to show inline errors.
- For yield entries, require `unit` (kg, bags) and `areaHarvested` if the app wants to compute yield per area.
- Keep forms short and mobile-friendly: group optional details under an "Advanced" collapsible section (soil conditions, weather notes, equipment cost).

Data flow & caching tips

- Cache `MaizePatch` list in local state so forms can quickly show the patch selector without requesting every time.
- When creating records, also cache/optimistically update the patch summary totals (local increment of totalExpenses/totalYield) to make UI feel responsive; reconcile with server response.

Charts & visualizations (suggested KPIs)

- Patch Summary Card (single patch):
  - Total yield (kg)
  - Total expenses (currency)
  - Cost per kg (currency/kg)
  - Profit and profit per kg
  - ROI %

- Comparison View (2–4 patches):
  - Bar chart: yield per patch
  - Stacked bar: expenses by category per patch
  - Table: seed var, fertilizer used, totalExpense, totalYield, cost/kg, profit/kg, ROI
  - Highlight recommended patch (highest ROI)

Migration and data hygiene notes for frontend

- After the backend migration, existing historical records may not have `patchId`. Provide a UI page for "Map records to patches" where the farmer can:
  - Select a date-range and patch to bulk-assign records
  - For ambiguous records, allow manual assign
  - Export CSV of unmapped records for offline review

Accessibility & localization

- Use large touch targets for mobile (>=44px). Localize units and currencies. Support right-to-left if planning internationalization.

Testing recommendations

- Add end-to-end tests that:
  - Create a patch -> Create activities/expenses/yields under it -> Fetch patch summary and assert aggregated values
  - Compare patches endpoint returns expected fields and numerical values

Roadmap & enhancements (frontend-visible)

1. Add InputUsage API and UI (detailed product lookup, supplier info, batch numbers)
2. Add a guided "Harvest insights" modal that suggests actions based on ROI (e.g., try different fertilizer next season)
3. Add CSV export for patch summaries and expense reports
4. Add offline-first support for field data capture (sync when online)

Contact & next steps

If you want, I can also:
- Generate an OpenAPI spec (if you don't already expose it via SpringDoc) with all the recordskeeping endpoints and example schemas.
- Create a small React starter app (TypeScript + Vite) that implements Patch list, Patch detail, and Compare Patches view wired to these APIs.

---
This guide should give your frontend team a clear path for building an effective recordskeeping and analytics UX for maize farmers. Tell me which part you'd like next (OpenAPI spec, React starter, InputUsage endpoint), and I'll scaffold it for you.

================================================================================
DTO Reference & Schema Documentation
================================================================================

This section documents every request and response DTO used in the records keeping and analytics APIs. Use this as a reference when building forms, handling responses, and validating user input on the frontend.


1. MaizePatchDTO (Request/Response)

Used by:
- POST /api/patches (request & response)
- GET /api/patches (response, array)
- GET /api/patches/{id} (response)

Fields:

| Field | Type | Nullable | Validation | Description |
|-------|------|----------|-----------|-------------|
| id | Long | Y | - | Patch unique identifier (server-generated) |
| farmerProfileId | Long | Y | - | Farmer's user ID (auto-set by server) |
| year | Integer | N | Required | Planting year (e.g., 2025) |
| season | String | N | Required | Season code (e.g., "LONG_RAIN", "SHORT_RAIN", "DRY") |
| name | String | N | Required | Friendly name (e.g., "Block A - 2025") |
| cropType | String | N | Required | Crop type (for maize focus, use "Maize") |
| area | Double | Y | - | Plot area size |
| areaUnit | String | Y | - | Area unit ("ha", "acres", "m2") |
| plantingDate | LocalDate | Y | - | Planting date (YYYY-MM-DD) |
| expectedHarvestDate | LocalDate | Y | - | Expected harvest date (YYYY-MM-DD) |
| actualHarvestDate | LocalDate | Y | - | Actual harvest date (YYYY-MM-DD) |
| location | String | Y | - | Physical location (e.g., "Block A", "Field 1") |
| notes | String | Y | - | Free-text notes |
| createdAt | LocalDateTime | Y | - | Server timestamp (read-only) |
| updatedAt | LocalDateTime | Y | - | Server timestamp (read-only) |

Example (Request):
```json
{
  "year": 2025,
  "season": "LONG_RAIN",
  "name": "Block A - 2025",
  "cropType": "Maize",
  "area": 1.25,
  "areaUnit": "ha",
  "plantingDate": "2025-03-10",
  "expectedHarvestDate": "2025-08-20",
  "location": "Block A",
  "notes": "Primary planting area"
}
```

Example (Response):
```json
{
  "id": 3,
  "farmerProfileId": 1,
  "year": 2025,
  "season": "LONG_RAIN",
  "name": "Block A - 2025",
  "cropType": "Maize",
  "area": 1.25,
  "areaUnit": "ha",
  "plantingDate": "2025-03-10",
  "expectedHarvestDate": "2025-08-20",
  "actualHarvestDate": null,
  "location": "Block A",
  "notes": "Primary planting area",
  "createdAt": "2025-03-09T14:30:00",
  "updatedAt": "2025-03-09T14:30:00"
}
```


2. FarmActivityRequest (Request) / FarmActivityResponse (Response)

Used by:
- POST /api/farm-activities (request)
- GET /api/farm-activities, GET /api/farm-activities/{id} (response)
- PUT /api/farm-activities/{id} (request & response)

Request Fields (FarmActivityRequest):

| Field | Type | Nullable | Validation | Description |
|-------|------|----------|-----------|-------------|
| activityType | String | N | Required | Activity type ("PLANTING", "WEEDING", "FERTILIZING", "TOP_DRESSING", "SPRAYING", "HARVESTING", etc.) |
| cropType | String | N | Required | Crop type (e.g., "Maize") |
| activityDate | LocalDate | N | Required | Date activity was performed (YYYY-MM-DD) |
| description | String | Y | - | Brief description of activity |
| areaSize | Double | Y | - | Area covered by activity |
| units | String | Y | - | Area unit ("ha", "acres") |
| yield | Double | Y | - | Yield amount (for harvest activities) |
| cost | BigDecimal | Y | Positive | Total cost of activity |
| productUsed | String | Y | - | Product name (e.g., "Urea", "Pesticide X") |
| applicationRate | Double | Y | - | Rate of application (e.g., kg/ha) |
| weatherConditions | String | Y | - | Weather description ("sunny", "rainy", "cloudy", etc.) |
| soilConditions | String | Y | - | Soil state ("wet", "dry", "well-drained") |
| notes | String | Y | - | Free-text notes |
| location | String | Y | - | Specific field/plot location |
| laborHours | Integer | Y | Positive | Labor hours spent |
| equipmentUsed | String | Y | - | Equipment name/type |
| laborCost | BigDecimal | Y | Positive | Labor cost |
| equipmentCost | BigDecimal | Y | Positive | Equipment rental/usage cost |
| seedVarietyId | Long | Y | - | Reference to seed variety (if planting) |
| seedVarietyName | String | Y | - | Cached seed variety name |
| fertilizerProductId | Long | Y | - | Reference to fertilizer product (if fertilizing) |
| fertilizerProductName | String | Y | - | Cached fertilizer name |
| pesticideProductId | Long | Y | - | Reference to pesticide (if spraying) |
| pesticideProductName | String | Y | - | Cached pesticide name |
| patchId | Long | Y | Recommended | Patch this activity belongs to |

Response Fields (FarmActivityResponse):

Includes all request fields plus:

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Activity unique identifier (server-generated) |
| patchName | String | Cached patch name for quick display |
| createdAt | LocalDateTime | Server timestamp (read-only) |
| updatedAt | LocalDateTime | Server timestamp (read-only) |
| yieldTrend | String | Trend analysis ("INCREASE", "DECREASE", "STEADY") |
| percentageChange | Double | Percentage change vs. previous activity |
| possibleReasons | String | Suggested explanation for trend |

Example (Request):
```json
{
  "activityType": "FERTILIZING",
  "cropType": "Maize",
  "activityDate": "2025-04-15",
  "description": "Top-dressing with Urea",
  "areaSize": 1.25,
  "units": "ha",
  "productUsed": "Urea",
  "applicationRate": 40.0,
  "weatherConditions": "sunny",
  "soilConditions": "moist",
  "notes": "Applied after good rains",
  "laborHours": 3,
  "laborCost": 300.00,
  "fertilizerProductId": 5,
  "fertilizerProductName": "Urea",
  "patchId": 3
}
```

Example (Response):
```json
{
  "id": 45,
  "activityType": "FERTILIZING",
  "cropType": "Maize",
  "activityDate": "2025-04-15",
  "description": "Top-dressing with Urea",
  "areaSize": 1.25,
  "units": "ha",
  "productUsed": "Urea",
  "applicationRate": 40.0,
  "weatherConditions": "sunny",
  "soilConditions": "moist",
  "notes": "Applied after good rains",
  "laborHours": 3,
  "equipmentUsed": null,
  "laborCost": 300.00,
  "equipmentCost": null,
  "fertilizerProductId": 5,
  "fertilizerProductName": "Urea",
  "patchId": 3,
  "patchName": "Block A - 2025",
  "createdAt": "2025-04-15T10:00:00",
  "updatedAt": "2025-04-15T10:00:00",
  "yieldTrend": null,
  "percentageChange": null,
  "possibleReasons": null
}
```


3. FarmExpenseRequest (Request) / FarmExpenseResponse (Response)

Used by:
- POST /api/farm-expenses (request)
- GET /api/farm-expenses, GET /api/farm-expenses/{id} (response)
- PUT /api/farm-expenses/{id} (request & response)

Request Fields (FarmExpenseRequest):

| Field | Type | Nullable | Validation | Description |
|-------|------|----------|-----------|-------------|
| cropType | String | N | Required | Crop type (e.g., "Maize") |
| category | String | N | Required | Expense category ("SEEDS", "FERTILIZER", "PESTICIDES", "LABOR", "EQUIPMENT", "TRANSPORT", "IRRIGATION", "STORAGE", "MARKETING", "ADMINISTRATIVE", "MAINTENANCE", "OTHER") |
| description | String | N | Required | What was purchased/paid for |
| amount | BigDecimal | N | Required, Positive | Cost amount |
| expenseDate | LocalDate | N | Required | Date expense was incurred (YYYY-MM-DD) |
| supplier | String | Y | - | Supplier/vendor name |
| invoiceNumber | String | Y | - | Invoice or receipt number |
| paymentMethod | String | Y | - | Payment method ("CASH", "MPESA", "CHEQUE", "TRANSFER") |
| notes | String | Y | - | Free-text notes |
| growthStage | String | Y | - | Growth stage ("PRE_PLANTING", "PLANTING", "EARLY_GROWTH", "VEGETATIVE", "FLOWERING", "FRUITING", "MATURITY", "HARVEST", "POST_HARVEST", "STORAGE") |
| farmActivityId | Long | Y | - | Link to farm activity (if applicable) |
| patchId | Long | Y | Recommended | Patch this expense belongs to |
| isRecurring | Boolean | Y | - | Whether expense is recurring |
| recurringFrequency | String | Y | - | Frequency if recurring ("WEEKLY", "MONTHLY", "QUARTERLY") |

Response Fields (FarmExpenseResponse):

Includes all request fields plus:

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Expense unique identifier (server-generated) |
| patchName | String | Cached patch name |
| createdAt | LocalDateTime | Server timestamp (read-only) |
| updatedAt | LocalDateTime | Server timestamp (read-only) |

Example (Request):
```json
{
  "cropType": "Maize",
  "category": "FERTILIZER",
  "description": "Urea 50kg bags",
  "amount": 2500.00,
  "expenseDate": "2025-04-10",
  "supplier": "Agro-Deals Ltd",
  "invoiceNumber": "INV-2025-0456",
  "paymentMethod": "MPESA",
  "notes": "Top-dressing phase",
  "growthStage": "VEGETATIVE",
  "patchId": 3
}
```

Example (Response):
```json
{
  "id": 102,
  "cropType": "Maize",
  "category": "FERTILIZER",
  "description": "Urea 50kg bags",
  "amount": 2500.00,
  "expenseDate": "2025-04-10",
  "supplier": "Agro-Deals Ltd",
  "invoiceNumber": "INV-2025-0456",
  "paymentMethod": "MPESA",
  "notes": "Top-dressing phase",
  "growthStage": "VEGETATIVE",
  "farmActivityId": null,
  "patchId": 3,
  "patchName": "Block A - 2025",
  "isRecurring": false,
  "recurringFrequency": null,
  "createdAt": "2025-04-10T15:00:00",
  "updatedAt": "2025-04-10T15:00:00"
}
```


4. YieldRecordRequest (Request) / YieldRecordResponse (Response)

Used by:
- POST /api/yield-records (request)
- GET /api/yield-records, GET /api/yield-records/{id} (response)
- PUT /api/yield-records/{id} (request & response)

Request Fields (YieldRecordRequest):

| Field | Type | Nullable | Validation | Description |
|-------|------|----------|-----------|-------------|
| cropType | String | N | Required | Crop type (e.g., "Maize") |
| harvestDate | LocalDate | N | Required | Harvest date (YYYY-MM-DD) |
| yieldAmount | BigDecimal | N | Required, Positive | Total yield harvested |
| unit | String | N | Required | Yield unit ("kg", "bags", "tons", "liters") |
| areaHarvested | BigDecimal | Y | - | Area harvested (in hectares/acres) |
| marketPrice | BigDecimal | Y | Positive | Price per unit (currency) |
| qualityGrade | String | Y | - | Quality grade ("A", "B", "C", "Grade 1", "Grade 2") |
| storageLocation | String | Y | - | Where yield is stored |
| buyer | String | Y | - | Buyer name/company |
| notes | String | Y | - | Free-text notes |
| farmActivityId | Long | Y | - | Link to harvest activity |
| patchId | Long | Y | Recommended | Patch this yield belongs to |

Response Fields (YieldRecordResponse):

Includes all request fields plus:

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Yield record unique identifier (server-generated) |
| patchName | String | Cached patch name |
| seedVarietyId | Long | Which seed was planted |
| seedVarietyName | String | Seed variety name |
| primaryFertilizerId | Long | Main fertilizer used |
| primaryPesticideId | Long | Main pesticide used |
| fertilizerProductName | String | Fertilizer name |
| pesticideProductName | String | Pesticide name |
| soilConditionAtHarvest | String | Soil state at harvest |
| weatherDuringGrowth | String | Weather summary |
| estimatedInputEffectiveness | Integer | 1–5 rating of inputs |
| totalInputCost | BigDecimal | Total spent on inputs |
| costPerKgProduced | BigDecimal | Input cost divided by yield |
| profitPerKg | BigDecimal | Revenue minus cost per kg |
| totalRevenue | BigDecimal | yieldAmount × marketPrice |
| createdAt | LocalDateTime | Server timestamp (read-only) |
| updatedAt | LocalDateTime | Server timestamp (read-only) |

Example (Request):
```json
{
  "cropType": "Maize",
  "harvestDate": "2025-08-20",
  "yieldAmount": 2000.00,
  "unit": "kg",
  "areaHarvested": 1.25,
  "marketPrice": 40.00,
  "qualityGrade": "A",
  "storageLocation": "Farm store",
  "buyer": "Local aggregator",
  "notes": "Good harvest despite late rains",
  "patchId": 3
}
```

Example (Response):
```json
{
  "id": 78,
  "cropType": "Maize",
  "harvestDate": "2025-08-20",
  "yieldAmount": 2000.00,
  "unit": "kg",
  "areaHarvested": 1.25,
  "marketPrice": 40.00,
  "qualityGrade": "A",
  "storageLocation": "Farm store",
  "buyer": "Local aggregator",
  "notes": "Good harvest despite late rains",
  "farmActivityId": 89,
  "patchId": 3,
  "patchName": "Block A - 2025",
  "seedVarietyId": 12,
  "seedVarietyName": "H511",
  "primaryFertilizerId": 5,
  "primaryPesticideId": null,
  "fertilizerProductName": "Urea",
  "pesticideProductName": null,
  "soilConditionAtHarvest": "well-drained",
  "weatherDuringGrowth": "Mixed; good early, late rains",
  "estimatedInputEffectiveness": 4,
  "totalInputCost": 4200.00,
  "costPerKgProduced": 2.10,
  "profitPerKg": 38.10,
  "totalRevenue": 80000.00,
  "createdAt": "2025-08-20T16:00:00",
  "updatedAt": "2025-08-20T16:00:00"
}
```


5. InputUsageRecordDTO

Used by:
- (No dedicated public endpoint yet; currently recorded within FarmActivityRequest)
- Returned within PatchSummaryDTO as summaries

Fields:

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | Long | Y | Input usage record unique identifier |
| farmActivityId | Long | Y | Activity where input was used |
| yieldRecordId | Long | Y | Yield record linked to this input |
| patchId | Long | Y | Patch this input belongs to |
| patchName | String | Y | Cached patch name |
| inputType | String | N | Input type ("SEED", "FERTILIZER", "PESTICIDE", "HERBICIDE", "FUNGICIDE", "INSECTICIDE", "SOIL_AMENDMENT", "BIOFERTILIZER", "OTHER") |
| seedVarietyId | Long | Y | Seed reference (if inputType == SEED) |
| fertilizerProductId | Long | Y | Fertilizer reference (if inputType == FERTILIZER) |
| pesticideProductId | Long | Y | Pesticide reference (if inputType == PESTICIDE) |
| quantityUsed | BigDecimal | Y | Amount of input used |
| unit | String | N | Unit of measurement ("kg", "liters", "packets", "bags") |
| applicationDate | LocalDate | N | Date input was applied |
| costOfUsage | BigDecimal | Y | Cost of this input |
| applicationRate | BigDecimal | Y | Rate of application (e.g., kg/ha) |
| effectivenessRating | Integer | Y | Farmer's 1–5 rating of input effectiveness |
| visibleResults | String | Y | Farmer's observations (e.g., "plants looked greener") |
| contributedToHighYield | Boolean | Y | Did this help achieve high yield? |
| estimatedYieldContributionPercent | Integer | Y | Farmer's estimate of % contribution (0–100) |
| notes | String | Y | Additional notes |
| problemsEncountered | String | Y | Any issues with this input |


6. PatchSummaryDTO (Analytics Response)

Used by:
- GET /api/farm-analytics/patches/{patchId}/summary (response)

Fields:

| Field | Type | Description |
|-------|------|-------------|
| patchId | Long | Patch unique identifier |
| patchName | String | Patch name (e.g., "Block A - 2025") |
| year | Integer | Planting year |
| season | String | Season (e.g., "LONG_RAIN") |
| cropType | String | Crop type ("Maize") |
| area | Double | Patch area |
| areaUnit | String | Area unit |
| totalExpenses | BigDecimal | Sum of all expenses for patch |
| totalYield | BigDecimal | Sum of all yield amounts |
| totalRevenue | BigDecimal | Sum of all revenue |
| costPerKg | BigDecimal | totalExpenses ÷ totalYield |
| profit | BigDecimal | totalRevenue − totalExpenses |
| profitPerKg | BigDecimal | profit ÷ totalYield |
| roiPercentage | BigDecimal | (profit ÷ totalExpenses) × 100 |
| activityTypes | String[] | List of activity types performed ("PLANTING", "WEEDING", "FERTILIZING", etc.) |
| inputSummaries | String[] | List of input summaries (e.g., "SEED H511 qty=10kg", "FERTILIZER Urea 50kg") |
| expenseSummaries | String[] | List of expense summaries (e.g., "FERTILIZER: 2500 - Urea 50kg bags") |

Example (Response):
```json
{
  "patchId": 3,
  "patchName": "Block A - 2025",
  "year": 2025,
  "season": "LONG_RAIN",
  "cropType": "Maize",
  "area": 1.25,
  "areaUnit": "ha",
  "totalExpenses": 4200.00,
  "totalYield": 2000.00,
  "totalRevenue": 80000.00,
  "costPerKg": 2.10,
  "profit": 75800.00,
  "profitPerKg": 37.90,
  "roiPercentage": 1804.76,
  "activityTypes": ["PLANTING", "WEEDING", "FERTILIZING", "TOP_DRESSING", "HARVESTING"],
  "inputSummaries": [
    "SEED H511 qty=10kg",
    "FERTILIZER Urea 50kg",
    "PESTICIDE Actellic 2L"
  ],
  "expenseSummaries": [
    "SEEDS: 1200 - H511 seeds 10kg",
    "FERTILIZER: 2500 - Urea 50kg bags",
    "PESTICIDES: 500 - Actellic 2L bottle"
  ]
}
```


7. PatchComparisonDTO (Analytics Response)

Used by:
- POST /api/farm-analytics/patches/compare (response)

Fields:

| Field | Type | Description |
|-------|------|-------------|
| patches | PatchSummaryDTO[] | Array of patch summaries for comparison |

Example (Response):
```json
{
  "patches": [
    {
      "patchId": 3,
      "patchName": "Block A - 2025",
      "year": 2025,
      "cropType": "Maize",
      "totalExpenses": 4200.00,
      "totalYield": 2000.00,
      "totalRevenue": 80000.00,
      "costPerKg": 2.10,
      "profit": 75800.00,
      "roiPercentage": 1804.76
    },
    {
      "patchId": 5,
      "patchName": "Block B - 2024",
      "year": 2024,
      "cropType": "Maize",
      "totalExpenses": 3800.00,
      "totalYield": 1500.00,
      "totalRevenue": 60000.00,
      "costPerKg": 2.53,
      "profit": 56200.00,
      "roiPercentage": 1478.95
    }
  ]
}
```


Frontend Form Validation Reference

Use these validation rules when building forms:

- Required fields: Show error if empty
- Numeric fields (amount, quantity, area): Validate >= 0 (positive for amounts/quantities)
- Date fields: Validate is valid date, not in future (except expectedHarvestDate)
- Enum fields (activityType, category, inputType): Show dropdown/select with predefined options
- patchId: Make mandatory or pre-select a default patch to prevent orphaned records

Enums (predefined values)

ActivityType:
- LAND_PREPARATION, PLANTING, SEEDING, TRANSPLANTING, IRRIGATION
- FERTILIZING, TOP_DRESSING, SPRAYING, PEST_CONTROL, DISEASE_CONTROL
- WEEDING, PRUNING, THINNING, HARVESTING, YIELD_RECORDING
- STORAGE, TRANSPORT, MARKETING, MAINTENANCE, OTHER

ExpenseCategory:
- SEEDS, FERTILIZER, PESTICIDES, LABOR, EQUIPMENT
- TRANSPORT, IRRIGATION, STORAGE, MARKETING, ADMINISTRATIVE
- MAINTENANCE, OTHER

GrowthStage:
- PRE_PLANTING, PLANTING, EARLY_GROWTH, VEGETATIVE, FLOWERING
- FRUITING, MATURITY, HARVEST, POST_HARVEST, STORAGE

InputType:
- SEED, FERTILIZER, PESTICIDE, HERBICIDE, FUNGICIDE
- INSECTICIDE, SOIL_AMENDMENT, BIOFERTILIZER, OTHER

Season:
- LONG_RAIN, SHORT_RAIN, DRY


Error Handling (common HTTP status codes)

- 200 OK: Request succeeded, response body contains result
- 201 Created: Resource created successfully (usually for POST)
- 204 No Content: Request succeeded, no response body (DELETE)
- 400 Bad Request: Validation error, check response body for details
- 401 Unauthorized: Missing or invalid token
- 403 Forbidden: User does not own this resource
- 404 Not Found: Resource does not exist
- 500 Internal Server Error: Server error, check logs

Error response example:
```json
{
  "message": "Validation failed",
  "errors": {
    "amount": "Amount must be positive",
    "expenseDate": "Expense date cannot be in future"
  }
}
```

---

Use this DTO reference alongside the API Reference section above to build robust frontend forms and handle all responses correctly.
