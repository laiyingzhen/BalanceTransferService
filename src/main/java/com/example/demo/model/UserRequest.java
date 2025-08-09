package com.example.demo.model;

import java.math.BigDecimal;

public class UserRequest {
    private String userId;
    private BigDecimal initialBalance;

    public String getUserId() {
        return userId;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
}
