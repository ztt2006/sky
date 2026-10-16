package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/***
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;


    /***
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 清除缓存
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /***
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /***
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        dishService.deleteBatch(ids);
        // 将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");
        return Result.success();
    }


    /***
     * 根据菜品id查询菜品详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据菜品id查询菜品详情")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据菜品id查询菜品详情：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /***
     * 更新菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("更新菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("更新菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 将所有的菜品缓存数据清理掉，所有以dish_开头的key
        cleanCache("dish_*");
        return Result.success();
    }


    /***
     * 根据分类Id查询菜品列表
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类Id查询菜品列表")
    public Result<List<DishVO>> list(@RequestParam Long categoryId) {
        log.info("根据分类Id查询菜品列表：{}", categoryId);
        List<DishVO> dishVOList = dishService.listByCategoryId(categoryId);
        return Result.success(dishVOList);
    }

    /***
     * 菜品起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> stratOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售停售：{}", id);
        dishService.startOrStop(status, id);
// /将所有的菜品缓存数据清理掉，所有以dish_开头的key

        cleanCache("dish_*");
        return Result.success();
    }

    /***
     * 清除缓存
     * @param pattern
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}

