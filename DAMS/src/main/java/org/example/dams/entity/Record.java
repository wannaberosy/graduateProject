package org.example.dams.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author zqy
 * @since 2025-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Record对象", description="")
@TableName("record")
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "数据资产id")
    private Integer goods;

    @ApiModelProperty(value = "商品名称")
    @TableField(exist = false)
    private String goodsname;

    @ApiModelProperty(value = "操作人")
    @TableField("userId")
    private Integer userid;

    @ApiModelProperty(value = "用户名称")
    @TableField(exist = false)
    private String username;

    @ApiModelProperty(value = "数量")
    private Integer count;

    @ApiModelProperty(value = "操作时间")
    private Date createtime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "操作类型：1-入库，2-出库")
    @TableField("action")
    private String action;

}
