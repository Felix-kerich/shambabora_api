#!/bin/bash

# Test script for Group Post endpoints
# Make sure the backend server is running before executing this script

BASE_URL="http://localhost:8080/api"
USER_TOKEN="YOUR_USER_JWT_TOKEN_HERE"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "Group Post Management Endpoint Tests"
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
            -H "Authorization: Bearer $USER_TOKEN" \
            -H "Content-Type: application/json" \
            "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method \
            -H "Authorization: Bearer $USER_TOKEN" \
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

# You'll need to replace GROUP_ID with an actual group ID
GROUP_ID="REPLACE_WITH_ACTUAL_GROUP_ID"

# Test 1: Create Group Post
echo -e "${YELLOW}Test 1: Create Group Post${NC}"
echo "POST /posts/group/$GROUP_ID"
POST_DATA='{
  "content": "This is a test post in the group! 🌾 #farming #collaboration"
}'
api_call "POST" "/posts/group/$GROUP_ID" "$POST_DATA"
print_result $? "Create group post"
echo ""

# Test 2: Get Group Posts
echo -e "${YELLOW}Test 2: Get Group Posts${NC}"
echo "GET /posts/group/$GROUP_ID?page=0&size=10"
api_call "GET" "/posts/group/$GROUP_ID?page=0&size=10"
print_result $? "Get group posts"
echo ""

# You'll need to replace POST_ID with an actual post ID from Test 2
POST_ID="REPLACE_WITH_ACTUAL_POST_ID"

# Test 3: Like Post
echo -e "${YELLOW}Test 3: Like Post${NC}"
echo "POST /posts/$POST_ID/like"
api_call "POST" "/posts/$POST_ID/like"
print_result $? "Like post"
echo ""

# Test 4: Unlike Post
echo -e "${YELLOW}Test 4: Unlike Post${NC}"
echo "DELETE /posts/$POST_ID/like"
api_call "DELETE" "/posts/$POST_ID/like"
print_result $? "Unlike post"
echo ""

# Test 5: Add Comment to Post
echo -e "${YELLOW}Test 5: Add Comment to Post${NC}"
echo "POST /posts/$POST_ID/comments"
COMMENT_DATA='{
  "content": "Great post! Very informative. 👍"
}'
api_call "POST" "/posts/$POST_ID/comments" "$COMMENT_DATA"
print_result $? "Add comment to post"
echo ""

# Test 6: Get Post Comments
echo -e "${YELLOW}Test 6: Get Post Comments${NC}"
echo "GET /posts/$POST_ID/comments?page=0&size=10"
api_call "GET" "/posts/$POST_ID/comments?page=0&size=10"
print_result $? "Get post comments"
echo ""

echo "======================================"
echo "Test suite completed"
echo "======================================"
echo ""
echo -e "${YELLOW}Note:${NC} Make sure to:"
echo "1. Replace USER_TOKEN with a valid user JWT token"
echo "2. Replace GROUP_ID with an actual group ID"
echo "3. Replace POST_ID with an actual post ID from Test 2"
echo "4. Make sure the user is a member of the group to create posts"
