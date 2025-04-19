package org.example.dams.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dams.common.Result;
import org.example.dams.entity.Comment;
import org.example.dams.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/comment")
@Slf4j

public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/list/{goodsId}")
    public Result getComments(@PathVariable Integer goodsId) {
        try {
            log.info("Fetching comments for goodsId: {}", goodsId);
            List<Comment> comments = commentService.getCommentsByGoodsId(goodsId);
            
            if (comments == null) {
                log.warn("No comments found for goodsId: {}", goodsId);
                return Result.success(new ArrayList<>());
            }
            
            log.info("Found {} comments for goodsId: {}", comments.size(), goodsId);
            return Result.success(comments);
        } catch (Exception e) {
            log.error("Error fetching comments for goodsId: " + goodsId, e);
            return Result.fail();
        }
    }

    @GetMapping("/stats/{goodsId}")
    public Result getCommentStats(@PathVariable Integer goodsId) {
        try {
            log.info("Fetching comment stats for goodsId: {}", goodsId);
            Map<String, Object> stats = commentService.getCommentStats(goodsId);
            
            if (stats == null) {
                log.warn("No stats found for goodsId: {}", goodsId);
                return Result.success(new HashMap<>());
            }
            
            log.info("Found stats for goodsId: {}", goodsId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("Error fetching comment stats for goodsId: " + goodsId, e);
            return Result.fail();
        }
    }

    @PostMapping("/add")
    public Result addComment(@RequestBody Comment comment) {
        try {
            log.info("Adding new comment: {}", comment);
            if (comment.getGoodsId() == null || comment.getUserId() == null) {
                return Result.fail();
            }
            
            boolean success = commentService.addComment(comment);
            if (success) {
                log.info("Successfully added comment for goodsId: {}", comment.getGoodsId());
                return Result.success();
            } else {
                log.warn("Failed to add comment: {}", comment);
                return Result.fail();
            }
        } catch (Exception e) {
            log.error("Error adding comment: " + comment, e);
            return Result.fail();
        }
    }

    @PutMapping("/status/{commentId}")
    public Result updateStatus(@PathVariable Integer commentId, @RequestParam String status) {
        try {
            log.info("Updating comment status: commentId={}, status={}", commentId, status);
            
            // 验证评论是否存在
            Comment comment = commentService.getById(commentId);
            if (comment == null) {
                log.warn("Comment not found: {}", commentId);
                return Result.fail();
            }
            
            // 验证状态值
            if (!Arrays.asList("pending", "resolved", "rejected").contains(status)) {
                log.warn("Invalid status: {}", status);
                return Result.fail();
            }
            
            // 更新状态
            comment.setStatus(status);
            boolean success = commentService.updateById(comment);
            
            if (success) {
                log.info("Successfully updated comment status: commentId={}, status={}", commentId, status);
                return Result.success();
            } else {
                log.warn("Failed to update comment status: commentId={}, status={}", commentId, status);
                return Result.fail();
            }
        } catch (Exception e) {
            log.error("Error updating comment status: commentId=" + commentId, e);
            return Result.fail();
        }
    }

    @PostMapping("/status/{commentId}")
    public Result updateStatusPost(@PathVariable Integer commentId, @RequestParam String status) {
        try {
            log.info("Updating comment status: commentId={}, status={}", commentId, status);
            
            // 验证状态值
            if (!Arrays.asList("pending", "resolved", "rejected").contains(status)) {
                log.warn("Invalid status: {}", status);
                return Result.fail();
            }
            
            boolean success = commentService.updateCommentStatus(commentId, status);
            
            if (success) {
                log.info("Successfully updated comment status");
                return Result.success();
            } else {
                log.warn("Failed to update comment status");
                return Result.fail();
            }
        } catch (Exception e) {
            log.error("Error updating comment status", e);
            return Result.fail();
        }
    }
}



