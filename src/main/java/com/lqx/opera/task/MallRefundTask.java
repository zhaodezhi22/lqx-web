package com.lqx.opera.task;

import com.lqx.opera.service.MallOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MallRefundTask {

    private final MallOrderService mallOrderService;

    public MallRefundTask(MallOrderService mallOrderService) {
        this.mallOrderService = mallOrderService;
    }

    /**
     * Auto approve refund requests when neither seller nor reviewer handles them within 48 hours.
     */
    @Scheduled(fixedDelay = 3600000)
    public void autoApproveExpiredRefunds() {
        mallOrderService.autoApproveExpiredRefunds();
    }
}
