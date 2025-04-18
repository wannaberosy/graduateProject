package org.example.dams.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.dams.entity.Storage;
import org.example.dams.entity.User;
import org.example.dams.mapper.StorageMapper;
import org.example.dams.service.StorageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zqy
 * @since 2025-04-11
 */
@Service
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService {
   @Resource
   private StorageMapper storageMapper;

   @Override
   public IPage pageCC(IPage<Storage> page, Wrapper wrapper) {
       return storageMapper.pageCC(page,wrapper);
   }
}
