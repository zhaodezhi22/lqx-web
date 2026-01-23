package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuqin.opera.entity.Product;
import com.liuqin.opera.mapper.ProductMapper;
import com.lqx.opera.dto.CartItemDto;
import com.lqx.opera.entity.MallOrder;
import com.lqx.opera.entity.MallOrderItem;
import com.lqx.opera.mapper.MallOrderItemMapper;
import com.lqx.opera.mapper.MallOrderMapper;
import com.lqx.opera.service.MallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Long userId, String address, List<CartItemDto> items) {
        // 1. 创建 MallOrder 对象
        MallOrder order = new MallOrder();
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAddressSnapshot(address);
        order.setStatus(0); // 0-待付
        order.setTotalAmount(BigDecimal.ZERO);
        
        // 保存主订单以获取 ID (如果是自增主键，需要先保存)
        this.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 2. 循环处理商品
        for (CartItemDto item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + item.getProductId());
            }

            // 校验库存
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("库存不足: " + product.getName());
            }

            // 扣减库存
            product.setStock(product.getStock() - item.getQuantity());
            productMapper.updateById(product);

            // 累加金额
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // 保存明细
            MallOrderItem orderItem = new MallOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            mallOrderItemMapper.insert(orderItem);
        }

        // 3. 更新主订单金额
        order.setTotalAmount(totalAmount);
        this.updateById(order);
    }
}
