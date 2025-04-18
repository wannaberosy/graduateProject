package org.example.dams.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Record;
import org.example.dams.entity.Storage;
import org.example.dams.mapper.RecordMapper;
import org.example.dams.mapper.StorageMapper;
import org.example.dams.service.RecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zqy
 * @since 2025-04-13
 */
@Slf4j
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {
    @Resource
    private RecordMapper recordMapper;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @Override
    public IPage pageCC(IPage<Record> page, Wrapper wrapper) {
        return recordMapper.pageCC(page,wrapper);
    }

    @Override
    public Map<String, Object> getTodayInOutCount() {
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startTime = calendar.getTime();
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endTime = calendar.getTime();

            QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
            queryWrapper.between("createtime", startTime, endTime);

            List<Record> records = recordMapper.selectList(queryWrapper);

            int inCount = 0;
            int outCount = 0;
            for (Record record : records) {
                if ("1".equals(record.getAction())) {
                    inCount += record.getCount();
                } else if ("2".equals(record.getAction())) {
                    outCount += record.getCount();
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("in", inCount);
            result.put("out", outCount);
            return result;
        } catch (Exception e) {
            log.error("获取今日统计失败", e);
            throw new RuntimeException("获取今日统计失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 获取最近的操作记录
     */
    @Override
    public List<Map<String, Object>> getRecentRecords() {
        try {
            QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("createtime")
                    .last("LIMIT 10");

            List<Record> records = recordMapper.selectRecentRecords(queryWrapper);
            List<Map<String, Object>> result = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            for (Record record : records) {
                // 打印原始数据
                log.info("原始记录: id={}, action={}, count={}, goodsname={}", 
                        record.getId(), record.getAction(), record.getCount(), record.getGoodsname());

                Map<String, Object> map = new HashMap<>();
                map.put("id", record.getId());
                map.put("goodsname", record.getGoodsname());
                map.put("username", record.getUsername());
                map.put("count", record.getCount());
                map.put("action", record.getAction());
                map.put("createtime", record.getCreatetime() != null ? 
                        sdf.format(record.getCreatetime()) : null);
                map.put("remark", record.getRemark());
                
                // 打印转换后的数据
                log.info("转换后数据: {}", map);
                
                result.add(map);
            }

            // 打印最终结果
            log.info("返回结果集大小: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("获取最近记录失败", e);
            throw new RuntimeException("获取最近记录失败：" + e.getMessage(), e);
        }
    }

    // 修复日期格式化问题
    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
    
    /**
     * 获取最近7天的出入库趋势
     */
    @Override
    public Map<String, Object> getInOutTrend() {
        try {
            log.info("开始获取趋势数据");
            
            // 获取7天前的起始时间点（使用东八区时间）
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            calendar.add(Calendar.DAY_OF_MONTH, -6); // 获取7天前的日期
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date startTime = calendar.getTime();

            log.info("查询起始时间：{}", startTime);

            // 获取当前时间作为结束时间点
            Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            endCalendar.set(Calendar.MILLISECOND, 999);
            Date endTime = endCalendar.getTime();

            // 修改查询条件，使用between而不是ge
            QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
            queryWrapper.between("createtime", startTime, endTime)
                       .orderByAsc("createtime");

            log.info("执行查询，开始时间：{}，结束时间：{}", startTime, endTime);
            List<Record> records = recordMapper.selectList(queryWrapper);
            log.info("查询到 {} 条记录", records.size());

            // 使用东八区的SimpleDateFormat
            SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
            dateSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            // 使用LinkedHashMap保持日期顺序
            Map<String, int[]> dailyCounts = new LinkedHashMap<>();

            // 初始化7天的数据
            calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            calendar.add(Calendar.DAY_OF_MONTH, -6);
            for (int i = 0; i < 7; i++) {
                String dateStr = dateSdf.format(calendar.getTime());
                dailyCounts.put(dateStr, new int[2]); // [0]为入库数，[1]为出库数
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // 统计数据
            for (Record record : records) {
                try {
                    String dateStr = dateSdf.format(record.getCreatetime());
                    int[] counts = dailyCounts.get(dateStr);
                    if (counts != null) {
                        // 打印每条记录的信息用于调试
                        log.debug("处理记录: 日期={}, 操作类型={}, 数量={}", 
                                dateStr, record.getAction(), record.getCount());
                        
                        if ("1".equals(record.getAction())) {
                            counts[0] += record.getCount();
                        } else if ("2".equals(record.getAction())) {
                            counts[1] += record.getCount();
                        }
                    }
                } catch (Exception e) {
                    log.error("处理单条记录时出错：{}", record, e);
                }
            }

            // 整理结果
            List<String> dates = new ArrayList<>(dailyCounts.keySet());
            List<Integer> inCounts = new ArrayList<>();
            List<Integer> outCounts = new ArrayList<>();

            // 打印每天的统计结果用于调试
            for (String date : dates) {
                int[] counts = dailyCounts.get(date);
                inCounts.add(counts[0]);
                outCounts.add(counts[1]);
                log.debug("日期: {}, 入库: {}, 出库: {}", date, counts[0], counts[1]);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("dates", dates);
            result.put("in", inCounts);
            result.put("out", outCounts);

            log.info("趋势数据处理完成：{}", result);
            return result;
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            throw new RuntimeException("获取趋势数据失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean save(Record record) {
        try {
            // 验证必要字段
            if (record.getGoods() == null || record.getCount() == null || 
                StringUtils.isEmpty(record.getAction())) {
                throw new RuntimeException("必要字段不能为空");
            }
            
            // 插入记录
            int result = recordMapper.insert(record);
            return result > 0;
        } catch (Exception e) {
            log.error("保存出入库记录失败", e);
            throw new RuntimeException("保存失败：" + e.getMessage());
        }
    }
}
