package org.example.dams.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.dams.entity.Comment;
import org.example.dams.mapper.CommentMapper;
import org.example.dams.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Override
    public List<Comment> getCommentsByGoodsId(Integer goodsId) {
        return baseMapper.getCommentsByGoodsId(goodsId);
    }

    @Override
    public Map<String, Object> getCommentStats(Integer goodsId) {
        return baseMapper.getCommentStats(goodsId);
    }

    @Override
    @Transactional
    public boolean addComment(Comment comment) {
        comment.setCreateTime(new Date());
        if (comment.getType() == null) {
            comment.setType("comment");
        }
        if (comment.getStatus() == null) {
            comment.setStatus("pending");
        }
        return save(comment);
    }

    @Override
    @Transactional
    public boolean updateCommentStatus(Integer commentId, String status) {
        try {
            log.info("Updating comment status: commentId={}, status={}", commentId, status);
            
            // 使用 UpdateWrapper 来更新状态
            UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", commentId)
                        .set("status", status);
            
            boolean success = this.update(updateWrapper);
            log.info("Status update result: {}", success);
            return success;
            
        } catch (Exception e) {
            log.error("Error updating comment status", e);
            throw e;
        }
    }
}





