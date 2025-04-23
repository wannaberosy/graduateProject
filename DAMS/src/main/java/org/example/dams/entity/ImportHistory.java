package org.example.dams.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("import_history")
public class ImportHistory {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String type;          // 导入类型: goods, records
    private String fileName;      // 文件名
    private Integer totalCount;   // 总记录数
    private Integer successCount; // 成功记录数
    private Integer failCount;    // 失败记录数
    private Integer userId;       // 操作用户ID
    private String userName;      // 操作用户名称
    private Date createTime;      // 创建时间
    private String errorDetails;  // 错误详情 (JSON格式)
}
