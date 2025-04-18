package org.example.dams.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dams.entity.Goodstype;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.dams.entity.Storage;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
@Mapper
public interface GoodstypeMapper extends BaseMapper<Goodstype> {
    IPage pageCC(IPage<Goodstype> page, @Param(Constants.WRAPPER) Wrapper wrapper);
}
