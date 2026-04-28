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
     * Release expired ticket locks promptly so 30s timeout is close to real time.
     */
    @Scheduled(fixedDelay = 5000)
    public void cancelUnpaidOrders() {
        ticketService.cancelUnpaidOrders();
    }
}
