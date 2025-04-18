package org.example.dams.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.dams.common.QueryPageParam;
import org.example.dams.common.Result;
import org.example.dams.entity.Menu;
import org.example.dams.entity.Storage;
import org.example.dams.entity.User;
import org.example.dams.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
@RestController
@RequestMapping("/storage")
public class StorageController {
    @Autowired
    private StorageService storageService;

    //删除
    @GetMapping("/del")
    public Result del(@RequestParam String id){
        return storageService.removeById(id)?Result.success():Result.fail();
    }
    //新增
    @PostMapping("/save")
    public Result save(@RequestBody Storage storage){
        return storageService.save(storage)?Result.success():Result.fail();
    }

    //更新
    @PostMapping("/update")
    public Result update(@RequestBody Storage storage){
        return storageService.updateById(storage)?Result.success():Result.fail();
    }

    @GetMapping("/findByName")
    public Result findByNo(@RequestParam String name){
        List list = storageService.lambdaQuery().eq(Storage::getName, name).list();
        return list.size()>0?Result.success(list):Result.fail();
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");

        //System.out.println("name==="+(String)param.get("name"));


        Page<Storage> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Storage> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(name)&& !"null".equals(name)){
            lambdaQueryWrapper.like(Storage::getName,name);
        }



        IPage result = storageService.pageCC(page,lambdaQueryWrapper);

        return Result.success(result.getRecords(),result.getTotal());
    }

    @GetMapping("/list")
    public Result list(){
        List list = storageService.lambdaQuery().list();
        return Result.success(list);
    }

    @GetMapping("/count")
    public Result count() {
        int count = storageService.count();
        return Result.success(count);
    }
}
