# M-Pesa Payment Integration Guide - ShambaBora Marketplace

## Overview
The ShambaBora marketplace has a **fully implemented M-Pesa STK Push payment integration** that allows buyers to pay for products directly through their mobile phones using M-Pesa.

## ✅ What's Already Implemented

### 1. **STK Push Initiation**
- When a buyer places an order, they can initiate payment
- The system sends an STK Push request to the buyer's phone
- The buyer receives a prompt on their phone to enter their M-Pesa PIN

### 2. **Payment Callback Handling**
- Safaricom M-Pesa sends a callback to your server when payment is complete
- The callback is automatically processed
- Payment status is updated (PAID/FAILED)
- Order status is updated to PAID when successful

### 3. **Payment Status Tracking**
- Frontend can poll for payment status
- Track payment throughout the lifecycle (PENDING → PAID/FAILED)

---

## 🔧 Configuration

### Current Configuration (in `application.properties`)

```properties
# M-Pesa Credentials (from Safaricom Developer Portal)
mpesa.consumer-key=Aeg1i7Yt3PWXHWGQsm6HHwNgMjOKhtFEMtdtCczAf7axpet9
mpesa.consumer-secret=wpFS7VMhpRDCAMAC2pUVTNX05Ogg8JP7mBFWlBAUxj5u1IAGAcY5XSXncnIfg3w0
mpesa.business-short-code=174379
mpesa.passkey=bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919

# Callback URL - ⚠️ MUST BE PUBLICLY ACCESSIBLE
mpesa.callback-url=http://localhost:8080/api/marketplace/payments/callback

# API URLs (Sandbox)
mpesa.auth-url=https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials
mpesa.stk-push-url=https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest
mpesa.query-url=https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query

# Test Mode - Set to false for production
mpesa.test-mode=false
```

---

## 🚨 IMPORTANT: Callback URL Configuration

### Problem with Current Setup
The current callback URL is `http://localhost:8080` which will **NOT work in production** because:
- Safaricom servers cannot reach your local machine
- The callback needs a **publicly accessible URL**

### Solutions:

#### Option 1: Use ngrok (For Testing/Development)
```bash
# Install ngrok
sudo snap install ngrok

# Start ngrok tunnel
ngrok http 8080

# You'll get a public URL like: https://abc123.ngrok.io
# Update application.properties:
mpesa.callback-url=https://abc123.ngrok.io/api/marketplace/payments/callback
```

#### Option 2: Deploy to Production Server
```properties
# Update with your production domain:
mpesa.callback-url=https://yourdomain.com/api/marketplace/payments/callback

# Example:
mpesa.callback-url=https://api.shambabora.com/api/marketplace/payments/callback
```

#### Option 3: Use a Cloud Service (Recommended for Production)
Deploy to:
- AWS EC2 + Elastic IP
- DigitalOcean Droplet
- Heroku
- Google Cloud Platform
- Azure

Then update the callback URL with your server's public IP/domain.

---

## 📋 Complete Payment Flow

### Step 1: Create a Product
```bash
POST /api/marketplace/products
Content-Type: application/json

{
  "name": "Maize Seeds",
  "description": "High quality hybrid maize seeds",
  "price": 500.00,
  "unit": "kg",
  "quantity": 100,
  "sellerId": 1,
  "category": "SEEDS",
  "available": true
}
```

### Step 2: Place an Order
```bash
POST /api/marketplace/orders
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
    "totalPrice": 2500.00,
    "status": "PLACED",
    "createdAt": "2026-01-07T18:30:00Z"
  }
}
```

### Step 3: Initiate M-Pesa Payment (STK Push)
```bash
POST /api/marketplace/payments/initiate
Content-Type: application/json

{
  "orderId": 1,
  "phoneNumber": "254712345678",
  "accountReference": "ORDER-001"
}
```

**What Happens:**
1. System validates the order exists
2. System gets M-Pesa access token
3. System sends STK Push to buyer's phone
4. Buyer receives M-Pesa prompt on their phone
5. System saves payment record with status "PENDING"

