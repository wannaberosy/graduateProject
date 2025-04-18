package org.example.dams.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Goodstype;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dams.entity.Storage;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
public interface GoodstypeService extends IService<Goodstype> {
    IPage pageCC(IPage<Goodstype> page, Wrapper wrapper);
}
