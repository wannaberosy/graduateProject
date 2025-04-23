package org.example.dams.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.example.dams.dto.GoodsImportDTO;
import org.example.dams.dto.RecordImportDTO;
import org.example.dams.entity.*;
import org.example.dams.service.GoodsService;
import org.example.dams.service.ImportService;
import org.example.dams.service.RecordService;
import org.example.dams.service.ImportHistoryService;
import org.example.dams.service.StorageService;
import org.example.dams.service.GoodstypeService;
import org.example.dams.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
/*import org.slf4j.Logger;
import org.slf4j.LoggerFactory;*/
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
public class ImportServiceImpl implements ImportService {

    @Autowired
    private GoodsService goodsService;
    
    @Autowired
    private RecordService recordService;
    
    @Autowired(required = false)
    private ImportHistoryService importHistoryService;
    
    @Autowired
    private StorageService storageService;
    
    @Autowired
    private GoodstypeService goodstypeService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 导入数据资产
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importGoods(MultipartFile file, Integer storage, Integer goodstype) {
        Map<String, Object> result = new HashMap<>();
        List<GoodsImportDTO> successList = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        int totalRows = 0;
        
        try {
            log.info("开始解析Excel文件: {}, 仓库ID: {}, 分类ID: {}", 
                file.getOriginalFilename(), storage, goodstype);
            
            // 验证仓库是否存在
            Storage storageEntity = storageService.getById(storage);
            if (storageEntity == null) {
                result.put("success", false);
                result.put("message", "选择的仓库不存在");
                return result;
            }
            
            // 验证资产类型是否存在
            Goodstype goodstypeEntity = goodstypeService.getById(goodstype);
            if (goodstypeEntity == null) {
                result.put("success", false);
                result.put("message", "选择的资产类型不存在");
                return result;
            }
            
            // 获取Excel工作簿
            Workbook workbook = getWorkbook(file);
            if (workbook == null) {
                result.put("success", false);
                result.put("message", "无法解析Excel文件，请确保文件格式正确");
                return result;
            }
            
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.put("success", false);
                result.put("message", "Excel文件中没有找到工作表");
                return result;
            }
            
            // 获取总行数
            int rows = sheet.getPhysicalNumberOfRows();
            log.info("Excel文件总行数: {}", rows);
            
            // 如果行数少于2（没有数据行，只有标题行）
            if (rows < 2) {
                result.put("success", false);
                result.put("message", "Excel文件中没有数据");
                return result;
            }
            
            // 获取标题行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.put("success", false);
                result.put("message", "Excel文件中没有找到标题行");
                return result;
            }
            
            // 验证标题行 - 新的期望标题
            String[] expectedHeaders = {"资产名称", "资产数量", "资产描述"};
            boolean headersValid = validateHeaders(headerRow, expectedHeaders);
            if (!headersValid) {
                result.put("success", false);
                result.put("message", "Excel文件的列标题不符合模板要求");
                return result;
            }
            
            // 开始解析数据行
            totalRows = rows - 1; // 减去标题行
            for (int i = 1; i < rows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    errorMessages.add("第" + (i + 1) + "行为空行，已跳过");
                    continue;
                }
                
