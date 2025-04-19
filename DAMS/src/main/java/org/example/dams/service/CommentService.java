package org.example.dams.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dams.entity.Comment;
import java.util.List;
import java.util.Map;

public interface CommentService extends IService<Comment> {
    List<Comment> getCommentsByGoodsId(Integer goodsId);
    Map<String, Object> getCommentStats(Integer goodsId);
    boolean addComment(Comment comment);
    boolean updateCommentStatus(Integer commentId, String status);
}



