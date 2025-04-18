package org.example.dams.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dams.entity.Storage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.dams.entity.User;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
@Mapper
public interface StorageMapper extends BaseMapper<Storage> {
    IPage pageCC(IPage<Storage> page, @Param(Constants.WRAPPER) Wrapper wrapper);
}
