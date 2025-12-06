# Frontend Integration Guide - ShambaBora Marketplace

## Quick Start for Frontend & Mobile Developers

### Base API URL
```
http://localhost:8080/api/marketplace
```

---

## 1. PRODUCT LISTING

### Get Available Products (Browse)
```
GET /products?q=maize&page=0&size=10
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Maize",
        "price": 50.00,
        "unit": "kg",
        "available": true,
        "sellerId": 1,
        "description": "High quality maize"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3
  }
}
```

---

## 2. SELLER DASHBOARD

### View All My Products (Seller)
```
GET /products/seller/{sellerId}?page=0&size=10
```

Shows all products including sold items.

### View Available Products Only (Seller)
```
GET /products/seller/{sellerId}/available?page=0&size=10
```

Shows only products that haven't been sold yet.

---

## 3. ORDERING FLOW

### Step 1: Place Order
```
POST /orders
Content-Type: application/json

{
  "buyerId": 2,
  "productId": 1,
  "quantity": 5
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order placed",
  "data": {
    "id": 1,
    "buyerId": 2,
    "sellerId": 1,
    "productId": 1,
    "quantity": 5,
    "totalPrice": 250.00,
    "status": "PLACED",
    "createdAt": "2024-01-15T12:00:00Z"
  }
}
```

---

## 4. PAYMENT FLOW (M-Pesa)

### Step 1: Initiate Payment
```
POST /payments/initiate
Content-Type: application/json

{
  "orderId": 1,
  "phoneNumber": "254712345678",
  "accountReference": "ORDER-001"
}
```

**Phone Number Formats (all accepted):**
- `254712345678` ✓
- `+254712345678` ✓
- `0712345678` ✓

**Response:**
```json
{
  "success": true,
  "message": "Payment initiated",
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "checkoutRequestId": "ws_CO_DMZ_123456789",
    "responseCode": "0",
    "responseDescription": "Success. Request accepted for processing",
    "customerMessage": "Success. Request accepted for processing"
  }
}
```

### Step 2: Customer Receives STK Prompt
- Customer gets a popup on their phone
- They enter their M-Pesa PIN
- M-Pesa processes the payment

### Step 3: Poll Payment Status
```
GET /payments/{paymentId}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "status": "PAID",
    "amount": 250.00,
    "transactionCode": "NLJ7RT61SV",
    "phoneNumber": "254712345678",
    "createdAt": "2024-01-15T12:00:00Z",
    "paidAt": "2024-01-15T12:05:00Z"
  }
}
```

**Status Values:**
- `PENDING` - Waiting for customer to enter PIN
- `PAID` - Payment successful ✓
- `FAILED` - Payment failed ✗
- `CANCELLED` - Customer cancelled

---

## 5. BUYER DASHBOARD

### View My Orders
```
GET /orders/buyer/{buyerId}?page=0&size=10
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "buyerId": 2,
        "sellerId": 1,
        "productId": 1,
        "quantity": 5,
        "totalPrice": 250.00,
        "status": "PAID",
        "createdAt": "2024-01-15T12:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

## 6. SELLER DASHBOARD

### View My Orders
```
GET /orders/seller/{sellerId}?page=0&size=10
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "buyerId": 2,
        "sellerId": 1,
        "productId": 1,
        "quantity": 5,
        "totalPrice": 250.00,
        "status": "PAID",
        "createdAt": "2024-01-15T12:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

## 7. COMPLETE WORKFLOW EXAMPLE

### React/TypeScript Implementation

