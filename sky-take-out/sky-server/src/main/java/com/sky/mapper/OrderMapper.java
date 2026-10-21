package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 订单状态未变化时，一次性保存取消信息和模拟退款结果。
     */
    @Update("update orders set status = #{order.status}, pay_status = #{order.payStatus}, " +
            "cancel_reason = #{order.cancelReason}, cancel_time = #{order.cancelTime} " +
            "where id = #{order.id} and user_id = #{order.userId} " +
            "and status = #{previousStatus} and pay_status = #{previousPayStatus}")
    int cancelIfUnchanged(@Param("order") Orders order,
                          @Param("previousStatus") Integer previousStatus,
                          @Param("previousPayStatus") Integer previousPayStatus);

    /***
     * 分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /***
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);
}
