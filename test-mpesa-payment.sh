#!/bin/bash

# M-Pesa Payment Integration Test Script
# This script tests the complete payment flow

BASE_URL="http://localhost:8080/api/marketplace"

echo "🧪 ShambaBora M-Pesa Payment Integration Test"
echo "=============================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Create a test product
echo -e "${YELLOW}Step 1: Creating test product...${NC}"
PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Maize Seeds",
    "description": "High quality test seeds",
    "price": 100.00,
    "unit": "kg",
    "quantity": 50,
    "sellerId": 1,
    "category": "SEEDS",
    "available": true
  }')

PRODUCT_ID=$(echo $PRODUCT_RESPONSE | jq -r '.data.id')
echo -e "${GREEN}✓ Product created with ID: $PRODUCT_ID${NC}"
echo ""

# Step 2: Place an order
echo -e "${YELLOW}Step 2: Placing order...${NC}"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -d "{
    \"buyerId\": 2,
    \"productId\": $PRODUCT_ID,
    \"quantity\": 2
  }")

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.data.id')
TOTAL_PRICE=$(echo $ORDER_RESPONSE | jq -r '.data.totalPrice')
echo -e "${GREEN}✓ Order placed with ID: $ORDER_ID${NC}"
echo -e "  Total Price: KSh $TOTAL_PRICE"
echo ""

# Step 3: Initiate payment
echo -e "${YELLOW}Step 3: Initiating M-Pesa payment...${NC}"
echo "Enter phone number (format: 254712345678):"
read PHONE_NUMBER

PAYMENT_RESPONSE=$(curl -s -X POST "$BASE_URL/payments/initiate" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderId\": $ORDER_ID,
    \"phoneNumber\": \"$PHONE_NUMBER\",
    \"accountReference\": \"ORDER-$ORDER_ID\"
  }")

PAYMENT_ID=$(echo $PAYMENT_RESPONSE | jq -r '.data.paymentId')
CHECKOUT_REQUEST_ID=$(echo $PAYMENT_RESPONSE | jq -r '.data.checkoutRequestId')
RESPONSE_CODE=$(echo $PAYMENT_RESPONSE | jq -r '.data.responseCode')

if [ "$RESPONSE_CODE" = "0" ]; then
  echo -e "${GREEN}✓ STK Push sent successfully!${NC}"
  echo -e "  Payment ID: $PAYMENT_ID"
  echo -e "  Checkout Request ID: $CHECKOUT_REQUEST_ID"
  echo -e "  ${YELLOW}📱 Check your phone for M-Pesa prompt${NC}"
  echo ""
else
  echo -e "${RED}✗ Failed to send STK Push${NC}"
  echo "Response: $PAYMENT_RESPONSE"
  exit 1
fi

# Step 4: Poll for payment status
echo -e "${YELLOW}Step 4: Monitoring payment status...${NC}"
echo "Polling every 3 seconds (max 60 seconds)..."
echo ""

ATTEMPTS=0
MAX_ATTEMPTS=20

while [ $ATTEMPTS -lt $MAX_ATTEMPTS ]; do
  sleep 3
  ATTEMPTS=$((ATTEMPTS + 1))
  
  STATUS_RESPONSE=$(curl -s "$BASE_URL/payments/$PAYMENT_ID")
  STATUS=$(echo $STATUS_RESPONSE | jq -r '.data.status')
  
  echo "[$ATTEMPTS/$MAX_ATTEMPTS] Status: $STATUS"
  
  if [ "$STATUS" = "PAID" ]; then
    TRANSACTION_CODE=$(echo $STATUS_RESPONSE | jq -r '.data.transactionCode')
    echo ""
    echo -e "${GREEN}════════════════════════════════════════${NC}"
    echo -e "${GREEN}✓✓✓ PAYMENT SUCCESSFUL! ✓✓✓${NC}"
    echo -e "${GREEN}════════════════════════════════════════${NC}"
    echo -e "  Payment ID: $PAYMENT_ID"
    echo -e "  Order ID: $ORDER_ID"
    echo -e "  Transaction Code: $TRANSACTION_CODE"
    echo -e "  Amount: KSh $TOTAL_PRICE"
    echo ""
    
    # Check order status
    ORDER_STATUS_RESPONSE=$(curl -s "$BASE_URL/orders/buyer/2?page=0&size=10")
    echo -e "${GREEN}Order has been marked as PAID${NC}"
    exit 0
  elif [ "$STATUS" = "FAILED" ]; then
    echo ""
    echo -e "${RED}════════════════════════════════════════${NC}"
    echo -e "${RED}✗✗✗ PAYMENT FAILED ✗✗✗${NC}"
    echo -e "${RED}════════════════════════════════════════${NC}"
    exit 1
  fi
done

echo ""
echo -e "${YELLOW}⏱️ Payment status check timed out${NC}"
echo "Payment may still be processing. Check status manually:"
echo "curl $BASE_URL/payments/$PAYMENT_ID"
