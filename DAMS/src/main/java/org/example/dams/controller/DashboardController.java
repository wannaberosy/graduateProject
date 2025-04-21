package org.example.dams.controller;

import org.example.dams.common.Result;
import org.example.dams.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // 获取总览数据
    @GetMapping("/overview")
    public Result getOverview() {
        try {
            return Result.success(dashboardService.getOverview());
        } catch (Exception e) {
            return Result.error("获取总览数据失败：" + e.getMessage());
        }
    }

    // 获取最近操作记录
    @GetMapping("/recent-activities")
    public Result getRecentActivities() {
        try {
            return Result.success(dashboardService.getRecentActivities());
        } catch (Exception e) {
            return Result.error("获取最近操作记录失败：" + e.getMessage());
        }
    }

    // 获取出入库趋势
    @GetMapping("/inout-trend")
    public Result getInOutTrend() {
        try {
            return Result.success(dashboardService.getInOutTrend());
        } catch (Exception e) {
            return Result.error("获取出入库趋势失败：" + e.getMessage());
        }
    }

    @GetMapping("/goods-type-distribution")
    public Result getGoodsTypeDistribution() {
        try {
            List<Map<String, Object>> distribution = dashboardService.getGoodsTypeDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.error("获取资产分类统计失败: " + e.getMessage());
        }
    }
}










