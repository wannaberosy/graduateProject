package org.example.dams.dto;

import lombok.Data;

/**
 * 入库记录导入DTO
 */
@Data
public class RecordImportDTO {
    private Integer goods;      // 商品ID
    private Integer count;      // 数量
    private String remark;      // 备注
}
