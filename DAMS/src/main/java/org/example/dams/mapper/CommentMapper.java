package org.example.dams.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select({
        "SELECT c.*, u.name as user_name, g.name as goods_name",
        "FROM comment c",
        "LEFT JOIN user u ON c.user_id = u.id",
        "LEFT JOIN goods g ON c.goods_id = g.id",
        "WHERE c.goods_id = #{goodsId}",
        "ORDER BY c.create_time DESC"
    })
    List<Comment> getCommentsByGoodsId(@Param("goodsId") Integer goodsId);

    @Select({
        "SELECT COALESCE(AVG(rating), 0) as avgRating,",
        "COUNT(*) as totalCount,",
        "COUNT(CASE WHEN rating = 5 THEN 1 END) as fiveStarCount,",
        "COUNT(CASE WHEN rating = 4 THEN 1 END) as fourStarCount,",
        "COUNT(CASE WHEN rating = 3 THEN 1 END) as threeStarCount,",
        "COUNT(CASE WHEN rating = 2 THEN 1 END) as twoStarCount,",
        "COUNT(CASE WHEN rating = 1 THEN 1 END) as oneStarCount",
        "FROM comment",
        "WHERE goods_id = #{goodsId} AND type = 'comment'"
    })
    Map<String, Object> getCommentStats(@Param("goodsId") Integer goodsId);
}





