package org.example.dams.mapper;

import org.example.dams.entity.ImportHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImportHistoryMapper extends BaseMapper<ImportHistory> {
    // 基本方法由 BaseMapper 提供
}
