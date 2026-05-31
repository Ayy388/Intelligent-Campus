package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysGrade;
import com.campus.module.sys.mapper.SysGradeMapper;
import com.campus.module.sys.service.SysGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysGradeServiceImpl implements SysGradeService {
    private final SysGradeMapper mapper;

    @Override
    public Page<SysGrade> page(int page, int size) {
        return mapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<SysGrade>().orderByDesc(SysGrade::getYear));
    }

    @Override
    public List<SysGrade> listAll() {
        return mapper.selectList(
            new LambdaQueryWrapper<SysGrade>().orderByDesc(SysGrade::getYear));
    }

    @Override
    public SysGrade getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void save(SysGrade grade) {
        mapper.insert(grade);
    }

    @Override
    public void update(SysGrade grade) {
        mapper.updateById(grade);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}