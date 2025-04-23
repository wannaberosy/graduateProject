package org.example.dams.service.impl;

import org.example.dams.entity.ImportHistory;
import org.example.dams.mapper.ImportHistoryMapper;
import org.example.dams.service.ImportHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ImportHistoryServiceImpl extends ServiceImpl<ImportHistoryMapper, ImportHistory> implements ImportHistoryService {
    // 默认实现由 ServiceImpl 提供
}
