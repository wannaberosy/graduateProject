package org.example.dams.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.dams.common.Result;
import org.example.dams.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("/import")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    @Autowired
    private ImportService importService;
    
    /**
     * 导入数据资产
     */
    @PostMapping("/goods")
    public Result importGoods(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "storage", required = true) Integer storage,
            @RequestParam(value = "goodstype", required = true) Integer goodstype) {
        try {
            log.info("开始导入数据资产, 文件名: {}, 仓库ID: {}, 分类ID: {}, 文件大小: {} bytes", 
                file.getOriginalFilename(), storage, goodstype, file.getSize());
            
            // 验证参数
            if (storage == null || storage <= 0) {
                return Result.fail("请选择有效的仓库");
            }
            
            if (goodstype == null || goodstype <= 0) {
                return Result.fail("请选择有效的资产分类");
            }
            
            // 验证文件是否为空
            if (file.isEmpty()) {
                log.warn("上传的文件为空");
                return Result.fail("请选择要上传的文件");
            }
            
            // 验证文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null || !(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
                log.warn("上传的文件类型不正确: {}", fileName);
                return Result.fail("请上传Excel文件(xlsx或xls格式)");
            }
            
            // 导入数据 - 更新参数名称
            Map<String, Object> importResult = importService.importGoods(file, storage, goodstype);
            
            // 详细记录导入结果
            if ((Boolean) importResult.getOrDefault("success", false)) {
                log.info("导入成功，总记录: {}, 成功: {}", 
                    importResult.getOrDefault("total", 0),
                    importResult.getOrDefault("successCount", 0));
                return Result.success(importResult);
            } else {
                log.warn("导入失败: {}", importResult.getOrDefault("message", "未知错误"));
                if (importResult.containsKey("errors")) {
                    log.warn("错误详情: {}", importResult.get("errors"));
                }
                return Result.fail(
                    importResult.getOrDefault("message", "导入过程中发现错误").toString());
            }
        } catch (Exception e) {
            log.error("导入数据资产出现异常", e);
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入入库记录
     */
    @PostMapping("/records")
    public Result importRecords(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "storage", required = false) Integer storageId,
            @RequestParam(value = "goodstype", required = false) Integer goodstypeId) {
        if (file.isEmpty()) {
            return Result.fail();
        }
        
        // 验证文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            return Result.fail();
        }
        
        // 导入数据
        Map<String, Object> importResult = importService.importRecords(file, storageId, goodstypeId);
        if ((Boolean) importResult.get("success")) {
            return Result.success(importResult);
        } else {
            return Result.fail();
        }
    }
    
    /**
     * 下载导入模板
     */
    @GetMapping("/template/{type}")
    public void downloadTemplate(@PathVariable String type, HttpServletResponse response) throws IOException {
        log.info("下载{}导入模板", type);
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(type + "_import_template.xlsx", "UTF-8"));
        
        // 创建工作簿
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet1");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers;
        
        // 根据类型选择不同的模板标题 - 简化模板字段
        if ("goods".equals(type)) {
            // 只保留必要字段，匹配实体类字段名
            headers = new String[]{"资产名称", "资产数量", "备注"};
        } else {
            headers = new String[]{"名称", "数量", "备注"};
        }
        
        // 设置列标题
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        
        // 创建示例数据行
        Row exampleRow = sheet.createRow(1);
        if ("goods".equals(type)) {
            exampleRow.createCell(0).setCellValue("示例资产");
            exampleRow.createCell(1).setCellValue(10);
            exampleRow.createCell(2).setCellValue("示例备注信息");
        } else {
            exampleRow.createCell(0).setCellValue("示例名称");
            exampleRow.createCell(1).setCellValue(5);
            exampleRow.createCell(2).setCellValue("示例备注");
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 写入响应
        workbook.write(response.getOutputStream());
        workbook.close();
        
        log.info("{}导入模板下载成功", type);
    }
}
