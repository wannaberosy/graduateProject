package org.example.dams.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.dams.common.QueryPageParam;
import org.example.dams.common.Result;
import org.example.dams.entity.Goods;
import org.example.dams.service.GoodsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    //删除
    @GetMapping("/del")
    public Result del(@RequestParam String id){
        return goodsService.removeById(id)?Result.success():Result.fail();
    }
    //新增
    @PostMapping("/save")
    public Result save(@RequestBody Goods goods){
        return goodsService.save(goods)?Result.success():Result.fail();
    }

    //更新
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        return goodsService.updateById(goods)?Result.success():Result.fail();
    }

    @GetMapping("/findByName")
    public Result findByNo(@RequestParam String name){
        List list = goodsService.lambdaQuery().eq(Goods::getName, name).list();
        return list.size()>0?Result.success(list):Result.fail();
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String goodstype = (String)param.get("goodstype");
        String storage = (String)param.get("storage");
        //System.out.println("name==="+(String)param.get("name"));


        Page<Goods> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Goods> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(name)&& !"null".equals(name)){
            lambdaQueryWrapper.like(Goods::getName,name);
        }
        if(StringUtils.isNotBlank(goodstype)&& !"null".equals(goodstype)){
            lambdaQueryWrapper.eq(Goods::getGoodstype,goodstype);
        }
        if(StringUtils.isNotBlank(storage)&& !"null".equals(storage)){
            lambdaQueryWrapper.eq(Goods::getStorage,storage);
        }



        IPage result = goodsService.pageCC(page,lambdaQueryWrapper);

        return Result.success(result.getRecords(),result.getTotal());
    }

    @GetMapping("/count")
    public Result count() {
        int count = goodsService.count();
        return Result.success(count);
    }

    @GetMapping("/warning")
    public Result warning() {
        List<Goods> warningGoods = goodsService.getWarningGoods();
        return Result.success(warningGoods);
    }

    @GetMapping("/typeDistribution")
    public Result typeDistribution() {
        List<Map<String, Object>> distribution = goodsService.getTypeDistribution();
        return Result.success(distribution);
    }
}
