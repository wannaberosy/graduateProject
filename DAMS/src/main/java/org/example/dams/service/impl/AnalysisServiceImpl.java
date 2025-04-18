package org.example.dams.service.impl;

import org.example.dams.mapper.GoodsMapper;
import org.example.dams.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public Map<String, Object> getStatistics() {
        try {
            Map<String, Object> statistics = goodsMapper.getStatistics();
            log.info("Raw statistics data: {}", statistics);
            
            // 确保所有数值都是有效的
            Map<String, Object> result = new HashMap<>();
            result.put("totalAssets", convertToInt(statistics.get("totalAssets")));
            result.put("newAssets", convertToInt(statistics.get("newAssets")));
            result.put("activeAssets", convertToInt(statistics.get("activeAssets")));
            result.put("activeUsers", convertToInt(statistics.get("activeUsers")));
            result.put("assetGrowth", convertToDouble(statistics.get("assetGrowth")));
            
            return result;
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            throw e;
        }
    }


    public List<Map<String, Object>> getTypeDistribution() {
        // 实现类型分布数据的获取逻辑
        return goodsMapper.getTypeDistribution();
    }

    @Override
    public Map<String, Object> getTrend(String timeRange) {
        try {
            List<Map<String, Object>> rawData = goodsMapper.getTrend(timeRange);
            log.info("Raw trend data: {}", rawData);
            
            List<String> dates = new ArrayList<>();
            List<Integer> values = new ArrayList<>();
            
            for (Map<String, Object> data : rawData) {
                dates.add((String) data.get("date"));
                values.add(convertToInt(data.get("count")));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("dates", dates);
            result.put("values", values);
            
            log.info("Processed trend data: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error getting trend data", e);
            throw e;
        }
    }


    public List<List<Object>> getHeatmap(String type) {
        List<Map<String, Object>> rawData = goodsMapper.getHeatmapData(type);
        List<List<Object>> result = new ArrayList<>();
        
        // 转换数据格式为 ECharts 热力图所需的格式
        // 格式: [['2023-01-01', 8], ['2023-01-02', 10], ...]
        for (Map<String, Object> data : rawData) {
            List<Object> item = new ArrayList<>();
            item.add(data.get("date"));
            
            // 将使用次数转换为 0-10 的范围
            int count = ((Number) data.get("count")).intValue();
            double normalizedCount = Math.min(10, count / 10.0);
            item.add(normalizedCount);
            
            result.add(item);
        }
        
        // 填充没有数据的日期，设置为 0
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalDate endDate = LocalDate.now();
        LocalDate current = startDate;
        
        Set<String> existingDates = rawData.stream()
            .map(m -> (String) m.get("date"))
            .collect(Collectors.toSet());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(formatter);
            if (!existingDates.contains(dateStr)) {
                List<Object> item = new ArrayList<>();
                item.add(dateStr);
                item.add(0);
                result.add(item);
            }
            current = current.plusDays(1);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getHeatmapData(String goodsType) {
        try {
            List<Map<String, Object>> data = goodsMapper.getHeatmapData(goodsType);
            log.info("Heatmap data: {}", data);
            return data;
        } catch (Exception e) {
            log.error("Error getting heatmap data", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getAssetTypeDistribution() {
        try {
            List<Map<String, Object>> data = goodsMapper.getAssetTypeDistribution();
            log.info("Asset type distribution: {}", data);
            return data;
        } catch (Exception e) {
            log.error("Error getting asset type distribution", e);
            throw e;
        }
    }

    @Override
    public byte[] generateAnalysisReport() throws Exception {
        try {
            // 创建一个新的Excel工作簿
            XSSFWorkbook workbook = new XSSFWorkbook();
            
            // 添加统计数据sheet
            addStatisticsSheet(workbook);
            
            // 添加资产类型分布sheet
            addTypeDistributionSheet(workbook);
            
            // 添加使用频率sheet
            addHeatmapSheet(workbook);

            // 将工作簿转换为字节数组
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            workbook.close();
            
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating analysis report", e);
            throw e;
        }
    }

    private void addStatisticsSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("统计数据");
        Map<String, Object> statistics = getStatistics();
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("指标");
        headerRow.createCell(1).setCellValue("数值");
        
        // 添加数据行
        int rowNum = 1;
        createDataRow(sheet, rowNum++, "总资产数", statistics.get("totalAssets"));
        createDataRow(sheet, rowNum++, "本月新增", statistics.get("newAssets"));
        createDataRow(sheet, rowNum++, "活跃资产", statistics.get("activeAssets"));
        createDataRow(sheet, rowNum++, "使用人数", statistics.get("activeUsers"));
        createDataRow(sheet, rowNum, "资产增长率", statistics.get("assetGrowth") + "%");
    }

    private void addTypeDistributionSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("资产类型分布");
        List<Map<String, Object>> distribution = getAssetTypeDistribution();
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("资产类型");
        headerRow.createCell(1).setCellValue("数量");
        headerRow.createCell(2).setCellValue("占比");
        
        // 计算总数
        int total = distribution.stream()
            .mapToInt(map -> ((Number) map.get("count")).intValue())
            .sum();
        
        // 添加数据行
        int rowNum = 1;
        for (Map<String, Object> type : distribution) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) type.get("typeName"));
            int count = ((Number) type.get("count")).intValue();
            row.createCell(1).setCellValue(count);
            row.createCell(2).setCellValue(String.format("%.2f%%", (count * 100.0 / total)));
        }
    }

    private void addHeatmapSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("使用频率分析");
        List<Map<String, Object>> heatmapData = getHeatmapData(null);
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("日期");
        headerRow.createCell(1).setCellValue("资产名称");
        headerRow.createCell(2).setCellValue("使用次数");
        
        // 添加数据行
        int rowNum = 1;
        for (Map<String, Object> data : heatmapData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) data.get("date"));
            row.createCell(1).setCellValue((String) data.get("goodsName"));
            row.createCell(2).setCellValue(((Number) data.get("useCount")).intValue());
        }
    }

    private void createDataRow(XSSFSheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(String.valueOf(value));
    }

    private int convertToInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double convertToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

