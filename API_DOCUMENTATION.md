# ShambaBora API Documentation

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080` (Development)  
**Production URL:** `https://api.inventorysystem.com`  
**Contact:** Felix Kerich - felixkipkiruikerich@gmail.com

---

## Table of Contents

1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [Farmer Profile](#farmer-profile)
4. [Farm Activities](#farm-activities)
5. [Farm Expenses](#farm-expenses)
6. [Yield Records](#yield-records)
7. [Farm Dashboard](#farm-dashboard)
8. [Weather](#weather)
9. [Marketplace](#marketplace)
10. [Collaboration](#collaboration)
11. [Error Handling](#error-handling)

---

## Authentication

All authenticated endpoints require a JWT Bearer token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

### Register User
**POST** `/api/auth/register`

Register a new user account.

**Request Body:**
```json
{
  "username": "string (required)",
  "email": "string (required, valid email)",
  "password": "string (required, min 6 characters)",
  "fullName": "string (required)",
  "phoneNumber": "string (optional)",
  "role": "string (optional, defaults to FARMER)"
}
```

**Response:**
```json
{
  "token": "string",
  "username": "string",
  "email": "string",
  "role": "string"
}
```

### Login
**POST** `/api/auth/login`

Authenticate and receive JWT token.

**Request Body:**
```json
{
  "usernameOrEmail": "string (required)",
  "password": "string (required)"
}
```

**Response:**
```json
{
  "token": "string",
  "username": "string",
  "email": "string",
  "role": "string"
}
```

---

## User Management

### Get Current User Profile
**GET** `/api/users/profile`

🔒 **Authentication Required**

Get the authenticated user's profile information.

**Response:**
```json
{
  "id": "number",
  "username": "string",
  "email": "string",
  "fullName": "string",
  "phoneNumber": "string",
  "role": "string"
}
```

### Update Current User
**PUT** `/api/users/profile`

🔒 **Authentication Required**

Update the authenticated user's profile.

**Request Body:**
```json
{
  "fullName": "string (optional)",
  "phoneNumber": "string (optional)",
  "email": "string (optional)"
}
```

### Delete Current User
**DELETE** `/api/users/profile`

🔒 **Authentication Required**

Delete the authenticated user's account.

**Response:** `204 No Content`

---

## Farmer Profile

### Get My Farmer Profile
**GET** `/api/farmer-profile/me`

🔒 **Authentication Required**

Get the current user's detailed farmer profile.

**Response:**
```json
{
  "id": "number",
  "userId": "number",
  "farmName": "string",
  "farmSize": "number",
  "location": "string",
  "primaryCrops": ["string"],
  "farmingExperience": "number",
  "certifications": ["string"]
}
```

### Create Farmer Profile
**POST** `/api/farmer-profile`

🔒 **Authentication Required**

Create a farmer profile for the current user.

**Request Body:**
```json
{
  "farmName": "string (required)",
  "farmSize": "number (optional)",
  "location": "string (optional)",
  "primaryCrops": ["string"] (optional),
  "farmingExperience": "number (optional)",
  "certifications": ["string"] (optional)
}
```

### Update Farmer Profile
**PUT** `/api/farmer-profile/me`

🔒 **Authentication Required**

Update the current user's farmer profile.

---

## Farm Activities

### Create Farm Activity
**POST** `/api/farm-activities`

🔒 **Authentication Required**

Record a new farm activity.

**Request Body:**
```json
{
  "activityType": "string (required)",
  "cropType": "string (required)",
  "activityDate": "date (required, YYYY-MM-DD)",
  "description": "string (optional)",
  "areaSize": "number (optional)",
  "units": "string (optional)",
  "yield": "number (optional)",
  "cost": "number (optional)",
  "productUsed": "string (optional)",
  "applicationRate": "number (optional)",
  "weatherConditions": "string (optional)",
  "soilConditions": "string (optional)",
  "notes": "string (optional)",
  "location": "string (optional)",
  "laborHours": "number (optional)",
  "equipmentUsed": "string (optional)",
  "laborCost": "number (optional)",
  "equipmentCost": "number (optional)"
}
```

**Response:**
```json
{
  "id": "number",
  "activityType": "string",
  "cropType": "string",
  "activityDate": "date",
  "description": "string",
  "createdAt": "timestamp"
}
```

### Get Farm Activity
**GET** `/api/farm-activities/{id}`

🔒 **Authentication Required**

Get a specific farm activity by ID.

### Update Farm Activity
**PUT** `/api/farm-activities/{id}`

🔒 **Authentication Required**

Update a farm activity.

### Delete Farm Activity
**DELETE** `/api/farm-activities/{id}`

🔒 **Authentication Required**

Delete a farm activity.

**Response:** `204 No Content`

### List Farm Activities
**GET** `/api/farm-activities`

🔒 **Authentication Required**

List farm activities with pagination and filtering.

**Query Parameters:**
- `activityType` (optional): Filter by activity type
- `page` (default: 0): Page number
- `size` (default: 10): Items per page

**Response:**
```json
{
  "content": [
    {
      "id": "number",
      "activityType": "string",
      "cropType": "string",
      "activityDate": "date"
    }
  ],
  "totalElements": "number",
  "totalPages": "number",
  "size": "number",
  "number": "number"
}
```

### Add Reminder to Activity
**POST** `/api/farm-activities/{id}/reminders`

🔒 **Authentication Required**

Add a reminder to a farm activity.

**Request Body:**
```json
{
  "reminderDate": "datetime (required)",
  "message": "string (required)",
  "notificationMethod": "string (optional)"
}
```

### List Activity Reminders
**GET** `/api/farm-activities/{id}/reminders`

🔒 **Authentication Required**

Get all reminders for a specific activity.

### List Upcoming Reminders
**GET** `/api/farm-activities/reminders/upcoming`

🔒 **Authentication Required**

Get all upcoming reminders for the current user.

### Export Activity to Calendar
**GET** `/api/farm-activities/{id}/calendar`

🔒 **Authentication Required**

Export a farm activity and its reminders as an iCal (.ics) file.

**Response:** iCal file download

---

## Farm Expenses

### Create Farm Expense
**POST** `/api/farm-expenses`

🔒 **Authentication Required**

Record a new farm expense.

**Request Body:**
```json
{
  "cropType": "string (required)",
  "category": "string (required)",
  "description": "string (required)",
  "amount": "number (required, positive)",
  "expenseDate": "date (required, YYYY-MM-DD)",
  "supplier": "string (optional)",
  "invoiceNumber": "string (optional)",
  "paymentMethod": "string (optional)",
  "notes": "string (optional)",
  "growthStage": "string (optional)",
  "farmActivityId": "number (optional)",
  "isRecurring": "boolean (optional)",
  "recurringFrequency": "string (optional)"
}
```

### Get Farm Expense
**GET** `/api/farm-expenses/{id}`

🔒 **Authentication Required**

### Update Farm Expense
**PUT** `/api/farm-expenses/{id}`

🔒 **Authentication Required**

### Delete Farm Expense
**DELETE** `/api/farm-expenses/{id}`

🔒 **Authentication Required**

### List Farm Expenses
**GET** `/api/farm-expenses`

🔒 **Authentication Required**

**Query Parameters:**
- `cropType` (optional): Filter by crop type
- `category` (optional): Filter by expense category
- `page` (default: 0)
- `size` (default: 10)

### Get Total Expenses
**GET** `/api/farm-expenses/total`

🔒 **Authentication Required**

Get total expenses for a specific crop or all crops.

**Query Parameters:**
- `cropType` (optional): Filter by crop type

**Response:**
```json
1250.50
```

### Get Expenses by Category
**GET** `/api/farm-expenses/breakdown/category`

🔒 **Authentication Required**

Get expenses breakdown by category for a specific crop.

**Query Parameters:**
- `cropType` (required)

**Response:**
```json
{
  "Seeds": 500.00,
  "Fertilizer": 350.50,
  "Labor": 400.00
}
```

### Get Expenses by Growth Stage
**GET** `/api/farm-expenses/breakdown/growth-stage`

🔒 **Authentication Required**

**Query Parameters:**
- `cropType` (required)

---

## Yield Records

### Create Yield Record
**POST** `/api/yield-records`

🔒 **Authentication Required**

Record a harvest/yield.

**Request Body:**
```json
{
  "cropType": "string (required)",
  "harvestDate": "date (required, YYYY-MM-DD)",
  "yieldAmount": "number (required, positive)",
  "unit": "string (required)",
  "areaHarvested": "number (optional)",
  "marketPrice": "number (optional)",
  "qualityGrade": "string (optional)",
  "storageLocation": "string (optional)",
  "buyer": "string (optional)",
  "notes": "string (optional)",
  "farmActivityId": "number (optional)"
}
```

### Get Yield Record
**GET** `/api/yield-records/{id}`

🔒 **Authentication Required**

### Update Yield Record
**PUT** `/api/yield-records/{id}`

🔒 **Authentication Required**

### Delete Yield Record
**DELETE** `/api/yield-records/{id}`

🔒 **Authentication Required**

### List Yield Records
**GET** `/api/yield-records`

🔒 **Authentication Required**

**Query Parameters:**
- `cropType` (optional)
- `page` (default: 0)
- `size` (default: 10)

### Get Total Yield
**GET** `/api/yield-records/total`

🔒 **Authentication Required**

**Query Parameters:**
- `cropType` (optional)

**Response:**
```json
5000.00
```

### Get Total Revenue
**GET** `/api/yield-records/revenue`

🔒 **Authentication Required**

**Query Parameters:**
- `cropType` (optional)

### Get Average Yield Per Unit
**GET** `/api/yield-records/average`

🔒 **Authentication Required**

**Query Parameters:**
- `cropType` (required)

### Get Best Yield Per Unit
**GET** `/api/yield-records/best`

🔒 **Authentication Required**

**Query Parameters:**
- `cropType` (required)

### Get Yield Trends
**GET** `/api/yield-records/trends`

🔒 **Authentication Required**

Get yield trends over time for a specific crop.

**Query Parameters:**
- `cropType` (required)
- `startDate` (optional, YYYY-MM-DD)
- `endDate` (optional, YYYY-MM-DD)

**Response:**
```json
[
  {
    "id": "number",
    "cropType": "string",
    "harvestDate": "date",
    "yieldAmount": "number",
    "unit": "string"
  }
]
```

---

## Farm Dashboard

### Get Farm Dashboard
**GET** `/api/farm-dashboard`

🔒 **Authentication Required**

Get comprehensive farm dashboard with all key metrics and recent activities.

**Response:**
```json
{
  "totalActivities": "number",
  "totalExpenses": "number",
  "totalRevenue": "number",
  "totalYield": "number",
  "recentActivities": [],
  "expensesByCategory": {},
  "yieldByCrop": {},
  "upcomingReminders": []
}
```

---

## Weather

### Get Current Weather
**GET** `/api/weather/current`

Get current weather for a location.

**Query Parameters:**
- `location` (required): City name or coordinates

**Response:**
```json
{
  "location": "string",
  "temperature": "number",
  "humidity": "number",
  "description": "string",
  "windSpeed": "number",
  "timestamp": "datetime"
}
```

### Get Weather Forecast
**GET** `/api/weather/forecast`

Get weather forecast for a location.

**Query Parameters:**
- `location` (required)

### Get Daily Forecast
**GET** `/api/weather/forecast/daily`

Get daily weather forecast (7-16 days).

**Query Parameters:**
- `location` (required)
- `days` (default: 7): Number of days (7-16)

### Get Monthly Statistics
**GET** `/api/weather/forecast/monthly`

Get monthly weather statistics (requires paid plan).

**Query Parameters:**
- `location` (required)
- `month` (required): Month number (1-12)

---

## Marketplace

### Products

#### Create Product
**POST** `/api/marketplace/products`

🔒 **Authentication Required**

List a product for sale.

**Request Body:**
```json
{
  "name": "string (required)",
  "description": "string (required)",
  "category": "string (required)",
  "price": "number (required)",
  "quantity": "number (required)",
  "unit": "string (required)",
  "imageUrl": "string (optional)",
  "location": "string (optional)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Product created",
  "data": {
    "id": "number",
    "name": "string",
    "price": "number",
    "available": true
  }
}
```

#### Update Product
**PUT** `/api/marketplace/products/{id}`

🔒 **Authentication Required**

#### Set Product Availability
**PATCH** `/api/marketplace/products/{id}/availability`

🔒 **Authentication Required**

**Query Parameters:**
- `available` (required): true or false

#### Get Product
**GET** `/api/marketplace/products/{id}`

#### Search Products
**GET** `/api/marketplace/products`

**Query Parameters:**
- `q` (optional): Search query
- `page` (default: 0)
- `size` (default: 10)

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10
  }
}
```

### Orders

#### Place Order
**POST** `/api/marketplace/orders`

🔒 **Authentication Required**

**Request Body:**
```json
{
  "productId": "number (required)",
  "quantity": "number (required)",
  "buyerId": "number (required)",
  "deliveryAddress": "string (optional)"
}
```

#### Update Order Status
**PATCH** `/api/marketplace/orders/{id}/status`

🔒 **Authentication Required**

**Query Parameters:**
- `status` (required): PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

#### Get Orders by Buyer
**GET** `/api/marketplace/orders/buyer/{buyerId}`

🔒 **Authentication Required**

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 10)

#### Get Orders by Seller
**GET** `/api/marketplace/orders/seller/{sellerId}`

🔒 **Authentication Required**

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 10)

---

## Collaboration

### Posts

#### Create Post
**POST** `/api/collaboration/posts`

🔒 **Authentication Required**

Create a community post.

**Headers:**
- `X-User-Id`: User ID (required)

**Request Body:**
```json
{
  "content": "string (required)",
  "groupId": "number (optional)",
  "imageUrl": "string (optional)",
  "tags": ["string"] (optional)
}
```

#### Get Feed
**GET** `/api/collaboration/posts/feed`

🔒 **Authentication Required**

Get personalized feed of posts.

**Headers:**
- `X-User-Id`: User ID (required)

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 20)

#### Get Group Posts
**GET** `/api/collaboration/posts/group/{groupId}`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

#### Like Post
**POST** `/api/collaboration/posts/{postId}/like`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

#### Unlike Post
**DELETE** `/api/collaboration/posts/{postId}/like`

🔒 **Authentication Required**

#### Add Comment
**POST** `/api/collaboration/posts/{postId}/comments`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

**Request Body:**
```json
{
  "content": "string (required)"
}
```

#### Get Post Comments
**GET** `/api/collaboration/posts/{postId}/comments`

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 10)

#### Flag Post
**POST** `/api/collaboration/posts/{postId}/flag`

🔒 **Authentication Required**

Flag a post for admin review.

**Headers:**
- `X-User-Id`: User ID (required)

**Query Parameters:**
- `reason` (optional): Reason for flagging

### Direct Messages

#### Send Message
**POST** `/api/collaboration/direct-messages`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Sender ID (required)

**Request Body:**
```json
{
  "recipientId": "number (required)",
  "content": "string (required)",
  "attachmentUrl": "string (optional)"
}
```

#### Get Conversation
**GET** `/api/collaboration/direct-messages/conversation/{otherUserId}`

🔒 **Authentication Required**

Get messages between current user and another user.

**Headers:**
- `X-User-Id`: Current user ID (required)

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 50)

#### Get Recent Conversations
**GET** `/api/collaboration/direct-messages/conversations`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

#### Mark Message as Read
**POST** `/api/collaboration/direct-messages/read/{messageId}`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

#### Get Messages After Timestamp
**GET** `/api/collaboration/direct-messages/conversation/{otherUserId}/after`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Current user ID (required)

**Query Parameters:**
- `since` (required): ISO 8601 timestamp

#### Get Conversation Partners
**GET** `/api/collaboration/direct-messages/partners`

🔒 **Authentication Required**

Get list of user IDs with whom you have conversations.

**Headers:**
- `X-User-Id`: User ID (required)

### Groups

#### Create Group
**POST** `/api/collaboration/groups`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Owner ID (required)

**Request Body:**
```json
{
  "name": "string (required)",
  "description": "string (optional)",
  "isPrivate": "boolean (optional)",
  "category": "string (optional)"
}
```

#### Add Member
**POST** `/api/collaboration/groups/{groupId}/members`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Inviter ID (required)

**Query Parameters:**
- `userId` (required): ID of user to add

#### Remove Member
**DELETE** `/api/collaboration/groups/{groupId}/members/{userId}`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Remover ID (required)

#### Update Member Role
**PUT** `/api/collaboration/groups/{groupId}/members/{userId}/role`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Updater ID (required)

**Query Parameters:**
- `role` (required): OWNER, ADMIN, MODERATOR, MEMBER

#### Suspend Member
**POST** `/api/collaboration/groups/{groupId}/members/{userId}/suspend`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Suspender ID (required)

#### Get Group Members
**GET** `/api/collaboration/groups/{groupId}/members`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Requester ID (required)

#### Get My Groups
**GET** `/api/collaboration/groups/my-groups`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

#### Browse Groups
**GET** `/api/collaboration/groups/browse`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

**Query Parameters:**
- `search` (optional): Search term
- `page` (default: 0)
- `size` (default: 20)

#### Join Group
**POST** `/api/collaboration/groups/{groupId}/join`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

#### Leave Group
**DELETE** `/api/collaboration/groups/{groupId}/leave`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

#### Get Group Details
**GET** `/api/collaboration/groups/{groupId}`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: User ID (required)

#### Delete Group
**DELETE** `/api/collaboration/groups/{groupId}`

🔒 **Authentication Required**

**Headers:**
- `X-User-Id`: Requester ID (required)

### Group Messages

#### Send Group Message
**POST** `/api/collaboration/messages`

🔒 **Authentication Required**

**Request Body:**
```json
{
  "groupId": "number (required)",
  "senderId": "number (required)",
  "content": "string (required)",
  "attachmentUrl": "string (optional)"
}
```

#### List Group Messages
**GET** `/api/collaboration/messages`

🔒 **Authentication Required**

**Query Parameters:**
- `groupId` (required)
- `page` (default: 0)
- `size` (default: 20)

---

## Error Handling

### Standard Error Response

```json
{
  "success": false,
  "message": "Error description",
  "error": "Detailed error message",
  "timestamp": "ISO 8601 datetime"
}
```

### HTTP Status Codes

- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **204 No Content**: Request successful, no content to return
- **400 Bad Request**: Invalid request data
- **401 Unauthorized**: Authentication required or failed
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **409 Conflict**: Resource conflict (e.g., duplicate)
- **500 Internal Server Error**: Server error

---

## Swagger UI

Interactive API documentation is available at:
- **Development**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/v3/api-docs

---

## Rate Limiting

Currently, there are no rate limits implemented. This may change in production.

---

## Pagination

Paginated endpoints use the following query parameters:
- `page`: Page number (0-indexed, default: 0)
- `size`: Items per page (default varies by endpoint)

Paginated responses include:
```json
{
  "content": [],
  "totalElements": "number",
  "totalPages": "number",
  "size": "number",
  "number": "number (current page)"
}
```

---

## Date Formats

- **Dates**: `YYYY-MM-DD` (e.g., "2024-11-04")
- **Timestamps**: ISO 8601 format (e.g., "2024-11-04T17:39:00Z")

---

## Mobile App Integration Tips

1. **Token Storage**: Store JWT tokens securely using platform-specific secure storage (Keychain for iOS, KeyStore for Android)

2. **Token Refresh**: The token expires after 24 hours (86400000 ms). Implement automatic re-authentication when receiving 401 responses.

3. **Offline Support**: Consider caching GET responses for offline access to farm data.

4. **Image Uploads**: Use multipart/form-data for image uploads (max 10MB per file).

5. **Real-time Updates**: WebSocket support is available for real-time messaging and notifications.

6. **Error Handling**: Always check the `success` field in API responses and handle errors gracefully.

7. **User Headers**: Some collaboration endpoints require `X-User-Id` header. Extract this from the JWT token after authentication.

---

## Support

For issues or questions:
- **Email**: felixkipkiruikerich@gmail.com
- **GitHub**: https://github.com/Felix-kerich

---

**Last Updated**: November 4, 2024
