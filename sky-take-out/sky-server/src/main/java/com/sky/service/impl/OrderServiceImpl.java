package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;

    /***
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new OrderBusinessException(MessageConstant.USER_NOT_LOGIN);
        }
        // 异常情况的处理（收货地址为空、超出配送范围、购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        // 查询当前用户的购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 构造订单数据
        Orders order = new Orders();
        // 当前小程序只提交地址、支付方式和备注，不能将缺省的 Integer 复制到 int。
        BeanUtils.copyProperties(ordersSubmitDTO, order, "packAmount", "tablewareNumber");
        order.setPackAmount(ordersSubmitDTO.getPackAmount() == null ? 0 : ordersSubmitDTO.getPackAmount());
        order.setTablewareNumber(ordersSubmitDTO.getTablewareNumber() == null ? 0 : ordersSubmitDTO.getTablewareNumber());
        order.setTablewareStatus(ordersSubmitDTO.getTablewareStatus() == null ? 1 : ordersSubmitDTO.getTablewareStatus());
        order.setDeliveryStatus(ordersSubmitDTO.getDeliveryStatus() == null ? 1 : ordersSubmitDTO.getDeliveryStatus());
        order.setPayMethod(normalizePayMethod(ordersSubmitDTO.getPayMethod() == 0 ? null : ordersSubmitDTO.getPayMethod()));
        BigDecimal goodsAmount = shoppingCartList.stream()
                .map(cart -> cart.getAmount().multiply(BigDecimal.valueOf(cart.getNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setAmount(goodsAmount.add(BigDecimal.valueOf(order.getPackAmount())));
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        // 小程序在下单成功后直接跳转成功页，因此创建订单时一并完成模拟支付。
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setPayStatus(Orders.PAID);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(order.getOrderTime());
        if (order.getEstimatedDeliveryTime() == null) {
            order.setEstimatedDeliveryTime(order.getOrderTime().plusHours(1));
        }
        // 向订单表插入1条数据
        orderMapper.insert(order);

        // 订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }

        // 向明细表中插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);

        // 清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        log.info("下单并模拟支付成功，订单号：{}", order.getNumber());

        // 封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    /**
     * 模拟支付：直接完成付款，不调用微信支付，也不依赖支付回调。
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        if (ordersPaymentDTO == null) {
            throw new OrderBusinessException("支付参数不能为空");
        }
        Orders order = getCurrentUserOrder(ordersPaymentDTO.getOrderNumber());
        if (isPaymentCompleted(order)) {
            // 兼容下单后继续调用支付接口及支付重试，不重复更新订单。
            log.info("订单已完成模拟支付，直接返回成功，订单号：{}", order.getNumber());
            return OrderPaymentVO.builder().build();
        }
        completePayment(order, normalizePayMethod(ordersPaymentDTO.getPayMethod()));
        log.info("模拟支付成功，订单号：{}", order.getNumber());
        return OrderPaymentVO.builder().build();
    }

    private Integer normalizePayMethod(Integer payMethod) {
        if (payMethod == null) {
            payMethod = 1;
        }
        if (payMethod != 1 && payMethod != 2) {
            throw new OrderBusinessException("不支持的支付方式");
        }
        return payMethod;
    }

    private boolean isPaymentCompleted(Orders order) {
        return Orders.PAID.equals(order.getPayStatus())
                && (Orders.TO_BE_CONFIRMED.equals(order.getStatus())
                || Orders.CONFIRMED.equals(order.getStatus())
                || Orders.DELIVERY_IN_PROGRESS.equals(order.getStatus())
                || Orders.COMPLETED.equals(order.getStatus()));
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {
        Orders order = getCurrentUserOrder(outTradeNo);
        // 重复通知不覆盖订单状态和首次支付时间。
        if (Orders.PAID.equals(order.getPayStatus())) {
            return;
        }
        completePayment(order, order.getPayMethod() == null ? 1 : order.getPayMethod());
    }

    private Orders getCurrentUserOrder(String orderNumber) {
        if (!StringUtils.hasText(orderNumber)) {
            throw new OrderBusinessException("订单号不能为空");
        }
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new OrderBusinessException(MessageConstant.USER_NOT_LOGIN);
        }
        Orders order = orderMapper.getByNumberAndUserId(orderNumber, userId);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }

    private void completePayment(Orders order, Integer payMethod) {
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())
                || !Orders.UN_PAID.equals(order.getPayStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(Orders.TO_BE_CONFIRMED)
                .payMethod(payMethod)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        // 只更新待付款且未支付的订单，防止并发请求覆盖已取消或已支付的状态。
        if (orderMapper.markPaid(orders) != 1) {
            // 并发支付可能已由另一个请求完成，重新查询后确认结果。
            Orders latestOrder = getCurrentUserOrder(order.getNumber());
            if (!isPaymentCompleted(latestOrder)) {
                throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
            }
        }
    }
}
