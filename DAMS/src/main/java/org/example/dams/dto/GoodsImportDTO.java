package org.example.dams.dto;

import lombok.Data;

/**
 * 商品导入数据传输对象 - 与实体对应
 */
@Data
public class GoodsImportDTO {
    
    /**
     * 资产名称
     */
    private String name;
    
    /**
     * 资产数量
     */
    private Integer count;
    
    /**
     * 资产备注
     */
    private String remark;
}
