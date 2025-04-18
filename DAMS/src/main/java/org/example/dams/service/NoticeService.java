package org.example.dams.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dams.entity.Storage;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zqy
 * @since 2025-04-16
 */
public interface NoticeService extends IService<Notice> {
    IPage pageCC(IPage<Notice> page, Wrapper wrapper);
}
