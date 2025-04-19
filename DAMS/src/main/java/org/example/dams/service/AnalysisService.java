package org.example.dams.service;

import java.util.List;
import java.util.Map;

public interface AnalysisService {
    Map<String, Object> getStatistics();
    Map<String, Object> getTrend(String timeRange);
    List<Map<String, Object>> getHeatmapData(String goodsType);
    List<Map<String, Object>> getAssetTypeDistribution();
    byte[] generateAnalysisReport() throws Exception;
}





