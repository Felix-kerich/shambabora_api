# ShambaBora Farm Management System

## Overview
A comprehensive farm management system that allows farmers to track activities, expenses, yields, and generate analytics for better decision-making.

## Features

### 1. Farm Activity Management
- **Enhanced Activity Types**: 20+ activity types from land preparation to marketing
- **Detailed Tracking**: Weather conditions, soil conditions, labor hours, equipment used
- **Cost Tracking**: Labor costs, equipment costs, and total activity costs
- **Location Tracking**: Specific field/plot location for each activity

### 2. Expense Management
- **Categorized Expenses**: Seeds, fertilizer, pesticides, labor, equipment, transport, etc.
- **Growth Stage Tracking**: Track expenses by crop growth stage (pre-planting to storage)
- **Supplier Management**: Track suppliers, invoice numbers, payment methods
- **Recurring Expenses**: Support for recurring expense tracking
- **Activity Linking**: Link expenses to specific farm activities

### 3. Yield Recording
- **Comprehensive Yield Data**: Amount, area harvested, yield per unit
- **Market Information**: Market price, total revenue, buyer information
- **Quality Tracking**: Quality grades, storage location
- **Performance Metrics**: Best yield, average yield, yield trends

### 4. Analytics & Reporting
- **Financial Analytics**: Total expenses, revenue, profit margins
- **Yield Analytics**: Yield trends, performance comparisons
- **Expense Breakdown**: By category and growth stage
- **Profitability Analysis**: By crop and time period
- **Recommendations**: AI-powered suggestions for improvement

### 5. Dashboard
- **Overview Statistics**: Total activities, expenses, yields
- **Financial Summary**: Revenue, expenses, profit margins
- **Crop Summaries**: Performance by crop type
- **Recent Activities**: Latest activities, expenses, yields
- **Upcoming Reminders**: Task reminders and notifications

## API Endpoints

### Farm Activities
- `POST /api/farm-activities` - Create activity
- `GET /api/farm-activities/{id}` - Get activity
- `PUT /api/farm-activities/{id}` - Update activity
- `DELETE /api/farm-activities/{id}` - Delete activity
- `GET /api/farm-activities` - List activities (with filtering)
- `POST /api/farm-activities/{id}/reminders` - Add reminder
- `GET /api/farm-activities/{id}/reminders` - List reminders
- `GET /api/farm-activities/reminders/upcoming` - Upcoming reminders

### Farm Expenses
- `POST /api/farm-expenses` - Create expense
- `GET /api/farm-expenses/{id}` - Get expense
- `PUT /api/farm-expenses/{id}` - Update expense
- `DELETE /api/farm-expenses/{id}` - Delete expense
- `GET /api/farm-expenses` - List expenses (with filtering)
- `GET /api/farm-expenses/total` - Get total expenses
- `GET /api/farm-expenses/breakdown/category` - Expenses by category
- `GET /api/farm-expenses/breakdown/growth-stage` - Expenses by growth stage

### Yield Records
- `POST /api/yield-records` - Create yield record
- `GET /api/yield-records/{id}` - Get yield record
- `PUT /api/yield-records/{id}` - Update yield record
- `DELETE /api/yield-records/{id}` - Delete yield record
- `GET /api/yield-records` - List yield records (with filtering)
- `GET /api/yield-records/total` - Get total yield
- `GET /api/yield-records/revenue` - Get total revenue
- `GET /api/yield-records/average` - Get average yield per unit
- `GET /api/yield-records/best` - Get best yield per unit
- `GET /api/yield-records/trends` - Get yield trends

### Analytics
- `GET /api/farm-analytics` - Generate comprehensive analytics

### Dashboard
- `GET /api/farm-dashboard` - Get farm dashboard

## Data Models

### FarmActivity
- Enhanced with 20+ activity types
- Weather and soil conditions tracking
- Labor and equipment cost tracking
- Location and notes fields

### FarmExpense
- Categorized expense tracking
- Growth stage association
- Supplier and payment information
- Recurring expense support

### YieldRecord
- Comprehensive yield data
- Market price and revenue tracking
- Quality and storage information
- Performance metrics calculation

## Key Benefits

1. **Professional Record Keeping**: Comprehensive tracking of all farm operations
2. **Financial Management**: Detailed expense and revenue tracking
3. **Performance Analysis**: Yield trends and profitability analysis
4. **Decision Support**: Data-driven recommendations for improvement
5. **Time Management**: Activity reminders and task scheduling
6. **Growth Stage Tracking**: Monitor expenses and activities by crop growth stage
7. **Multi-Crop Support**: Track multiple crops simultaneously
8. **Historical Analysis**: Compare performance across seasons

## Usage Examples

### Creating a Planting Activity
```json
POST /api/farm-activities
{
  "activityType": "PLANTING",
  "cropType": "Maize",
  "activityDate": "2024-03-15",
  "description": "Planting maize seeds",
  "areaSize": 2.5,
  "units": "acres",
  "weatherConditions": "Sunny",
  "soilConditions": "Moist",
  "location": "Field A",
  "laborHours": 8,
  "laborCost": 200.00,
  "equipmentUsed": "Tractor, Planter"
}
```

### Recording an Expense
```json
POST /api/farm-expenses
{
  "cropType": "Maize",
  "category": "SEEDS",
  "description": "Maize seeds purchase",
  "amount": 150.00,
  "expenseDate": "2024-03-10",
  "supplier": "Agro Supplies Ltd",
  "growthStage": "PRE_PLANTING",
  "paymentMethod": "Cash"
}
```

### Recording Yield
```json
POST /api/yield-records
{
  "cropType": "Maize",
  "harvestDate": "2024-08-20",
  "yieldAmount": 5.5,
  "unit": "tons",
  "areaHarvested": 2.5,
  "marketPrice": 300.00,
  "qualityGrade": "Grade A",
  "buyer": "Local Market"
}
```

This system provides farmers with professional-grade farm management capabilities, enabling them to make data-driven decisions and improve their farming operations.