**Response:**
```json
{
  "success": true,
  "message": "Payment initiated",
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "checkoutRequestId": "ws_CO_DMZ_123456789_07012026183045",
    "responseCode": "0",
    "responseDescription": "Success. Request accepted for processing",
    "customerMessage": "Success. Request accepted for processing"
  }
}
```

### Step 4: Customer Pays on Their Phone
- Customer receives STK push prompt on their M-Pesa app
- Customer enters M-Pesa PIN
- M-Pesa processes the payment

### Step 5: Safaricom Sends Callback (Automatic)
When payment is complete, Safaricom sends a callback to:
```
POST https://yourdomain.com/api/marketplace/payments/callback
```

**Callback Payload (from Safaricom):**
```json
{
  "Body": {
    "stkCallback": {
      "MerchantRequestID": "16813-1590513-1",
      "CheckoutRequestID": "ws_CO_DMZ_123456789_07012026183045",
      "ResultCode": 0,
      "ResultDesc": "The service request has been processed successfully.",
      "CallbackMetadata": {
        "Item": [
          {"Name": "Amount", "Value": 2500},
          {"Name": "MpesaReceiptNumber", "Value": "NLJ7RT61SV"},
          {"Name": "TransactionDate", "Value": 20260107183545},
          {"Name": "PhoneNumber", "Value": 254712345678}
        ]
      }
    }
  }
}
```

**What the System Does Automatically:**
- ✅ Finds the payment record by `CheckoutRequestID`
- ✅ Updates payment status to "PAID" (if ResultCode = 0)
- ✅ Saves M-Pesa receipt number (transaction code)
- ✅ Updates order status to "PAID"
- ✅ Records payment timestamp

### Step 6: Check Payment Status (Polling)
```bash
GET /api/marketplace/payments/{paymentId}
```

**Response (Successful Payment):**
```json
{
  "success": true,
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "status": "PAID",
    "amount": 2500.00,
    "transactionCode": "NLJ7RT61SV",
    "phoneNumber": "254712345678",
    "createdAt": "2026-01-07T18:30:45Z",
    "paidAt": "2026-01-07T18:31:15Z"
  }
}
```

---

## 📱 Frontend Integration Example

### React/TypeScript Example
```typescript
const completeCheckout = async (
  buyerId: number,
  productId: number,
  quantity: number,
  phoneNumber: string
) => {
  try {
    // 1. Place order
    const orderResponse = await fetch('/api/marketplace/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ buyerId, productId, quantity })
    });
    const orderData = await orderResponse.json();
    const order = orderData.data;
    
    console.log('✅ Order placed:', order.id);
    
    // 2. Initiate payment
    const paymentResponse = await fetch('/api/marketplace/payments/initiate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: order.id,
        phoneNumber: phoneNumber,
        accountReference: `ORDER-${order.id}`
      })
    });
    const paymentData = await paymentResponse.json();
    const payment = paymentData.data;
    
    console.log('📱 STK push sent. Customer should see prompt on phone.');
    console.log('Payment ID:', payment.paymentId);
    
    // 3. Poll for payment status (check every 3 seconds)
    let attempts = 0;
    const maxAttempts = 40; // 2 minutes max wait time
    
    const checkStatus = setInterval(async () => {
      attempts++;
      
      const statusResponse = await fetch(
        `/api/marketplace/payments/${payment.paymentId}`
      );
      const statusData = await statusResponse.json();
      const status = statusData.data.status;
      
      console.log(`Checking payment status (${attempts}/${maxAttempts}):`, status);
      
      if (status === 'PAID') {
        clearInterval(checkStatus);
        console.log('✅ Payment successful!');
        console.log('Transaction Code:', statusData.data.transactionCode);
        alert('Payment successful! Order confirmed.');
        // Redirect to order confirmation page
        window.location.href = `/orders/${order.id}`;
      } else if (status === 'FAILED') {
        clearInterval(checkStatus);
        console.log('❌ Payment failed');
        alert('Payment failed. Please try again.');
      } else if (attempts >= maxAttempts) {
        clearInterval(checkStatus);
        console.log('⏱️ Payment timeout');
        alert('Payment status check timed out. Please contact support.');
      }
    }, 3000); // Check every 3 seconds
    
  } catch (error) {
    console.error('Error:', error);
    alert('An error occurred. Please try again.');
  }
};

// Usage
completeCheckout(2, 1, 5, '254712345678');
```

