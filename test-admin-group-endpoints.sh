#!/bin/bash

# Test script for Admin Group Management endpoints
# Make sure the backend server is running before executing this script

BASE_URL="http://localhost:8080/api"
ADMIN_TOKEN="YOUR_ADMIN_JWT_TOKEN_HERE"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "Admin Group Management Endpoint Tests"
echo "======================================"
echo ""

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Function to make API call
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -H "Authorization: Bearer $ADMIN_TOKEN" \
            -H "Content-Type: application/json" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -H "Authorization: Bearer $ADMIN_TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    echo "Response code: $http_code"
    echo "Response body: $body" | jq '.' 2>/dev/null || echo "$body"
    echo ""
    
    return $http_code
}

# Test 1: Create Group as Admin
echo -e "${YELLOW}Test 1: Create Group as Admin${NC}"
echo "POST /admin/collaboration/groups"
GROUP_DATA='{
  "name": "Test Admin Group",
  "description": "This is a test group created by admin for testing purposes"
}'
api_call "POST" "/admin/collaboration/groups" "$GROUP_DATA"
print_result $? "Create group as admin"
echo ""

# Test 2: Get All Groups (Admin)
echo -e "${YELLOW}Test 2: Get All Groups (Admin)${NC}"
echo "GET /admin/collaboration/groups"
api_call "GET" "/admin/collaboration/groups"
print_result $? "Get all groups"
echo ""

# Test 3: Get Group Statistics
echo -e "${YELLOW}Test 3: Get Group Statistics${NC}"
echo "GET /admin/collaboration/groups/stats"
api_call "GET" "/admin/collaboration/groups/stats"
print_result $? "Get group statistics"
echo ""

# You'll need to replace GROUP_ID with an actual group ID from Test 2
GROUP_ID="REPLACE_WITH_ACTUAL_GROUP_ID"

# Test 4: Update Group as Admin
echo -e "${YELLOW}Test 4: Update Group as Admin${NC}"
echo "PUT /admin/collaboration/groups/$GROUP_ID"
UPDATE_DATA='{
  "name": "Updated Admin Group",
  "description": "This group has been updated by admin"
}'
api_call "PUT" "/admin/collaboration/groups/$GROUP_ID" "$UPDATE_DATA"
print_result $? "Update group as admin"
echo ""

# Test 5: Freeze Group
echo -e "${YELLOW}Test 5: Freeze Group${NC}"
echo "POST /admin/collaboration/groups/$GROUP_ID/freeze"
FREEZE_DATA='{
  "freeze": true,
  "reason": "Testing freeze functionality"
}'
api_call "POST" "/admin/collaboration/groups/$GROUP_ID/freeze" "$FREEZE_DATA"
print_result $? "Freeze group"
echo ""

# Test 6: Unfreeze Group
echo -e "${YELLOW}Test 6: Unfreeze Group${NC}"
echo "POST /admin/collaboration/groups/$GROUP_ID/freeze"
UNFREEZE_DATA='{
  "freeze": false,
  "reason": "Testing complete, unfreezing group"
}'
api_call "POST" "/admin/collaboration/groups/$GROUP_ID/freeze" "$UNFREEZE_DATA"
print_result $? "Unfreeze group"
echo ""

# Test 7: Force Delete Group (WARNING: This will delete the group)
# Uncomment to test deletion
# echo -e "${YELLOW}Test 7: Force Delete Group${NC}"
# echo "DELETE /admin/collaboration/groups/$GROUP_ID/force"
# api_call "DELETE" "/admin/collaboration/groups/$GROUP_ID/force"
# print_result $? "Force delete group"
# echo ""

echo "======================================"
echo "Test suite completed"
echo "======================================"
echo ""
echo -e "${YELLOW}Note:${NC} Make sure to:"
echo "1. Replace ADMIN_TOKEN with a valid admin JWT token"
echo "2. Replace GROUP_ID with an actual group ID from Test 2"
echo "3. Uncomment Test 7 if you want to test force deletion"
