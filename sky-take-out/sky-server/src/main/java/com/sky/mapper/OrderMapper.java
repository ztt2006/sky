package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {
    /***
     * 插入订单
     * @param order
     */
    void insert(Orders order);

    /**
     * 根据订单号和用户id查询订单
     *
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 将当前用户的待付款订单更新为已支付，返回实际更新行数。
     */
    @Update("update orders set status = #{status}, pay_status = #{payStatus}, " +
            "pay_method = #{payMethod}, checkout_time = #{checkoutTime} " +
            "where id = #{id} and user_id = #{userId} and status = 1 and pay_status = 0")
    int markPaid(Orders orders);
}
