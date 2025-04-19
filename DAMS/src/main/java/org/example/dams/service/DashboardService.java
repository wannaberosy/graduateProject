package org.example.dams.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    // 获取总览数据
    Map<String, Object> getOverview();
    
    // 获取最近操作记录
    List<Map<String, Object>> getRecentActivities();
    
    // 获取出入库趋势
    Map<String, Object> getInOutTrend();

    List<Map<String, Object>> getGoodsTypeDistribution();
}








