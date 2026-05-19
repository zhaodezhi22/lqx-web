package com.lqx.opera.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletWithdrawDto {
    private BigDecimal amount;
    private String accountName;
    private String accountNo;
    private String remark;
}
