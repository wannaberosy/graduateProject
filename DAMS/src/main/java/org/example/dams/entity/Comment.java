package org.example.dams.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer goodsId;
    private Integer userId;
    private String content;
    private Integer rating;  // 1-5星评分
    private String type;    // 'comment'评论 or 'feedback'反馈
    private String status;  // 'pending'待处理, 'resolved'已解决, 'rejected'已拒绝
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    // 标记为非数据库字段
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String goodsName;
}



