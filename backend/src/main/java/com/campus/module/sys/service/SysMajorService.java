package com.campus.module.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysMajor;
import java.util.List;

public interface SysMajorService {
    Page<SysMajor> page(int page, int size, String keyword);
    List<SysMajor> listAll(Long deptId);
    SysMajor getById(Long id);
    void save(SysMajor major);
    void update(SysMajor major);
    void delete(Long id);
}