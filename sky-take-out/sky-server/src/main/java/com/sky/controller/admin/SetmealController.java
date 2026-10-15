package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐管理")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;


    /***
     * 新增套餐接口
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐接口")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐接口，套餐信息：{}", setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }

    /***
     * 套餐分页查询接口
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询接口")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询接口，查询参数：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /***
     * 批量删除套餐接口
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐接口")
    public Result delete(@RequestParam String ids) {
        log.info("批量删除套餐接口，套餐ID列表：{}", ids);
        setmealService.delete(ids);
        return Result.success();
    }

    /***
     * 根据套餐id查询套餐详情接口
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据套餐id查询套餐详情接口")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据套餐id查询套餐详情接口，套餐ID：{}", id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }


    /***
     * 更新套餐接口
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("更新套餐接口")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("更新套餐接口，套餐信息：{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
