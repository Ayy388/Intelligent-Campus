package com.campus.module.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysDepartment;
import java.util.List;

public interface SysDepartmentService {
    Page<SysDepartment> page(int page, int size, String keyword);
    List<SysDepartment> listAll();
    SysDepartment getById(Long id);
    void save(SysDepartment dept);
    void update(SysDepartment dept);
    void delete(Long id);
}