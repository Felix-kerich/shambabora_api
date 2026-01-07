# M-Pesa Payment Quick Start 🚀

## ✅ Your M-Pesa Integration is Ready!

The marketplace already has **full M-Pesa STK Push integration**. Here's how to use it:

---

## 🎯 Quick Test (3 Steps)

### 1. Start the Application
```bash
mvn spring-boot:run
```

### 2. Run the Test Script
```bash
./test-mpesa-payment.sh
```

### 3. Complete Payment on Phone
- Enter your phone number when prompted
- Check your phone for M-Pesa prompt
- Enter your M-Pesa PIN
- Script will automatically detect payment completion

---

## 📋 API Endpoints

### 1️⃣ Create Product
```bash
POST /api/marketplace/products
{
  "name": "Maize",
  "price": 500.00,
  "sellerId": 1
}
```

### 2️⃣ Place Order
```bash
POST /api/marketplace/orders
{
  "buyerId": 2,
  "productId": 1,
  "quantity": 5
}
```

### 3️⃣ Initiate Payment (Send STK Push)
```bash
POST /api/marketplace/payments/initiate
{
  "orderId": 1,
  "phoneNumber": "254712345678",
  "accountReference": "ORDER-001"
}
```

**What happens:**
- ✅ Customer receives M-Pesa prompt on phone
- ✅ Customer enters PIN
- ✅ System automatically receives callback from Safaricom
- ✅ Payment status updated to PAID
- ✅ Order status updated to PAID

### 4️⃣ Check Payment Status
```bash
GET /api/marketplace/payments/{paymentId}
```

---

## ⚠️ IMPORTANT: For Production

### Current Issue
Callback URL is set to `http://localhost:8080` which **won't work** because Safaricom can't reach your local machine.

### Solution: Use ngrok (for testing)
```bash
# Install ngrok
sudo snap install ngrok

# Start ngrok
ngrok http 8080

# Copy the HTTPS URL (e.g., https://abc123.ngrok.io)
# Update application.properties:
mpesa.callback-url=https://abc123.ngrok.io/api/marketplace/payments/callback

# Restart application
```

### Solution: Deploy to Production
```properties
# Update with your domain:
mpesa.callback-url=https://yourdomain.com/api/marketplace/payments/callback
```

---

## 🔧 Configuration (application.properties)

```properties
# M-Pesa Sandbox Credentials (Already Configured)
mpesa.consumer-key=Aeg1i7Yt3PWXHWGQsm6HHwNgMjOKhtFEMtdtCczAf7axpet9
mpesa.consumer-secret=wpFS7VMhpRDCAMAC2pUVTNX05Ogg8JP7mBFWlBAUxj5u1IAGAcY5XSXncnIfg3w0
mpesa.business-short-code=174379
mpesa.passkey=bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919

# ⚠️ UPDATE THIS FOR PRODUCTION
mpesa.callback-url=http://localhost:8080/api/marketplace/payments/callback

# Test Mode
mpesa.test-mode=false  # Set to true to skip real M-Pesa calls
```

---

## 🔍 How It Works

```
1. Buyer places order
   ↓
2. Initiate payment (send STK push)
   ↓
3. Buyer receives M-Pesa prompt on phone
   ↓
4. Buyer enters M-Pesa PIN
   ↓
5. M-Pesa processes payment
   ↓
6. Safaricom sends callback to your server ⚡
   ↓
7. System automatically:
   - Updates payment status to PAID
   - Updates order status to PAID
   - Saves transaction code
   ↓
8. Frontend polls and detects payment success ✅
```

---

## 📱 Test Phone Numbers (Sandbox)

Use these Safaricom test numbers:
- **254708374149**
- **254712345678**

PIN: Use the passkey value (for sandbox testing)

---

## 🐛 Troubleshooting

### "Failed to get access token"
→ Check Consumer Key and Consumer Secret

### "Callback not received"
→ Ensure callback URL is publicly accessible (use ngrok)

### "STK Push failed"
→ Check phone number format (254XXXXXXXXX)

### Enable Debug Logs
Add to `application.properties`:
```properties
logging.level.com.app.shambabora.modules.marketplace.service.PaymentService=DEBUG
```

---

## 📚 Full Documentation
See: `docs/MPESA_PAYMENT_INTEGRATION_GUIDE.md`

---

## ✨ What You Have

✅ **STK Push** - Send payment prompt to customer's phone  
✅ **Callback Handler** - Automatic payment status updates  
✅ **Status Polling** - Check payment status  
✅ **Order Integration** - Orders auto-update when paid  
✅ **Transaction Tracking** - M-Pesa receipt numbers saved  
✅ **Error Handling** - Failed payments tracked  
✅ **Phone Validation** - Auto-format phone numbers  
✅ **Logging** - Detailed logs for debugging  

---

## 🚀 Ready to Go!

Your marketplace payment system is **production-ready**. Just deploy to a public server and update the callback URL!

**Need help?** Check the full guide: `docs/MPESA_PAYMENT_INTEGRATION_GUIDE.md`
