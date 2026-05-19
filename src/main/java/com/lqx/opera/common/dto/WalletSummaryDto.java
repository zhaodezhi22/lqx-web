package com.lqx.opera.common.dto;

import com.lqx.opera.entity.WalletWithdrawRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WalletSummaryDto {
    private BigDecimal mallIncome;
    private BigDecimal ticketIncome;
    private BigDecimal totalIncome;
    private BigDecimal withdrawnAmount;
    private BigDecimal availableAmount;
    private List<WalletWithdrawRequest> recentRecords;
}
