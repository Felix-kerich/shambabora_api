# ShambaBora Mobile App - Quick Start Guide

## Getting Started in 5 Minutes

### 1. Authentication Flow

```javascript
// Register a new user
POST /api/auth/register
{
  "username": "farmer123",
  "email": "farmer@example.com",
  "password": "securepass123",
  "fullName": "John Farmer",
  "phoneNumber": "+254712345678"
}

// Response includes JWT token
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "farmer123",
  "email": "farmer@example.com",
  "role": "FARMER"
}

// Login existing user
POST /api/auth/login
{
  "usernameOrEmail": "farmer123",
  "password": "securepass123"
}
```

### 2. Using the JWT Token

Include the token in all authenticated requests:

```javascript
headers: {
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "Content-Type": "application/json"
}
```

### 3. Core Features Implementation

#### A. Farm Dashboard (Home Screen)

```javascript
GET /api/farm-dashboard
Authorization: Bearer <token>

// Response contains everything for home screen
{
  "totalActivities": 45,
  "totalExpenses": 15000.00,
  "totalRevenue": 25000.00,
  "totalYield": 5000.00,
  "recentActivities": [...],
  "expensesByCategory": {...},
  "yieldByCrop": {...},
  "upcomingReminders": [...]
}
```

#### B. Record Farm Activity

```javascript
POST /api/farm-activities
Authorization: Bearer <token>

{
  "activityType": "Planting",
  "cropType": "Maize",
  "activityDate": "2024-11-04",
  "description": "Planted hybrid maize seeds",
  "areaSize": 2.5,
  "units": "acres",
  "cost": 5000.00,
  "weatherConditions": "Sunny",
  "notes": "Used improved seeds"
}
```

#### C. Track Expenses

```javascript
POST /api/farm-expenses
Authorization: Bearer <token>

{
  "cropType": "Maize",
  "category": "Seeds",
  "description": "Hybrid maize seeds",
  "amount": 5000.00,
  "expenseDate": "2024-11-04",
  "supplier": "Seed Co.",
  "paymentMethod": "M-Pesa"
}
```

#### D. Record Harvest

```javascript
POST /api/yield-records
Authorization: Bearer <token>

{
  "cropType": "Maize",
  "harvestDate": "2024-11-04",
  "yieldAmount": 1500,
  "unit": "kg",
  "areaHarvested": 2.5,
  "marketPrice": 50.00,
  "qualityGrade": "Grade A",
  "buyer": "Local Cooperative"
}
```

#### E. Weather Information

```javascript
GET /api/weather/current?location=Nairobi
// No authentication required

GET /api/weather/forecast/daily?location=Nairobi&days=7
```

#### F. Marketplace - List Product

```javascript
POST /api/marketplace/products
Authorization: Bearer <token>

{
  "name": "Fresh Maize",
  "description": "Organic maize from my farm",
  "category": "Grains",
  "price": 50.00,
  "quantity": 1000,
  "unit": "kg",
  "location": "Nairobi"
}
```

#### G. Community Posts

```javascript
POST /api/collaboration/posts
Authorization: Bearer <token>
X-User-Id: 123

{
  "content": "Just harvested my maize crop! Great yield this season.",
  "imageUrl": "https://...",
  "tags": ["maize", "harvest"]
}

// Get feed
GET /api/collaboration/posts/feed?page=0&size=20
X-User-Id: 123
```

#### H. Direct Messaging

```javascript
POST /api/collaboration/direct-messages
Authorization: Bearer <token>
X-User-Id: 123

{
  "recipientId": 456,
  "content": "Hi, interested in your maize product"
}

// Get conversation
GET /api/collaboration/direct-messages/conversation/456?page=0&size=50
X-User-Id: 123
```

---

## Common Workflows

### Workflow 1: New Farmer Onboarding

1. Register user → `POST /api/auth/register`
2. Create farmer profile → `POST /api/farmer-profile`
3. Get dashboard → `GET /api/farm-dashboard`

### Workflow 2: Daily Farm Operations

1. Check weather → `GET /api/weather/current`
2. Record activity → `POST /api/farm-activities`
3. Add expense → `POST /api/farm-expenses`
4. Check reminders → `GET /api/farm-activities/reminders/upcoming`

### Workflow 3: Harvest & Sell

1. Record yield → `POST /api/yield-records`
2. List product → `POST /api/marketplace/products`
3. Manage orders → `GET /api/marketplace/orders/seller/{sellerId}`

### Workflow 4: Community Engagement

1. Browse groups → `GET /api/collaboration/groups/browse`
2. Join group → `POST /api/collaboration/groups/{groupId}/join`
3. Create post → `POST /api/collaboration/posts`
4. View feed → `GET /api/collaboration/posts/feed`

---

## Data Models Reference

### Farm Activity Types
- Planting
- Irrigation
- Fertilization
- Pest Control
- Weeding
- Harvesting
- Soil Preparation

### Expense Categories
- Seeds
- Fertilizer
- Pesticides
- Labor
- Equipment
- Irrigation
- Transport
- Other

### Crop Types (Examples)
- Maize
- Wheat
- Rice
- Beans
- Tomatoes
- Potatoes
- Coffee
- Tea

### Order Status
- PENDING
- CONFIRMED
- SHIPPED
- DELIVERED
- CANCELLED

### Group Roles
- OWNER
- ADMIN
- MODERATOR
- MEMBER

---

## Mobile App Screens Mapping

### 1. Authentication Screens
- **Login Screen** → `POST /api/auth/login`
- **Register Screen** → `POST /api/auth/register`

