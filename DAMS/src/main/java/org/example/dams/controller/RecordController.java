package org.example.dams.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.dams.common.QueryPageParam;
import org.example.dams.common.Result;
import org.example.dams.entity.Goods;
import org.example.dams.entity.Record;
import org.example.dams.entity.RecordRes;
import org.example.dams.service.GoodsService;
import org.example.dams.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zqy
 * @since 2025-04-13
 */
@Slf4j
@RestController
@RequestMapping("/record")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @Autowired
    private GoodsService goodsService;

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String goodstype = (String)param.get("goodstype");
        String storage = (String)param.get("storage");
        String roleId = (String)param.get("roleId");
        String userId = (String)param.get("userId");

        //System.out.println("name==="+(String)param.get("name"));


        Page<Record> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        QueryWrapper<Record> queryWrapper = new QueryWrapper();
        queryWrapper.apply("a.goods=b.id and b.`storage`=c.id and b.goodsType = d.id");
        if("2".equals(roleId)){
            queryWrapper.apply("a.userId="+userId);
        }

        if(StringUtils.isNotBlank(name)&& !"null".equals(name)){
            queryWrapper.like("b.name",name);
        }
        if(StringUtils.isNotBlank(goodstype)&& !"null".equals(goodstype)){
            queryWrapper.eq("d.id",goodstype);
        }
        if(StringUtils.isNotBlank(storage)&& !"null".equals(storage)){
            queryWrapper.eq("c.id",storage);
        }



        IPage result = recordService.pageCC(page,queryWrapper);

        return Result.success(result.getRecords(),result.getTotal());
    }

    @PostMapping("/save")
    public Result save(@RequestBody Record record) {
        try {
            // 设置创建时间
            record.setCreatetime(new Date());
            
            // 验证操作类型
            if (!"1".equals(record.getAction()) && !"2".equals(record.getAction())) {
                return Result.error("无效的操作类型");
            }
            
            // 保存记录
            boolean success = recordService.save(record);
            
            if (success) {
                // 更新商品库存
                Goods goods = goodsService.getById(record.getGoods());
                if (goods != null) {
                    int newCount = goods.getCount();
                    if ("1".equals(record.getAction())) {
                        // 入库
                        newCount += record.getCount();
                    } else {
                        // 出库
                        newCount -= record.getCount();
                        if (newCount < 0) {
                            return Result.error("库存不足");
                        }
                    }
                    goods.setCount(newCount);
                    goodsService.updateById(goods);
                }
                
                return Result.success();
            } else {
                return Result.error("保存失败");
            }
        } catch (Exception e) {
            log.error("保存出入库记录失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    @GetMapping("/todayCount")
    public Result todayCount() {
        try {
            log.info("开始获取今日出入库统计");
            Map<String, Object> counts = recordService.getTodayInOutCount();
            log.info("获取今日出入库统计成功：{}", counts);
            return Result.success(counts);
        } catch (Exception e) {
            log.error("获取今日出入库统计失败", e);
            return Result.error("获取今日统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/recent")
    public Result recent() {
        try {
            log.info("开始获取最近记录");
            List<Map<String, Object>> records = recordService.getRecentRecords();
            log.info("获取最近记录成功，记录数：{}", records.size());
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取最近记录失败", e);
            return Result.error("获取最近记录失败，详细错误：" + e.getMessage());
        }
    }

    @GetMapping("/trend")
    public Result trend() {
        try {
            log.info("开始获取趋势数据");
            Map<String, Object> trend = recordService.getInOutTrend();
            log.info("获取趋势数据成功：{}", trend);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            return Result.error("获取趋势数据失败，详细错误：" + e.getMessage());
        }
    }

    @GetMapping("/test-recent")
    public Result testRecent() {
        List<Map<String, Object>> records = recordService.getRecentRecords();
        // 打印每条记录的 action 值
        for (Map<String, Object> record : records) {
            log.info("Record ID: {}, Action: {}", record.get("id"), record.get("action"));
        }
        return Result.success(records);
    }
}
