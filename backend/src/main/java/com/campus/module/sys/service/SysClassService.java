package com.campus.module.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysClass;

import java.util.List;

public interface SysClassService {
    Page<SysClass> page(int page, int size);

    SysClass getById(Long id);

    List<SysClass> listAll();

    void save(SysClass c);

    void update(Long id, SysClass c);

    void delete(Long id);
}