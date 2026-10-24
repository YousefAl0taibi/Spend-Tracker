package com.yousef.spendtracker.transaction;

public record CategoryResponse(Long id, String nameEn, String nameAr) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getNameEn(), category.getNameAr());
    }
}
