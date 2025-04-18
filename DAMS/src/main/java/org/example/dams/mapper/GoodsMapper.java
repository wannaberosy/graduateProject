package org.example.dams.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.dams.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.dams.entity.Goodstype;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    IPage pageCC(IPage<Goods> page, @Param(Constants.WRAPPER) Wrapper wrapper);

     @Select("SELECT g.*, s.name as storage_name, gt.name as type_name " +
            "FROM goods g " +
            "LEFT JOIN storage s ON g.storage = s.id " +
            "LEFT JOIN goodstype gt ON g.goodstype = gt.id " +
            "${ew.customSqlSegment}")
     List<Goods> selectWarningGoods(@Param("ew") QueryWrapper<Goods> queryWrapper);
    
    /**
     * 查询商品分类分布
     */
    @Select("SELECT gt.name, COUNT(g.id) as count, SUM(g.count) as total_count " +
            "FROM goodstype gt " +
            "LEFT JOIN goods g ON gt.id = g.goodstype " +
            "GROUP BY gt.id, gt.name")
    List<Map<String, Object>> selectTypeDistribution();

    @Select({
        "SELECT",
        "  (SELECT COUNT(*) FROM goods) as totalAssets,",
        "  (SELECT COUNT(DISTINCT r.goods) FROM record r",
        "   WHERE DATE_FORMAT(r.createtime,'%Y%m') = DATE_FORMAT(NOW(),'%Y%m')) as newAssets,",
        "  (SELECT COUNT(DISTINCT r.goods) FROM record r",
        "   WHERE r.createtime >= DATE_SUB(NOW(), INTERVAL 30 DAY)) as activeAssets,",
        "  (SELECT COUNT(DISTINCT r.userid) FROM record r",
        "   WHERE r.createtime >= DATE_SUB(NOW(), INTERVAL 30 DAY)) as activeUsers,",
        "  COALESCE((SELECT",
        "     ROUND(((curr_month - prev_month) / NULLIF(prev_month, 0)) * 100, 2)",
        "   FROM (",
        "     SELECT",
        "       COUNT(DISTINCT r1.goods) as curr_month,",
        "       (SELECT COUNT(DISTINCT r2.goods) FROM record r2",
        "        WHERE DATE_FORMAT(r2.createtime,'%Y%m') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH),'%Y%m')",
        "       ) as prev_month",
        "     FROM record r1",
        "     WHERE DATE_FORMAT(r1.createtime,'%Y%m') = DATE_FORMAT(NOW(),'%Y%m')",
        "   ) t), 0) as assetGrowth"
    })
    Map<String, Object> getStatistics();

    @Select("SELECT " +
            "t.name as name, " +
            "COUNT(*) as value " +
            "FROM goods g " +
            "LEFT JOIN goodsType t ON g.goodsType = t.id " +
            "GROUP BY g.goodsType, t.name")
    List<Map<String, Object>> getTypeDistribution();

    @Select({
        "<script>",
        "WITH RECURSIVE DateRange AS (",
        "  SELECT CURDATE() - INTERVAL",
        "    CASE #{timeRange}",
        "      WHEN 'week' THEN 7",
        "      WHEN 'month' THEN 30",
        "      ELSE 365",
        "    END DAY AS date",
        "  UNION ALL",
        "  SELECT date + INTERVAL 1 DAY",
        "  FROM DateRange",
        "  WHERE date &lt; CURDATE()",
        "),",
        "DailyStats AS (",
        "  SELECT DATE(r.createtime) as date, COUNT(DISTINCT r.goods) as count",
        "  FROM record r",
        "  WHERE r.createtime >= CURDATE() - INTERVAL",
        "    CASE #{timeRange}",
        "      WHEN 'week' THEN 7",
        "      WHEN 'month' THEN 30",
        "      ELSE 365",
        "    END DAY",
        "  GROUP BY DATE(r.createtime)",
        ")",
        "SELECT",
        "  DATE_FORMAT(d.date, '%Y-%m-%d') as date,",
        "  COALESCE(s.count, 0) as count",
        "FROM DateRange d",
        "LEFT JOIN DailyStats s ON d.date = s.date",
        "ORDER BY d.date",
        "</script>"
    })
    List<Map<String, Object>> getTrend(@Param("timeRange") String timeRange);

    @Select({
        "<script>",
        "SELECT",
        "  DATE_FORMAT(r.createtime, '%Y-%m-%d') as date,",
        "  g.name as goodsName,",
        "  COUNT(*) as useCount",
        "FROM record r",
        "JOIN goods g ON r.goods = g.id",
        "WHERE r.createtime >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)",
        "<if test='goodsType != null and goodsType != \"\"'>",
        "  AND g.goodsType = #{goodsType}",
        "</if>",
        "GROUP BY DATE(r.createtime), g.id",
        "ORDER BY date, useCount DESC",
        "</script>"
    })
    List<Map<String, Object>> getHeatmapData(@Param("goodsType") String goodsType);

    @Select({
        "SELECT g.goodsType as type, COUNT(*) as count, t.name as typeName",
        "FROM goods g",
        "LEFT JOIN goodstype t ON g.goodsType = t.id",
        "GROUP BY g.goodsType, t.name"
    })
    List<Map<String, Object>> getAssetTypeDistribution();
}
