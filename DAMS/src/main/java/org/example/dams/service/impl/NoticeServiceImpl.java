package org.example.dams.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Notice;
import org.example.dams.entity.Storage;
import org.example.dams.mapper.NoticeMapper;
import org.example.dams.mapper.StorageMapper;
import org.example.dams.service.NoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zqy
 * @since 2025-04-16
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
    @Resource
    private NoticeMapper noticeMapper;

    @Override
    public IPage pageCC(IPage<Notice> page, Wrapper wrapper) {
        return noticeMapper.pageCC(page,wrapper);
    }
}
