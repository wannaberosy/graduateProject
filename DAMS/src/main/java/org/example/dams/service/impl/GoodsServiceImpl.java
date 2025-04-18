package org.example.dams.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Goods;
import org.example.dams.entity.Storage;
import org.example.dams.mapper.GoodsMapper;
import org.example.dams.mapper.StorageMapper;
import org.example.dams.service.GoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public IPage pageCC(IPage<Goods> page, Wrapper wrapper) {
        return goodsMapper.pageCC(page,wrapper);
    }

    @Override
    public List<Goods> getWarningGoods() {
        // 创建查询条件
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<Goods>();
        // 假设预警阈值为20
        queryWrapper.lt("count", 20)  // count小于20的商品
                   .orderByAsc("count")  // 按库存量升序排序
                   .last("LIMIT 10");    // 限制返回10条记录
        
        return goodsMapper.selectWarningGoods(queryWrapper);
    }
    
    /**
     * 获取商品分类分布统计
     */
    @Override
    public List<Map<String, Object>> getTypeDistribution() {
        return goodsMapper.selectTypeDistribution();
    }
}
