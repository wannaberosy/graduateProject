package org.example.dams.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Storage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
public interface StorageService extends IService<Storage> {
    IPage pageCC(IPage<Storage> page, Wrapper wrapper);
}
