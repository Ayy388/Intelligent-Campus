package com.campus.module.edu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.edu.entity.Semester;
import java.util.List;

public interface SemesterService extends IService<Semester> {
    List<Semester> getActiveList();
}