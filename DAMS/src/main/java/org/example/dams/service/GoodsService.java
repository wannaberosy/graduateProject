package org.example.dams.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Goods;
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
 * @since 2025-04-11
 */
public interface GoodsService extends IService<Goods> {
    IPage pageCC(IPage<Goods> page, Wrapper wrapper);
    List<Goods> getWarningGoods();
    List<Map<String, Object>> getTypeDistribution();
}