---

## 🧪 Testing

### Test Mode
Set `mpesa.test-mode=true` in `application.properties` to:
- Skip real M-Pesa API calls
- Use mock responses
- Test the flow without M-Pesa credentials

### Testing with Real M-Pesa (Sandbox)
1. **Get Credentials:**
   - Go to https://developer.safaricom.co.ke/
   - Create an app
   - Get Consumer Key and Consumer Secret
   - Get Passkey from your app dashboard

2. **Setup Ngrok:**
   ```bash
   ngrok http 8080
   # Copy the HTTPS URL (e.g., https://abc123.ngrok.io)
   ```

3. **Update Configuration:**
   ```properties
   mpesa.callback-url=https://abc123.ngrok.io/api/marketplace/payments/callback
   mpesa.test-mode=false
   ```

4. **Test Payment:**
   - Use Safaricom test phone numbers (provided in their documentation)
   - Test PIN: `mpesa.passkey` value

---

## 🔍 Monitoring & Debugging

### Enable Debug Logging
Add to `application.properties`:
```properties
logging.level.com.app.shambabora.modules.marketplace.service.PaymentService=DEBUG
```

### Check Logs
Look for these log messages:
- `✓ Access token obtained successfully` - Auth working
- `✓ STK Push successful!` - STK Push sent
- `Payment successful: paymentId=...` - Callback received and processed

### Common Issues

#### 1. "Failed to get access token"
- **Cause:** Invalid Consumer Key or Consumer Secret
- **Fix:** Verify credentials from Safaricom Developer Portal

#### 2. "STK Push failed (client error): 401"
- **Cause:** Invalid access token
- **Fix:** Check Consumer Key/Secret, ensure they're current

#### 3. "Payment not found for checkout"
- **Cause:** Callback received but payment record not found
- **Fix:** Check if payment was created during initiation

#### 4. Callback not received
- **Cause:** Callback URL not publicly accessible
- **Fix:** Use ngrok or deploy to public server

---

## 🚀 Production Deployment Checklist

- [ ] Deploy application to public server
- [ ] Update `mpesa.callback-url` with public domain
- [ ] Switch to production M-Pesa credentials (not sandbox)
- [ ] Update M-Pesa API URLs to production:
  ```properties
  mpesa.auth-url=https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials
  mpesa.stk-push-url=https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest
  ```
- [ ] Set `mpesa.test-mode=false`
- [ ] Whitelist your server IP on Safaricom Developer Portal
- [ ] Test with real phone numbers and small amounts
- [ ] Setup monitoring and alerting
- [ ] Configure error handling and retry logic

---

## 📞 Support

For M-Pesa integration issues:
- **Safaricom Developer Support:** https://developer.safaricom.co.ke/support
- **Documentation:** https://developer.safaricom.co.ke/docs

For ShambaBora issues:
- Check logs in `/var/log/shambabora/`
- Review `PaymentService.java` for implementation details

---

## 🎉 Summary

Your marketplace M-Pesa integration is **fully functional**. The only thing you need to do for production is:

1. ✅ **Deploy to a public server** (or use ngrok for testing)
2. ✅ **Update the callback URL** to your public domain
3. ✅ **Test with real transactions**

The system will automatically:
- Send STK push to buyers
- Process callbacks from Safaricom
- Update payment and order statuses
- Track transaction codes

Happy selling! 🌽💰📱
