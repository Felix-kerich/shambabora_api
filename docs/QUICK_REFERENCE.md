# ShambaBora Marketplace - Quick Reference Card

## Base URL
```
http://localhost:8080/api/marketplace
```

---

## PRODUCTS

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/products` | Create product |
| PUT | `/products/{id}` | Update product |
| GET | `/products/{id}` | Get product details |
| GET | `/products?q=search` | Search products |
| PATCH | `/products/{id}/availability?available=true` | Set availability |
| GET | `/products/seller/{sellerId}` | Get all seller products |
| GET | `/products/seller/{sellerId}/available` | Get available seller products |

---

## ORDERS

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/orders` | Place order |
| PATCH | `/orders/{id}/status?status=PAID` | Update order status |
| GET | `/orders/buyer/{buyerId}` | Get buyer orders |
| GET | `/orders/seller/{sellerId}` | Get seller orders |

---

## PAYMENTS

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/payments/initiate` | Initiate M-Pesa payment |
| POST | `/payments/callback` | M-Pesa callback (automatic) |
| GET | `/payments/{paymentId}` | Get payment status |

---

## QUICK EXAMPLES

### 1. Create Product
```bash
curl -X POST http://localhost:8080/api/marketplace/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maize",
    "price": 50.00,
    "unit": "kg",
    "sellerId": 1
  }'
```

### 2. Place Order
```bash
curl -X POST http://localhost:8080/api/marketplace/orders \
  -H "Content-Type: application/json" \
  -d '{
    "buyerId": 2,
    "productId": 1,
    "quantity": 5
  }'
```

### 3. Initiate Payment
```bash
curl -X POST http://localhost:8080/api/marketplace/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "phoneNumber": "254712345678",
    "accountReference": "ORDER-001"
  }'
```

### 4. Check Payment Status
```bash
curl http://localhost:8080/api/marketplace/payments/1
```

### 5. Get Seller Products
```bash
curl "http://localhost:8080/api/marketplace/products/seller/1?page=0&size=10"
```

### 6. Get Seller Orders
```bash
curl "http://localhost:8080/api/marketplace/orders/seller/1?page=0&size=10"
```

---

## PAYMENT STATUS VALUES

| Status | Meaning |
|--------|---------|
| PENDING | Waiting for customer to enter PIN |
| PAID | Payment successful ✓ |
| FAILED | Payment failed ✗ |
| CANCELLED | Customer cancelled |

---

## ORDER STATUS VALUES

| Status | Meaning |
|--------|---------|
| PLACED | Order created, awaiting payment |
| PAID | Payment received |
| SHIPPED | Order shipped |
| COMPLETED | Order delivered |
| CANCELLED | Order cancelled |

---

## PHONE NUMBER FORMATS (All Accepted)

- `254712345678` ✓
- `+254712345678` ✓
- `0712345678` ✓

All are normalized to: `254XXXXXXXXX`

---

## CONFIGURATION

Add to `application.properties`:

```properties
mpesa.consumer-key=YOUR_KEY
mpesa.consumer-secret=YOUR_SECRET
mpesa.business-short-code=174379
mpesa.passkey=bfb279f9ba9b9d4abe336c6882c0d23c
mpesa.callback-url=https://your-domain.com/api/marketplace/payments/callback
```

---

## RESPONSE FORMAT

### Success
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### Error
```json
{
  "success": false,
  "message": "Error description"
}
```

---

## PAGINATION

All list endpoints support:
- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page

Example:
```
GET /products?page=0&size=10
```

Response includes:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

---

## KEY FEATURES

✅ M-Pesa STK Push payments  
✅ Automatic product availability management  
✅ Seller dashboard with product history  
✅ Order tracking for buyers and sellers  
✅ Payment status tracking  
✅ Transaction code storage  
✅ Phone number normalization  

---

## WORKFLOW

```
1. Seller creates product
2. Buyer searches and finds product
3. Buyer places order
4. Buyer initiates M-Pesa payment
5. Buyer enters M-Pesa PIN
6. M-Pesa processes payment
7. System updates order to PAID
8. Product marked as sold
9. Seller sees order in dashboard
```

---

## SELLER DASHBOARD

### View All Products (Sold + Available)
```
GET /products/seller/{sellerId}
```

### View Only Available Products
```
GET /products/seller/{sellerId}/available
```

### View Orders Received
```
GET /orders/seller/{sellerId}
```

---

## BUYER DASHBOARD

### View My Orders
```
GET /orders/buyer/{buyerId}
```

### Check Payment Status
```
GET /payments/{paymentId}
```

---

## COMMON ERRORS

| Error | Solution |
|-------|----------|
| Invalid phone number | Use format: 254XXXXXXXXX |
| Order not found | Verify orderId exists |
| Product not available | Product already sold |
| Payment already pending | Wait for previous payment to complete |
| M-Pesa connection error | Check internet, verify credentials |

---

## TESTING

### Test Phone (Sandbox)
- Number: `254708374149`
- PIN: `123456`
- Amount: Any amount

### Test Endpoints
```bash
# Create test product
curl -X POST http://localhost:8080/api/marketplace/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":100,"unit":"kg","sellerId":1}'

# Place test order
curl -X POST http://localhost:8080/api/marketplace/orders \
  -H "Content-Type: application/json" \
  -d '{"buyerId":2,"productId":1,"quantity":1}'

# Initiate test payment
curl -X POST http://localhost:8080/api/marketplace/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{"orderId":1,"phoneNumber":"254708374149","accountReference":"TEST"}'
```

---

## DOCUMENTATION

- **Full API Docs**: `MARKETPLACE_API_DOCUMENTATION.md`
- **Frontend Guide**: `FRONTEND_INTEGRATION_GUIDE.md`
- **Implementation Details**: `IMPLEMENTATION_SUMMARY.md`

---

## SUPPORT

1. Check documentation files
2. Verify M-Pesa credentials
3. Check callback URL is accessible
4. Review application logs
5. Test with sandbox credentials first
