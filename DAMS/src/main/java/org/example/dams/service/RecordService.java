package org.example.dams.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dams.entity.Storage;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zqy
 * @since 2025-04-13
 */
public interface RecordService extends IService<Record> {
    IPage pageCC(IPage<Record> page, Wrapper wrapper);
    Map<String, Object> getTodayInOutCount();
    List<Map<String, Object>> getRecentRecords();
    Map<String, Object> getInOutTrend();
}
