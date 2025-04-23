package org.example.dams.controller;

import org.example.dams.common.Result;
import org.example.dams.entity.ImportHistory;
import org.example.dams.service.ImportHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

@RestController
@RequestMapping("/import/history")
public class ImportHistoryController {

    @Autowired
    private ImportHistoryService importHistoryService;
    
    @PostMapping("/list")
    public Result list(@RequestBody Map<String, Object> params) {
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        
        Page<ImportHistory> page = new Page<>(pageNum, pageSize);
        IPage<ImportHistory> result = importHistoryService.page(page, 
                new QueryWrapper<ImportHistory>().orderByDesc("create_time"));
        
        return Result.success(result.getRecords(), result.getTotal());
    }
}

