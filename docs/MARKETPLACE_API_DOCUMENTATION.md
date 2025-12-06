# ShambaBora Marketplace API Documentation

## Overview
Complete API documentation for the ShambaBora marketplace with M-Pesa payment integration.

---

## Base URL
```
http://localhost:8080/api/marketplace
```

---

## 1. PRODUCT ENDPOINTS

### 1.1 Create Product
**Endpoint:** `POST /products`

**Request Body:**
```json
{
  "name": "Maize",
  "description": "High quality maize",
  "price": 50.00,
  "unit": "kg",
  "sellerId": 1
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Product created",
  "data": {
    "id": 1,
    "name": "Maize",
    "description": "High quality maize",
    "price": 50.00,
    "unit": "kg",
    "available": true,
    "sellerId": 1,
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

---

### 1.2 Update Product
**Endpoint:** `PUT /products/{id}`

**Request Body:**
```json
{
  "name": "Premium Maize",
  "description": "High quality premium maize",
  "price": 60.00,
  "unit": "kg",
  "sellerId": 1
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product updated",
  "data": {
    "id": 1,
    "name": "Premium Maize",
    "description": "High quality premium maize",
    "price": 60.00,
    "unit": "kg",
    "available": true,
    "sellerId": 1,
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:35:00Z"
  }
}
```

---

### 1.3 Get Product by ID
**Endpoint:** `GET /products/{id}`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Maize",
    "description": "High quality maize",
    "price": 50.00,
    "unit": "kg",
    "available": true,
    "sellerId": 1,
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

---

### 1.4 Search Products
**Endpoint:** `GET /products?q=maize&page=0&size=10`

**Query Parameters:**
- `q` (optional): Search query
- `page` (optional, default: 0): Page number
- `size` (optional, default: 10): Page size

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Maize",
        "description": "High quality maize",
        "price": 50.00,
        "unit": "kg",
        "available": true,
        "sellerId": 1,
        "createdAt": "2024-01-15T10:30:00Z",
        "updatedAt": "2024-01-15T10:30:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 1.5 Get All Products by Seller (Including Sold)
**Endpoint:** `GET /products/seller/{sellerId}?page=0&size=10`

**Description:** Returns all products created by a seller, including sold items.

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Maize",
        "description": "High quality maize",
        "price": 50.00,
        "unit": "kg",
        "available": false,
        "sellerId": 1,
        "createdAt": "2024-01-15T10:30:00Z",
        "updatedAt": "2024-01-15T10:35:00Z"
      },
      {
        "id": 2,
        "name": "Beans",
        "description": "Quality beans",
        "price": 80.00,
        "unit": "kg",
        "available": true,
        "sellerId": 1,
        "createdAt": "2024-01-15T11:00:00Z",
        "updatedAt": "2024-01-15T11:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

---

### 1.6 Get Available Products by Seller
**Endpoint:** `GET /products/seller/{sellerId}/available?page=0&size=10`

**Description:** Returns only available (not sold) products created by a seller.

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 2,
        "name": "Beans",
        "description": "Quality beans",
        "price": 80.00,
        "unit": "kg",
        "available": true,
        "sellerId": 1,
        "createdAt": "2024-01-15T11:00:00Z",
        "updatedAt": "2024-01-15T11:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 1.7 Set Product Availability
**Endpoint:** `PATCH /products/{id}/availability?available=false`

**Query Parameters:**
- `available` (required): true or false

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Availability updated"
}
```

---

## 2. ORDER ENDPOINTS

### 2.1 Place Order
**Endpoint:** `POST /orders`

**Request Body:**
```json
{
  "buyerId": 2,
  "productId": 1,
  "quantity": 5
}
```

**Response (201 Created):**
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

### 2.2 Update Order Status
**Endpoint:** `PATCH /orders/{id}/status?status=PAID`

