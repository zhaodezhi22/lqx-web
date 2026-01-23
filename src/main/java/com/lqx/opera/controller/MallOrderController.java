package com.lqx.opera.controller;

import com.liuqin.opera.common.Result;
import com.lqx.opera.dto.CreateOrderRequest;
import com.lqx.opera.service.MallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MallOrderController {

    @Autowired
    private MallOrderService mallOrderService;

    @PostMapping("/api/mall/order/create")
    public Result createOrder(@RequestBody CreateOrderRequest request) {
        mallOrderService.createOrder(request.getUserId(), request.getAddress(), request.getItems());
        return Result.success("订单创建成功");
    }
}
