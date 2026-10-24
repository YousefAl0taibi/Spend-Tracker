package com.yousef.spendtracker.transaction;

import jakarta.validation.constraints.NotNull;

public record CategoryUpdateRequest(@NotNull Long categoryId) {
}
