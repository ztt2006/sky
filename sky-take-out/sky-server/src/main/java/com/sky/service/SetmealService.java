package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

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

    /***
     * 根据套餐id查询套餐详情接口
     * @param id
     * @return
     */
    SetmealVO getById(Long id);

    /***
     * 更新套餐接口
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /***
     * 启用/停用套餐接口
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);
}
