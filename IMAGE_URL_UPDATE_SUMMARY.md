# Image URL Feature Added to Marketplace Products

## Summary
Successfully added `imageUrl` field to the marketplace products module, allowing products to have image URLs that can be saved to the database and retrieved in all product endpoints.

## Changes Made

### 1. Database Migration
**File:** `src/main/resources/db/migration/V4__add_image_url_to_products.sql`
- Added new migration to add `image_url` column to the `products` table
- Column type: `VARCHAR(500)` (nullable)

### 2. Product Entity
**File:** `src/main/java/com/app/shambabora/modules/marketplace/entity/Product.java`
- Added `imageUrl` field with `@Column(length = 500)` annotation
- Mapped to database column `image_url`

### 3. Product DTO
**File:** `src/main/java/com/app/shambabora/modules/marketplace/dto/ProductDTO.java`
- Added `imageUrl` field of type `String`
- Will be included in all API responses

### 4. Product Service
**File:** `src/main/java/com/app/shambabora/modules/marketplace/service/ProductService.java`
- Updated `create()` method to save `imageUrl` when creating a product
- Updated `update()` method to update `imageUrl` when updating a product
- Updated `toDto()` method to include `imageUrl` in the response

## API Usage

### Create Product with Image URL
```json
POST /api/marketplace/products
{
  "name": "Fresh Tomatoes",
  "description": "Organic tomatoes",
  "price": 150.00,
  "unit": "kg",
  "quantity": 100,
  "sellerId": 1,
  "imageUrl": "https://example.com/images/tomatoes.jpg"
}
```

### Update Product Image URL
```json
PUT /api/marketplace/products/{id}
{
  "imageUrl": "https://example.com/images/new-tomatoes.jpg"
}
```

### Get Product by ID (includes imageUrl)
```json
GET /api/marketplace/products/{id}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Fresh Tomatoes",
    "description": "Organic tomatoes",
    "price": 150.00,
    "unit": "kg",
    "quantity": 100,
    "available": true,
    "sellerId": 1,
    "imageUrl": "https://example.com/images/tomatoes.jpg",
    "createdAt": "2026-01-26T10:00:00Z",
    "updatedAt": "2026-01-26T10:00:00Z"
  }
}
```

### Get All Products (includes imageUrl in all items)
```json
GET /api/marketplace/products

Response:
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Fresh Tomatoes",
        "imageUrl": "https://example.com/images/tomatoes.jpg",
        ...
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

## Notes
- The `imageUrl` field is optional (nullable in database)
- You can send the image URL when creating a new product
- You can update the image URL when updating an existing product
- The image URL will be included in:
  - Get product by ID endpoint
  - Search products endpoint
  - Get seller products endpoint
  - Get seller available products endpoint

## Next Steps
1. Run the application to apply the database migration
2. Test creating products with image URLs
3. Test retrieving products to verify image URLs are returned
