package org.example.dams.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.dams.entity.Record;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.dams.entity.Storage;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zqy
 * @since 2025-04-13
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {
    IPage pageCC(IPage<Record> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    @Select("SELECT r.*, " +
            "g.name as goodsname, " +
            "u.name as username " +
            "FROM record r " +
            "LEFT JOIN goods g ON r.goods = g.id " +
            "LEFT JOIN user u ON r.userid = u.id " +
            "${ew.customSqlSegment}")
    List<Record> selectRecentRecords(@Param("ew") QueryWrapper<Record> queryWrapper);

    @Select("SELECT " +
            "SUM(CASE WHEN action = '1' THEN count ELSE 0 END) as inCount, " +
            "SUM(CASE WHEN action = '2' THEN count ELSE 0 END) as outCount " +
            "FROM record " +
            "WHERE createtime >= #{startTime}")
    Map<String, Object> getTodayStats(@Param("startTime") Date startTime);

    @Select("SELECT " +
            "r.id, " +
            "r.action, " +
            "r.count, " +
            "r.createtime, " +
            "g.name as goodsname, " +
            "u.name as username " +
            "FROM record r " +
            "LEFT JOIN goods g ON r.goods = g.id " +
            "LEFT JOIN user u ON r.userid = u.id " +
            "ORDER BY r.createtime DESC " +
            "LIMIT 10")
    List<Map<String, Object>> getRecentActivities();

    @Select("SELECT " +
            "DATE(createtime) as date, " +
            "SUM(CASE WHEN action = '1' THEN count ELSE 0 END) as inCount, " +
            "SUM(CASE WHEN action = '2' THEN count ELSE 0 END) as outCount " +
            "FROM record " +
            "WHERE createtime >= #{startTime} " +
            "GROUP BY DATE(createtime) " +
            "ORDER BY date")
    List<Map<String, Object>> getInOutTrend(@Param("startTime") Date startTime);

    @Select("SELECT " +
            "t.name as category, " +
            "COUNT(*) as count " +
            "FROM goods g " +
            "LEFT JOIN goodsType t ON g.goodsType = t.id " +
            "GROUP BY g.goodsType, t.name " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getGoodsTypeDistribution();
}
