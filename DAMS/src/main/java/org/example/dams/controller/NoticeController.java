package org.example.dams.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.dams.common.QueryPageParam;
import org.example.dams.common.Result;
import org.example.dams.entity.Notice;
import org.example.dams.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zqy
 * @since 2025-04-16
 */
@Slf4j
@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    //删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        try {
            log.info("接收到删除通知请求，ID：{}", id);
            boolean success = noticeService.removeById(id);
            if (success) {
                log.info("删除通知成功，ID：{}", id);
                return Result.success();
            } else {
                return Result.fail();
            }
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return Result.fail();
        }
    }
    //新增
    @PostMapping("/save")
    public Result save(@RequestBody Notice notice){
        try {
            log.info("接收到新增通知请求：{}", notice);
            if (notice.getTitle()== null || notice.getTitle().trim().isEmpty()) {
                return Result.fail();
            }
            
            notice.setTime(new Date());
            boolean success = noticeService.save(notice);
            if (success) {
                log.info("保存通知成功，ID：{}", notice.getId());
                return Result.success(notice);
            } else {
                return Result.fail();
            }
        } catch (Exception e) {
            log.error("保存通知失败", e);
            return Result.fail();
        }
    }

    //更新
    @PostMapping("/update")
    public Result update(@RequestBody Notice notice){
        try {
            log.info("接收到更新通知请求：{}", notice);
            if (notice.getId() == null) {
                return Result.fail();
            }
            
            notice.setTime(new Date());
            boolean success = noticeService.updateById(notice);
            if (success) {
                log.info("更新通知成功，ID：{}", notice.getId());
                return Result.success(notice);
            } else {
                return Result.fail();
            }
        } catch (Exception e) {
            log.error("更新通知失败", e);
            return Result.fail();
        }
    }

    @GetMapping("/findByNotice")
    public Result findByNo(@RequestParam String notice){
        List list = noticeService.lambdaQuery().eq(Notice::getContent, notice).list();
        return list.size()>0?Result.success(list):Result.fail();
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String content = (String)param.get("content");

        //System.out.println("name==="+(String)param.get("name"));


        Page<Notice> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Notice> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(content)&& !"null".equals(content)){
            lambdaQueryWrapper.like(Notice::getContent,content);
        }



        IPage result = noticeService.pageCC(page,lambdaQueryWrapper);

        return Result.success(result.getRecords(),result.getTotal());
    }

    @GetMapping("/list")
    public Result list(){
        try {
            List<Notice> notices = noticeService.list();
            log.info("获取通知列表成功，数量：{}", notices.size());
            return Result.success(notices);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.fail();
        }
    }

    @GetMapping("/count")
    public Result count() {
        int count = noticeService.count();
        return Result.success(count);
    }
}