### 2. Main Screens
- **Dashboard** → `GET /api/farm-dashboard`
- **Profile** → `GET /api/users/profile`, `GET /api/farmer-profile/me`
- **Weather** → `GET /api/weather/current`, `GET /api/weather/forecast/daily`

### 3. Farm Management
- **Activities List** → `GET /api/farm-activities`
- **Add Activity** → `POST /api/farm-activities`
- **Expenses List** → `GET /api/farm-expenses`
- **Add Expense** → `POST /api/farm-expenses`
- **Yield Records** → `GET /api/yield-records`
- **Add Yield** → `POST /api/yield-records`
- **Reminders** → `GET /api/farm-activities/reminders/upcoming`

### 4. Analytics
- **Expense Analytics** → `GET /api/farm-expenses/breakdown/category`
- **Yield Trends** → `GET /api/yield-records/trends`
- **Revenue** → `GET /api/yield-records/revenue`

### 5. Marketplace
- **Products List** → `GET /api/marketplace/products`
- **My Products** → Filter by seller
- **Product Details** → `GET /api/marketplace/products/{id}`
- **Add Product** → `POST /api/marketplace/products`
- **My Orders (Buyer)** → `GET /api/marketplace/orders/buyer/{buyerId}`
- **My Orders (Seller)** → `GET /api/marketplace/orders/seller/{sellerId}`
- **Place Order** → `POST /api/marketplace/orders`

### 6. Community
- **Feed** → `GET /api/collaboration/posts/feed`
- **Create Post** → `POST /api/collaboration/posts`
- **Groups** → `GET /api/collaboration/groups/my-groups`
- **Browse Groups** → `GET /api/collaboration/groups/browse`
- **Group Details** → `GET /api/collaboration/groups/{groupId}`
- **Group Posts** → `GET /api/collaboration/posts/group/{groupId}`

### 7. Messaging
- **Conversations** → `GET /api/collaboration/direct-messages/conversations`
- **Chat** → `GET /api/collaboration/direct-messages/conversation/{otherUserId}`
- **Send Message** → `POST /api/collaboration/direct-messages`

---

## Error Handling Examples

```javascript
// Example error handling in React Native
async function fetchDashboard() {
  try {
    const response = await fetch('http://localhost:8080/api/farm-dashboard', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      if (response.status === 401) {
        // Token expired - redirect to login
        navigateToLogin();
      } else if (response.status === 404) {
        // Resource not found
        showError('Dashboard data not found');
      } else {
        // Other errors
        const error = await response.json();
        showError(error.message);
      }
      return;
    }
    
    const data = await response.json();
    setDashboardData(data);
  } catch (error) {
    // Network error
    showError('Network error. Please check your connection.');
  }
}
```

---

## Testing Endpoints

### Using cURL

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"test123","fullName":"Test User"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"test","password":"test123"}'

# Get Dashboard (replace TOKEN)
curl -X GET http://localhost:8080/api/farm-dashboard \
  -H "Authorization: Bearer TOKEN"
```

### Using Postman

1. Import the OpenAPI spec from: `http://localhost:8080/v3/api-docs`
2. Set up environment variables:
   - `baseUrl`: http://localhost:8080
   - `token`: (set after login)
3. Use `{{baseUrl}}` and `{{token}}` in requests

---

## Performance Tips

1. **Pagination**: Always use pagination for lists to reduce data transfer
2. **Caching**: Cache dashboard and static data locally
3. **Lazy Loading**: Load images and heavy content on demand
4. **Debouncing**: Debounce search queries to reduce API calls
5. **Offline Mode**: Queue POST requests when offline and sync when online

---

## Security Best Practices

1. **Never** store JWT token in plain text
2. Use secure storage (Keychain/KeyStore)
3. Implement token refresh mechanism
4. Clear token on logout
5. Validate all user inputs before sending to API
6. Use HTTPS in production
7. Implement certificate pinning for production

---

## Sample React Native Code

```javascript
// API Service
import AsyncStorage from '@react-native-async-storage/async-storage';

const API_BASE_URL = 'http://localhost:8080';

class ApiService {
  async getToken() {
    return await AsyncStorage.getItem('jwt_token');
  }

  async setToken(token) {
    await AsyncStorage.setItem('jwt_token', token);
  }

  async request(endpoint, options = {}) {
    const token = await this.getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    return await response.json();
  }

  // Auth
  async login(usernameOrEmail, password) {
    const data = await this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ usernameOrEmail, password }),
    });
    await this.setToken(data.token);
    return data;
  }

  // Dashboard
  async getDashboard() {
    return await this.request('/api/farm-dashboard');
  }

  // Farm Activities
  async createActivity(activityData) {
    return await this.request('/api/farm-activities', {
      method: 'POST',
      body: JSON.stringify(activityData),
    });
  }

  async getActivities(page = 0, size = 10) {
    return await this.request(`/api/farm-activities?page=${page}&size=${size}`);
  }
}

export default new ApiService();
```

---

## Next Steps

1. Review full API documentation in `API_DOCUMENTATION.md`
2. Test endpoints using Swagger UI: http://localhost:8080/swagger-ui.html
3. Set up your development environment
4. Implement authentication flow
5. Build core screens (Dashboard, Activities, Expenses)
6. Add marketplace and community features
7. Implement offline support
8. Test thoroughly

---

## Support

- **Email**: felixkipkiruikerich@gmail.com
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Full Documentation**: See `API_DOCUMENTATION.md`

Happy coding! 🚀🌾
