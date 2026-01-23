package com.lqx.opera.task;

import com.lqx.opera.service.TicketService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTask {

    private final TicketService ticketService;

    public OrderTask(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Cancel unpaid orders every minute
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void cancelUnpaidOrders() {
        // System.out.println("Running scheduled task: cancelUnpaidOrders");
        ticketService.cancelUnpaidOrders();
    }
}
