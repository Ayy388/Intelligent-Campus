package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.mapper.SysMajorMapper;
import com.campus.module.sys.service.SysMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysMajorServiceImpl implements SysMajorService {
    private final SysMajorMapper mapper;

    @Override
    public Page<SysMajor> page(int page, int size, String keyword) {
        LambdaQueryWrapper<SysMajor> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty())
            w.like(SysMajor::getName, keyword);
        w.orderByAsc(SysMajor::getId);
        return mapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public List<SysMajor> listAll(Long deptId) {
        LambdaQueryWrapper<SysMajor> w = new LambdaQueryWrapper<>();
        if (deptId != null) w.eq(SysMajor::getDepartmentId, deptId);
        w.orderByAsc(SysMajor::getId);
        return mapper.selectList(w);
    }

    @Override
    public SysMajor getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void save(SysMajor major) {
        mapper.insert(major);
    }

    @Override
    public void update(SysMajor major) {
        mapper.updateById(major);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}