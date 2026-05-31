package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.sys.entity.SysDepartment;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.mapper.SysDepartmentMapper;
import com.campus.module.sys.mapper.SysMajorMapper;
import com.campus.module.sys.service.SysDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDepartmentServiceImpl implements SysDepartmentService {
    private final SysDepartmentMapper mapper;
    private final SysMajorMapper majorMapper;

    @Override
    public Page<SysDepartment> page(int page, int size, String keyword) {
        LambdaQueryWrapper<SysDepartment> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty())
            w.like(SysDepartment::getName, keyword);
        w.orderByAsc(SysDepartment::getSortOrder);
        return mapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public List<SysDepartment> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<SysDepartment>()
            .orderByAsc(SysDepartment::getSortOrder));
    }

    @Override
    public SysDepartment getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void save(SysDepartment dept) {
        mapper.insert(dept);
    }

    @Override
    public void update(SysDepartment dept) {
        mapper.updateById(dept);
    }

    @Override
    public void delete(Long id) {
        Long count = majorMapper.selectCount(
            new LambdaQueryWrapper<SysMajor>()
                .eq(SysMajor::getDepartmentId, id));
        if (count > 0) {
            throw new BusinessException("该院系下有 " + count + " 个专业，请先删除专业");
        }
        mapper.deleteById(id);
    }
}