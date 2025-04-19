package org.example.dams.service.impl;

import org.example.dams.mapper.GoodsMapper;
import org.example.dams.mapper.RecordMapper;
import org.example.dams.mapper.StorageMapper;
import org.example.dams.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private StorageMapper storageMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private RecordMapper recordMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取仓库总数
        result.put("storageCount", storageMapper.selectCount(null));
        
        // 获取商品总数
        result.put("goodsCount", goodsMapper.selectCount(null));
        
        // 获取今日出入库数据
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        
        Map<String, Object> todayStats = recordMapper.getTodayStats(today.getTime());
        result.put("todayIn", todayStats.get("inCount"));
        result.put("todayOut", todayStats.get("outCount"));
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities() {
        try {
            List<Map<String, Object>> activities = recordMapper.getRecentActivities();
            
            // 处理每条记录
            for (Map<String, Object> activity : activities) {
                // 确保 action 字段存在且为字符串类型
                Object actionObj = activity.get("action");
                activity.put("action", actionObj != null ? String.valueOf(actionObj) : "2");
                
                // 确保 count 为正数
                Object countObj = activity.get("count");
                if (countObj != null) {
                    int count = Integer.parseInt(String.valueOf(countObj));
                    activity.put("count", Math.abs(count));
                }
            }
            
            return activities;
        } catch (Exception e) {
            throw new RuntimeException("获取最近活动记录失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getInOutTrend() {
        // 获取最近7天的数据
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        
        List<Map<String, Object>> trends = recordMapper.getInOutTrend(calendar.getTime());
        
        // 处理数据
        List<String> dates = new ArrayList<>();
        List<Integer> inData = new ArrayList<>();
        List<Integer> outData = new ArrayList<>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        for (Map<String, Object> trend : trends) {
            dates.add(sdf.format(trend.get("date")));
            inData.add(((Number) trend.get("inCount")).intValue());
            outData.add(((Number) trend.get("outCount")).intValue());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("in", inData);
        result.put("out", outData);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getGoodsTypeDistribution() {
        return recordMapper.getGoodsTypeDistribution();
    }
}