```typescript
// 1. Browse Products
const browseProducts = async () => {
  const response = await fetch('/api/marketplace/products?q=maize&page=0&size=10');
  const data = await response.json();
  return data.data.content;
};

// 2. Place Order
const placeOrder = async (buyerId: number, productId: number, quantity: number) => {
  const response = await fetch('/api/marketplace/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ buyerId, productId, quantity })
  });
  const data = await response.json();
  return data.data; // Returns order with id
};

// 3. Initiate Payment
const initiatePayment = async (orderId: number, phoneNumber: string) => {
  const response = await fetch('/api/marketplace/payments/initiate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      orderId,
      phoneNumber,
      accountReference: `ORDER-${orderId}`
    })
  });
  const data = await response.json();
  return data.data; // Returns paymentId
};

// 4. Poll Payment Status
const checkPaymentStatus = async (paymentId: number) => {
  return new Promise((resolve) => {
    const interval = setInterval(async () => {
      const response = await fetch(`/api/marketplace/payments/${paymentId}`);
      const data = await response.json();
      const status = data.data.status;
      
      if (status === 'PAID') {
        clearInterval(interval);
        resolve({ success: true, message: 'Payment successful!' });
      } else if (status === 'FAILED') {
        clearInterval(interval);
        resolve({ success: false, message: 'Payment failed' });
      }
      // Continue polling if PENDING
    }, 3000); // Check every 3 seconds
  });
};

// 5. Complete Flow
const completeCheckout = async (buyerId: number, productId: number, quantity: number, phoneNumber: string) => {
  try {
    // Place order
    const order = await placeOrder(buyerId, productId, quantity);
    console.log('Order placed:', order.id);
    
    // Initiate payment
    const payment = await initiatePayment(order.id, phoneNumber);
    console.log('Payment initiated. Customer should see STK prompt.');
    
    // Poll for payment status
    const result = await checkPaymentStatus(payment.paymentId);
    
    if (result.success) {
      console.log('Order complete! Product marked as sold.');
      return { success: true, orderId: order.id };
    } else {
      console.log('Payment failed. Please try again.');
      return { success: false };
    }
  } catch (error) {
    console.error('Checkout failed:', error);
    return { success: false };
  }
};

// Usage
completeCheckout(2, 1, 5, '254712345678');
```

---

## 8. SELLER FEATURES

### Create Product
```
POST /products
Content-Type: application/json

{
  "name": "Maize",
  "description": "High quality maize",
  "price": 50.00,
  "unit": "kg",
  "sellerId": 1
}
```

### Update Product
```
PUT /products/{id}
Content-Type: application/json

{
  "name": "Premium Maize",
  "price": 60.00,
  "description": "Updated description",
  "unit": "kg",
  "sellerId": 1
}
```

### View All My Products (Including Sold)
```
GET /products/seller/{sellerId}?page=0&size=10
```

### View Only Available Products
```
GET /products/seller/{sellerId}/available?page=0&size=10
```

### View My Orders
```
GET /orders/seller/{sellerId}?page=0&size=10
```

---

## 9. ERROR HANDLING

### Common Errors

**Invalid Phone Number:**
```json
{
  "success": false,
  "message": "Invalid phone number format. Use 254XXXXXXXXX"
}
```

**Order Not Found:**
```json
{
  "success": false,
  "message": "Order not found"
}
```

**Product Not Available:**
```json
{
  "success": false,
  "message": "Product is not available"
}
```

### Error Handling in Frontend
```typescript
const handleApiCall = async (url: string, options?: RequestInit) => {
  try {
    const response = await fetch(url, options);
    const data = await response.json();
    
    if (!data.success) {
      throw new Error(data.message);
    }
    
    return data.data;
  } catch (error) {
    console.error('API Error:', error);
    // Show error to user
    return null;
  }
};
```

---

## 10. TESTING CHECKLIST

- [ ] Browse products
- [ ] Search products by name
- [ ] Place order
- [ ] Initiate payment
- [ ] Verify STK prompt appears on phone
- [ ] Complete payment
- [ ] Verify order status changes to PAID
- [ ] Verify product marked as sold
- [ ] View seller's sold products
- [ ] View seller's available products
- [ ] View buyer's orders
- [ ] View seller's orders

---

## 11. IMPORTANT NOTES

1. **Phone Number Format**: Always normalize to `254XXXXXXXXX` before sending
2. **Polling**: Poll payment status every 3 seconds for up to 2 minutes
3. **Product Availability**: Once payment is successful, product is automatically marked as sold
4. **Seller Dashboard**: Shows both sold and available products
5. **Order Status**: Changes to PAID automatically when payment succeeds
6. **Timestamps**: All timestamps are in UTC (ISO 8601 format)

---

## 12. PRODUCTION CHECKLIST

- [ ] Update M-Pesa credentials in `application.properties`
- [ ] Set callback URL to your production domain
- [ ] Enable HTTPS for all API calls
- [ ] Implement proper error handling and user feedback
- [ ] Add loading states during payment processing
- [ ] Test with real M-Pesa transactions
- [ ] Implement retry logic for failed payments
- [ ] Add analytics/logging for payment events
- [ ] Set up monitoring for payment failures
