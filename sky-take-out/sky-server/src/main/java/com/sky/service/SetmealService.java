package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

public interface SetmealService {
    /***
     * 新增套餐接口
     * @param setmealDTO
     */
    void save(SetmealDTO setmealDTO);

    /***
     * 套餐分页查询接口
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    /***
     * 批量删除套餐接口
     * @param ids
     */
    void delete(String ids);
}
