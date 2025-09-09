package com.app.shambabora.entity;

public enum Role {
    FARMER,           // Can self-register
    BUYER,            // Can self-register  
    EXTENSION_OFFICER, // Requires admin approval
    STAFF,            // Admin can create
    ADMIN             // Admin can create
}