**Query Parameters:**
- `status` (required): PLACED, PAID, SHIPPED, COMPLETED, CANCELLED

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Status updated",
  "data": {
    "id": 1,
    "buyerId": 2,
    "sellerId": 1,
    "productId": 1,
    "quantity": 5,
    "totalPrice": 250.00,
    "status": "PAID",
    "createdAt": "2024-01-15T12:00:00Z"
  }
}
```

---

### 2.3 Get Orders by Buyer
**Endpoint:** `GET /orders/buyer/{buyerId}?page=0&size=10`

**Response (200 OK):**
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
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 2.4 Get Orders by Seller
**Endpoint:** `GET /orders/seller/{sellerId}?page=0&size=10`

**Response (200 OK):**
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
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 3. PAYMENT ENDPOINTS (M-Pesa Integration)

### 3.1 Initiate Payment
**Endpoint:** `POST /payments/initiate`

**Description:** Initiates an M-Pesa STK Push payment for an order. The customer will receive a prompt on their phone to enter their M-Pesa PIN.

**Request Body:**
```json
{
  "orderId": 1,
  "phoneNumber": "254712345678",
  "accountReference": "ORDER-001"
}
```

**Phone Number Format:**
- Accepted formats: `254712345678`, `+254712345678`, `0712345678`
- All formats are normalized to `254XXXXXXXXX`

**Response (200 OK):**
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

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Invalid phone number format. Use 254XXXXXXXXX"
}
```

---

### 3.2 Payment Callback (M-Pesa Server → Your Server)
**Endpoint:** `POST /payments/callback`

**Description:** M-Pesa servers call this endpoint to notify about payment status. This is automatically called by M-Pesa.

**Request Body (from M-Pesa):**
```json
{
  "Body": {
    "stkCallback": {
      "MerchantRequestID": "16813-1590513-1",
      "CheckoutRequestID": "ws_CO_DMZ_123456789",
      "ResultCode": 0,
      "ResultDesc": "The service request has been processed successfully.",
      "CallbackMetadata": {
        "Item": [
          {
            "Name": "Amount",
            "Value": 250.00
          },
          {
            "Name": "MpesaReceiptNumber",
            "Value": "NLJ7RT61SV"
          },
          {
            "Name": "TransactionDate",
            "Value": 20240115120530
          },
          {
            "Name": "PhoneNumber",
            "Value": 254712345678
          }
        ]
      }
    }
  }
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Callback processed"
}
```

**Result Codes:**
- `0`: Success - Payment completed
- `1`: Cancelled by user
- `2`: Request timeout

---

### 3.3 Get Payment Status
**Endpoint:** `GET /payments/{paymentId}`

**Response (200 OK):**
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

**Payment Status Values:**
- `PENDING`: Payment initiated, waiting for customer to enter PIN
- `PAID`: Payment successful
- `FAILED`: Payment failed
- `CANCELLED`: Payment cancelled by customer

---

## 4. PAYMENT FLOW DIAGRAM

```
1. Frontend/Mobile App
   ↓
2. POST /payments/initiate
   ├─ Validate order exists
   ├─ Get M-Pesa access token
   ├─ Send STK Push request
   └─ Save payment record (status: PENDING)
   ↓
3. Customer receives STK prompt on phone
   ├─ Enters M-Pesa PIN
   └─ M-Pesa processes payment
   ↓
4. M-Pesa calls POST /payments/callback
   ├─ If ResultCode == 0 (Success):
   │  ├─ Update payment status to PAID
   │  ├─ Update order status to PAID
   │  └─ Mark product as sold (available = false)
   └─ If ResultCode != 0 (Failed):
      └─ Update payment status to FAILED
   ↓
5. Frontend polls GET /payments/{paymentId}
   └─ Displays payment status to user
```

---

## 5. CONFIGURATION

### M-Pesa Credentials Setup

Add the following to `application.properties`:

