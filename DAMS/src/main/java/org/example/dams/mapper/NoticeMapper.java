package org.example.dams.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dams.entity.Notice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.dams.entity.Storage;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zqy
 * @since 2025-04-16
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
    IPage pageCC(IPage<Notice> page, @Param(Constants.WRAPPER) Wrapper wrapper);
}
