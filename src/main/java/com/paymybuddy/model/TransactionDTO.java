package com.paymybuddy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private int id;
    private String connectionName;
    private String description;
    private BigDecimal amount;
}
