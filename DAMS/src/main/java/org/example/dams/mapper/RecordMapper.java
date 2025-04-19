package org.example.dams.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @Select("SELECT a.*, " +
            "b.name as goodsname, " +
            "c.name as username " +
            "FROM record a " +
            "LEFT JOIN goods b ON a.goods = b.id " +
            "LEFT JOIN user c ON a.userId = c.id " +
            "WHERE 1=1 " +
            "<if test=\"param.name != null and param.name != ''\"> " +
            "AND b.name like concat('%', #{param.name}, '%') " +
            "</if> " +
            "<if test=\"param.storage != null and param.storage != ''\"> " +
            "AND a.storage = #{param.storage} " +
            "</if> " +
            "<if test=\"param.goodstype != null and param.goodstype != ''\"> " +
            "AND a.goodstype = #{param.goodstype} " +
            "</if> " +
            "<if test=\"param.roleId == '2'\"> " +
            "AND a.userId = #{param.userId} " +
            "</if> " +
            "ORDER BY a.createtime DESC")
    List<Record> listPage(Page<Record> page, @Param("param") Record param);
}
