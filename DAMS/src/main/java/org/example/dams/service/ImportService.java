package org.example.dams.service;

import org.example.dams.dto.GoodsImportDTO;
import org.example.dams.dto.RecordImportDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 导入服务接口
 */
public interface ImportService {
    /**
     * 导入商品数据
     * @param file Excel文件
     * @param storage 仓库ID
     * @param goodstype 商品类型ID
     * @return 导入结果
     */
    Map<String, Object> importGoods(MultipartFile file, Integer storage, Integer goodstype);
    
    /**
     * 导入入库记录
     */
    Map<String, Object> importRecords(MultipartFile file, Integer storageId, Integer goodstypeId);
}