                try {
                    // 解析行数据到DTO
                    GoodsImportDTO goodsDTO = parseRowToDTO(row, headerRow);
                    
                    // 验证数据有效性
                    String validationError = validateGoodsDTO(goodsDTO);
                    if (validationError != null) {
                        errorMessages.add("第" + (i + 1) + "行: " + validationError);
                        continue;
                    }
                    
                    // 添加到成功列表
                    successList.add(goodsDTO);
                } catch (Exception e) {
                    log.error("解析第{}行时出错", i + 1, e);
                    errorMessages.add("第" + (i + 1) + "行解析失败: " + e.getMessage());
                }
            }
            
            // 关闭工作簿
            workbook.close();
            
            // 如果没有成功解析的数据
            if (successList.isEmpty()) {
                result.put("success", false);
                result.put("message", "没有解析到有效数据");
                result.put("errors", errorMessages);
                return result;
            }
            
            // 将解析的数据保存到数据库
            int savedCount = saveImportedGoods(successList, storage, goodstype);
            
            // 准备返回结果
            result.put("success", true);
            result.put("total", totalRows);
            result.put("successCount", savedCount);
            result.put("failedCount", totalRows - savedCount);
            
            if (!errorMessages.isEmpty()) {
                result.put("message", "部分数据导入成功，但有" + errorMessages.size() + "条记录出现错误");
                result.put("errors", errorMessages);
            } else {
                result.put("message", "全部数据导入成功");
            }
            
            return result;
        } catch (Exception e) {
            log.error("导入商品数据失败", e);
            result.put("success", false);
            result.put("message", "导入过程中发生错误: " + e.getMessage());
            errorMessages.add("系统错误: " + e.getMessage());
            result.put("errors", errorMessages);
            
            // 手动触发回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return result;
        }
    }
    
    /**
     * 导入入库记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importRecords(MultipartFile file, Integer storageId, Integer goodstypeId) {
        log.info("开始导入入库记录，文件名: {}, 仓库ID: {}, 分类ID: {}", file.getOriginalFilename(), storageId, goodstypeId);
        
        Map<String, Object> result = new HashMap<>();
        List<RecordImportDTO> importList = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        
        try {
            // 获取上传的Excel文件
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            // 从第二行开始读取数据（跳过表头）
            int rowCount = 0;
            for (Row row : sheet) {
                rowCount++;
                if (rowCount == 1) continue; // 跳过表头
                
                try {
                    RecordImportDTO dto = new RecordImportDTO();
                    
                    // 读取商品名称
                    String goodsName = getCellValueAsString(row.getCell(0));
                    // 根据商品名称查找商品ID
                    Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq("name", goodsName));
                    if (goods == null) {
                        errorMessages.add("第" + rowCount + "行: 商品 '" + goodsName + "' 不存在");
                        continue;
                    }
                    
                    dto.setGoods(goods.getId());
                    dto.setCount(getCellValueAsInteger(row.getCell(1)));
                    dto.setRemark(getCellValueAsString(row.getCell(2)));
                    
                    // 添加到导入列表
                    importList.add(dto);
                } catch (Exception e) {
                    errorMessages.add("第" + rowCount + "行数据格式有误: " + e.getMessage());
                }
            }
            
            workbook.close();
            inputStream.close();
            
            // 如果存在错误消息，返回错误
            if (!errorMessages.isEmpty()) {
                result.put("success", false);
                result.put("message", "导入过程中发现错误");
                result.put("errors", errorMessages);
                return result;
            }
            
            // 保存数据
            int successCount = 0;
            for (RecordImportDTO dto : importList) {
                // 获取商品信息
                Goods goods = goodsService.getById(dto.getGoods());
                if (goods == null) {
                    continue;
                }
                
                // 创建入库记录
                Record record = new Record();
                record.setGoods(dto.getGoods());
                record.setCount(dto.getCount());
       /*         record.setStorage(storageId != null ? storageId : goods.getStorage());
                record.setGoodstype(goodstypeId != null ? goodstypeId : goods.getGoodstype());*/
                record.setAction("1"); // 1代表入库
                record.setRemark(dto.getRemark());
                record.setCreatetime(new Date());
                // 设置当前用户ID (从SecurityContext或Session获取)
                /*record.setUserId(getCurrentUserId());
                record.setUserName(getCurrentUserName());*/
                
                // 保存记录
                boolean saved = recordService.save(record);
                if (saved) {
                    successCount++;
                    
                    // 更新商品库存
                    goods.setCount(goods.getCount() + dto.getCount());
                    goodsService.updateById(goods);
                }
            }
            
            // 添加导入历史记录
            ImportHistory history = new ImportHistory();
            history.setType("records");
            history.setFileName(file.getOriginalFilename());
            history.setTotalCount(importList.size());
            history.setSuccessCount(successCount);
            history.setFailCount(importList.size() - successCount);
            /*history.setUserId(getCurrentUserId());
            history.setUserName(getCurrentUserName());*/
            history.setCreateTime(new Date());
            if (!errorMessages.isEmpty()) {
                history.setErrorDetails(new ObjectMapper().writeValueAsString(errorMessages));
            }
            importHistoryService.save(history);
            
            // 返回成功结果
            result.put("success", true);
            result.put("message", "成功导入" + successCount + "条入库记录");
            result.put("total", importList.size());
            result.put("successCount", successCount);
            
            log.info("导入入库记录完成，成功: {}, 总数: {}", successCount, importList.size());
            
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "导入文件处理失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 防止数字变成科学计数法
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 获取单元格的整数值
     */
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("单元格值不是有效的数字: " + cell.getStringCellValue());
                }
            default:
                throw new IllegalArgumentException("单元格类型不支持转换为数字");
        }
    }

    // 获取Excel工作簿
    private Workbook getWorkbook(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        
        try {
            if (fileName != null && fileName.endsWith(".xlsx")) {
                return new XSSFWorkbook(inputStream);
            } else if (fileName != null && fileName.endsWith(".xls")) {
                return new HSSFWorkbook(inputStream);
            }
        } catch (Exception e) {
            log.error("解析Excel文件失败", e);
            throw e;
        }
        
        return null;
    }

    // 验证标题行
    private boolean validateHeaders(Row headerRow, String[] expectedHeaders) {
        // 检查标题数量
        if (headerRow.getPhysicalNumberOfCells() < expectedHeaders.length) {
            log.warn("标题列数不足. 预期: {}, 实际: {}", expectedHeaders.length, headerRow.getPhysicalNumberOfCells());
            return false;
        }
        
        // 创建实际标题映射
        Map<String, Integer> actualHeaderMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                actualHeaderMap.put(cell.getStringCellValue(), i);
            }
        }
        
        // 记录实际标题供调试
        log.info("实际Excel标题: {}", actualHeaderMap.keySet());
        
        // 定义列标题的可接受别名
        Map<String, List<String>> headerAliases = new HashMap<>();
        headerAliases.put("资产名称", Arrays.asList("资产名称", "物品名称", "名称", "商品名称"));
        headerAliases.put("资产类型", Arrays.asList("资产类型", "物品类型", "类型", "商品类型", "仓库ID", "仓库"));
        headerAliases.put("资产数量", Arrays.asList("资产数量", "物品数量", "数量", "商品数量"));
        headerAliases.put("资产描述", Arrays.asList("资产描述", "物品描述", "描述", "商品描述", "备注"));
        
        // 检查是否所有预期的标题都有对应的列
        for (String expectedHeader : expectedHeaders) {
            boolean found = false;
            List<String> aliases = headerAliases.getOrDefault(expectedHeader, Collections.singletonList(expectedHeader));
            
            for (String alias : aliases) {
                if (actualHeaderMap.containsKey(alias)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                log.warn("找不到标题: {} 或其别名: {}", expectedHeader, aliases);
                return false;
            }
        }
        
        return true;
    }

    // 解析行数据到DTO - 使用实体类中的字段名
    private GoodsImportDTO parseRowToDTO(Row row, Row headerRow) {
        GoodsImportDTO dto = new GoodsImportDTO();
        
        // 获取列索引
        Map<String, Integer> columnIndexMap = getColumnIndexMap(headerRow);
        
        // 资产名称 - 查找匹配的列
        Integer nameIndex = getColumnIndex(columnIndexMap, "资产名称", "物品名称", "名称", "商品名称");
        if (nameIndex != null && row.getCell(nameIndex) != null) {
            dto.setName(getCellStringValue(row.getCell(nameIndex)));
        }
        
        // 资产数量 - 查找匹配的列
        Integer countIndex = getColumnIndex(columnIndexMap, "资产数量", "物品数量", "数量", "商品数量");
        if (countIndex != null && row.getCell(countIndex) != null) {
            dto.setCount(getCellNumericValue(row.getCell(countIndex)));
        }
        
        // 资产备注 - 查找匹配的列
        Integer remarkIndex = getColumnIndex(columnIndexMap, "资产描述", "物品描述", "描述", "商品描述", "备注");
        if (remarkIndex != null && row.getCell(remarkIndex) != null) {
            dto.setRemark(getCellStringValue(row.getCell(remarkIndex)));
        }
        
        return dto;
    }

    // 获取标题行中各列的索引
    private Map<String, Integer> getColumnIndexMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                map.put(cell.getStringCellValue(), i);
            }
        }
        return map;
    }

    // 查找列索引，支持多个别名
    private Integer getColumnIndex(Map<String, Integer> columnMap, String... possibleNames) {
        for (String name : possibleNames) {
            if (columnMap.containsKey(name)) {
                return columnMap.get(name);
            }
        }
        return null;
    }

    // 获取单元格字符串值
    private String getCellStringValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int)cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else {
            return "";
        }
    }

    // 获取单元格数值
    private int getCellNumericValue(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int)cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    // 验证DTO数据
    private String validateGoodsDTO(GoodsImportDTO dto) {
        if (StringUtils.isEmpty(dto.getName())) {
            return "资产名称不能为空";
        }
        
        if (dto.getCount() == null || dto.getCount() <= 0) {
            return "资产数量必须大于0";
        }
        
        // 可以增加更多验证逻辑
        
        return null; // 返回null表示验证通过
    }

    // 保存导入的数据 - 匹配实体类字段名
    private int saveImportedGoods(List<GoodsImportDTO> goodsList, Integer storage, Integer goodstype) {
        int count = 0;
        
        for (GoodsImportDTO dto : goodsList) {
            try {
                log.info("尝试保存导入商品: {}, 仓库ID: {}, 分类ID: {}", 
                    dto.getName(), storage, goodstype);
                
                // 创建资产记录
                Goods goodsEntity = new Goods();
                goodsEntity.setName(dto.getName());
                goodsEntity.setGoodstype(goodstype);
                goodsEntity.setStorage(storage);
                goodsEntity.setCount(dto.getCount());
                goodsEntity.setRemark(dto.getRemark());
                
                // 保存资产记录
                goodsService.save(goodsEntity);
                count++;
                
                log.info("成功保存商品: {}, ID: {}", dto.getName(), goodsEntity.getId());
                
                // 记录导入历史
                recordImportHistory(goodsEntity, "导入");
                
            } catch (Exception e) {
                log.error("保存资产记录失败: {}", dto.getName(), e);
                // 不抛出异常，继续处理下一条记录
            }
        }
        
        return count;
    }

    // 记录导入历史
    private void recordImportHistory(Goods goods, String operation) {
        try {
            // 创建记录实体
            ImportHistory history = new ImportHistory();
            history.setId(goods.getId());
            history.setFileName(goods.getName());
            history.setTotalCount(goods.getCount());
            history.setUserName(userService.getName());
            history.setCreateTime(new Date());
            
            // 保存记录
            importHistoryService.save(history);
        } catch (Exception e) {
            log.error("记录导入历史失败", e);
            // 不中断主流程
        }
    }
}
