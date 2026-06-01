package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.mapper.SysClassMapper;
import com.campus.module.sys.mapper.SysDepartmentMapper;
import com.campus.module.sys.mapper.SysMajorMapper;
import com.campus.module.sys.mapper.SysGradeMapper;
import com.campus.module.sys.mapper.SysUserMapper;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.service.SysClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysClassServiceImpl implements SysClassService {
    private final SysClassMapper sysClassMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysMajorMapper majorMapper;
    private final SysGradeMapper gradeMapper;
    private final SysUserMapper userMapper;

    @Override
    public Page<SysClass> page(int page, int size) {
        Page<SysClass> result = sysClassMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysClass>().orderByDesc(SysClass::getId));
        for (SysClass c : result.getRecords()) {
            if (c.getDepartmentId() != null) {
                var d = departmentMapper.selectById(c.getDepartmentId());
                if (d != null) c.setDepartmentName(d.getName());
            }
            if (c.getMajorId() != null) {
                var m = majorMapper.selectById(c.getMajorId());
                if (m != null) c.setMajorName(m.getName());
            }
            if (c.getGradeId() != null) {
                var g = gradeMapper.selectById(c.getGradeId());
                if (g != null) c.setGradeName(g.getName());
            }
            if (c.getCounselorId() != null) {
                SysUser cu = userMapper.selectById(c.getCounselorId());
                if (cu != null) c.setCounselorName(cu.getRealName());
            }
        }
        return result;
    }

    @Override
    public SysClass getById(Long id) {
        return sysClassMapper.selectById(id);
    }

    @Override
    public List<SysClass> listAll() {
        return sysClassMapper.selectList(null);
    }

    @Override
    public void save(SysClass c) {
        sysClassMapper.insert(c);
    }

    @Override
    public void update(Long id, SysClass c) {
        c.setId(id);
        sysClassMapper.updateById(c);
    }

    @Override
    public void delete(Long id) {
        sysClassMapper.deleteById(id);
    }
}