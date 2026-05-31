package com.campus.module.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysGrade;
import java.util.List;

public interface SysGradeService {
    Page<SysGrade> page(int page, int size);
    List<SysGrade> listAll();
    SysGrade getById(Long id);
    void save(SysGrade grade);
    void update(SysGrade grade);
    void delete(Long id);
}