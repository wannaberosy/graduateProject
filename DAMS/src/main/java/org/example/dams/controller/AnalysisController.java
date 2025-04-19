package org.example.dams.controller;

import org.example.dams.common.Result;
import org.example.dams.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @GetMapping("/statistics")
    public Result getStatistics() {
        try {
            Map<String, Object> statistics = analysisService.getStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail();
        }
    }

    @GetMapping("/type-distribution")
    public Result getTypeDistribution() {
        try {
            List<Map<String, Object>> data = analysisService.getAssetTypeDistribution();
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail();
        }
    }

    @GetMapping("/trend")
    public Result getTrend(@RequestParam(defaultValue = "month") String timeRange) {
        try {
            Map<String, Object> trend = analysisService.getTrend(timeRange);
            return Result.success(trend);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail();
        }
    }

    @GetMapping("/heatmap")
    public Result getHeatmap(@RequestParam(required = false) String goodsType) {
        try {
            List<Map<String, Object>> data = analysisService.getHeatmapData(goodsType);
            return Result.success(data);
        } catch (Exception e) {

            return Result.fail();
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAnalysisReport() {
        try {
            byte[] reportData = analysisService.generateAnalysisReport();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                "资产分析报告_" + LocalDate.now() + ".xlsx");
            
            return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
        } catch (Exception e) {

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}





