package com.campus.module.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.module.edu.entity.Semester;
import com.campus.module.edu.mapper.SemesterMapper;
import com.campus.module.edu.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {
    private final SemesterMapper semesterMapper;

    @Override
    public List<Semester> getActiveList() {
        return semesterMapper.selectList(new LambdaQueryWrapper<Semester>()
                .eq(Semester::getStatus, 1)
                .orderByDesc(Semester::getKsrq));
    }
}