```properties
# M-Pesa Configuration
mpesa.consumer-key=YOUR_CONSUMER_KEY
mpesa.consumer-secret=YOUR_CONSUMER_SECRET
mpesa.business-short-code=174379
mpesa.passkey=bfb279f9ba9b9d4abe336c6882c0d23c
mpesa.callback-url=https://your-domain.com/api/marketplace/payments/callback

# For sandbox/testing:
# mpesa.auth-url=https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials
# mpesa.stk-push-url=https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest
# mpesa.query-url=https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query
```

### Getting M-Pesa Credentials

1. Go to [Safaricom Developer Portal](https://developer.safaricom.co.ke/)
2. Create an account and log in
3. Create a new app
4. Get your `Consumer Key` and `Consumer Secret`
5. Use the provided `Business Short Code` and `Passkey`
6. Set your callback URL in the app settings

---

## 6. DATABASE SCHEMA

### Products Table
```sql
CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  description VARCHAR(1000),
  price DECIMAL(12,2) NOT NULL,
  unit VARCHAR(40) NOT NULL,
  available BOOLEAN NOT NULL DEFAULT true,
  seller_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Orders Table
```sql
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  total_price DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  payment_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Payments Table
```sql
CREATE TABLE payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  checkout_request_id VARCHAR(50),
  merchant_request_id VARCHAR(50),
  transaction_code VARCHAR(20),
  phone_number VARCHAR(20),
  response_description VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  paid_at TIMESTAMP
);
```

---

## 7. ERROR HANDLING

### Common Error Responses

**400 Bad Request:**
```json
{
  "success": false,
  "message": "Invalid phone number format. Use 254XXXXXXXXX"
}
```

**404 Not Found:**
```json
{
  "success": false,
  "message": "Order not found"
}
```

**500 Internal Server Error:**
```json
{
  "success": false,
  "message": "Failed to initiate payment: Connection timeout"
}
```

---

## 8. FRONTEND INTEGRATION EXAMPLE

### React/TypeScript Example

```typescript
// Initiate Payment
const initiatePayment = async (orderId: number, phoneNumber: string) => {
  try {
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
    if (data.success) {
      const paymentId = data.data.paymentId;
      // Poll for payment status
      pollPaymentStatus(paymentId);
    }
  } catch (error) {
    console.error('Payment initiation failed:', error);
  }
};

// Poll Payment Status
const pollPaymentStatus = async (paymentId: number) => {
  const interval = setInterval(async () => {
    try {
      const response = await fetch(`/api/marketplace/payments/${paymentId}`);
      const data = await response.json();
      
      if (data.data.status === 'PAID') {
        clearInterval(interval);
        console.log('Payment successful!');
        // Update UI, redirect, etc.
      } else if (data.data.status === 'FAILED') {
        clearInterval(interval);
        console.log('Payment failed');
      }
    } catch (error) {
      console.error('Status check failed:', error);
    }
  }, 3000); // Poll every 3 seconds
};
```

---

## 9. TESTING

### Test Credentials (Sandbox)
- Phone: `254708374149`
- PIN: `123456`
- Amount: Any amount

### cURL Examples

**Initiate Payment:**
```bash
curl -X POST http://localhost:8080/api/marketplace/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "phoneNumber": "254712345678",
    "accountReference": "ORDER-001"
  }'
```

**Get Payment Status:**
```bash
curl -X GET http://localhost:8080/api/marketplace/payments/1
```

**Get Seller Products:**
```bash
curl -X GET "http://localhost:8080/api/marketplace/products/seller/1?page=0&size=10"
```

---

## 10. NOTES

- All timestamps are in UTC (ISO 8601 format)
- Phone numbers are normalized to `254XXXXXXXXX` format
- Products are marked as sold when order status changes to `PAID`
- Sellers can view all their products (sold and available) via `/products/seller/{sellerId}`
- Sellers can view only available products via `/products/seller/{sellerId}/available`
- Payment callback URL must be publicly accessible for M-Pesa to reach it
- Implement HTTPS for production environments
