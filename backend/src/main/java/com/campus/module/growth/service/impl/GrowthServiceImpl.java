package com.campus.module.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.growth.entity.*;
import com.campus.module.growth.mapper.*;
import com.campus.module.growth.service.GrowthService;
import com.campus.module.sys.mapper.SysUserMapper;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.mapper.CourseMapper;
import com.campus.module.edu.mapper.CourseSelectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrowthServiceImpl implements GrowthService {
    private final StudentProfileMapper profileMapper;
    private final CheckInMapper checkinMapper;
    private final CheckInRecordMapper recordMapper;
    private final SysUserMapper userMapper;
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper selMapper;

    @Override
    public StudentProfile getProfile(Long studentId) {
        return profileMapper.selectOne(new LambdaQueryWrapper<StudentProfile>()
            .eq(StudentProfile::getStudentId, studentId));
    }

    @Override
    public void saveOrUpdateProfile(StudentProfile p) {
        StudentProfile existing = profileMapper.selectOne(
            new LambdaQueryWrapper<StudentProfile>().eq(StudentProfile::getStudentId, p.getStudentId()));
        if (existing != null) {
            p.setId(existing.getId());
            p.setEvaluation(existing.getEvaluation());
            p.setUpdateTime(LocalDateTime.now());
            profileMapper.updateById(p);
        } else {
            p.setUpdateTime(LocalDateTime.now());
            profileMapper.insert(p);
        }
    }

    @Override
    public void addEvaluation(Long studentId, String content, Long teacherId) {
        StudentProfile profile = getProfile(studentId);
        if (profile == null) {
            profile = new StudentProfile();
            profile.setStudentId(studentId);
            profile.setEvaluation(content);
            profileMapper.insert(profile);
        } else {
            String existing = profile.getEvaluation() != null ? profile.getEvaluation() + "\n" : "";
            profile.setEvaluation(existing + content);
            profileMapper.updateById(profile);
        }
    }

    @Override
    public Page<CheckIn> pageCheckIns(Long teacherId, int page, int size) {
        LambdaQueryWrapper<CheckIn> w = new LambdaQueryWrapper<>();
        if (teacherId != null) w.eq(CheckIn::getTeacherId, teacherId);
        w.orderByDesc(CheckIn::getCreateTime);
        Page<CheckIn> result = checkinMapper.selectPage(new Page<>(page, size), w);
        for (CheckIn c : result.getRecords()) {
            if (c.getTeacherId() != null) {
                com.campus.module.sys.entity.SysUser teacher = userMapper.selectById(c.getTeacherId());
                if (teacher != null) c.setTeacherName(teacher.getRealName());
            }
            if (c.getCourseId() != null) {
                Course course = courseMapper.selectById(c.getCourseId());
                if (course != null) c.setCourseName(course.getCourseName());
            }
        }
        return result;
    }

    @Override
    public Page<CheckIn> getCheckInsForStudent(Long studentId, int page, int size) {
        SysUser student = userMapper.selectById(studentId);
        if (student == null || student.getClassId() == null)
            return new Page<>(page, size);
        LambdaQueryWrapper<CheckIn> w = new LambdaQueryWrapper<CheckIn>()
            .eq(CheckIn::getClassId, student.getClassId())
            .orderByDesc(CheckIn::getCreateTime);
        Page<CheckIn> result = checkinMapper.selectPage(new Page<>(page, size), w);
        for (CheckIn c : result.getRecords()) {
            if (c.getTeacherId() != null) {
                SysUser teacher = userMapper.selectById(c.getTeacherId());
                if (teacher != null) c.setTeacherName(teacher.getRealName());
            }
            if (c.getCourseId() != null) {
                Course course = courseMapper.selectById(c.getCourseId());
                if (course != null) c.setCourseName(course.getCourseName());
            }
        }
        return result;
    }

    @Override
    public CheckIn createCheckIn(CheckIn c) {
        c.setStatus(1);
        if (c.getTotalCount() == null) c.setTotalCount(0);
        if (c.getCheckedCount() == null) c.setCheckedCount(0);
        if (c.getCourseId() != null) {
            Course course = courseMapper.selectById(c.getCourseId());
            if (course != null) {
                c.setCourseName(course.getCourseName());
                LambdaQueryWrapper<CourseSelection> qw = new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, c.getCourseId())
                        .eq(CourseSelection::getStatus, 1);
                if (c.getClassId() != null) {
                    List<SysUser> students = userMapper.selectList(
                        new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getClassId, c.getClassId()));
                    if (!students.isEmpty()) {
                        qw.in(CourseSelection::getStudentId,
                            students.stream().map(SysUser::getId).collect(Collectors.toList()));
                    }
                }
                Long enrolled = selMapper.selectCount(qw);
                c.setTotalCount(enrolled.intValue());
            }
        }
        checkinMapper.insert(c);
        return c;
    }

    @Override
    @Transactional
    public CheckInRecord doCheckIn(Long checkinId, Long studentId) {
        CheckIn checkin = checkinMapper.selectById(checkinId);
        if (checkin == null) throw new BusinessException("签到不存在");
        if (checkin.getStatus() == null) {
            checkin.setStatus(1);
            checkinMapper.updateById(checkin);
        }
        if (checkin.getStatus() != 1) throw new BusinessException("签到已结束");
        LocalDateTime now = LocalDateTime.now();
        if (checkin.getStartTime() != null && now.isBefore(checkin.getStartTime()))
            throw new BusinessException("签到尚未开始");
        if (checkin.getEndTime() != null && now.isAfter(checkin.getEndTime()))
            throw new BusinessException("签到已截止");

        if (checkin.getCourseId() != null) {
            Long enrolled = selMapper.selectCount(new LambdaQueryWrapper<CourseSelection>()
                    .eq(CourseSelection::getCourseId, checkin.getCourseId())
                    .eq(CourseSelection::getStudentId, studentId)
                    .eq(CourseSelection::getStatus, 1));
            if (enrolled == 0) throw new BusinessException("您未选该课程，无法签到");
        }

        Long cnt = recordMapper.selectCount(new LambdaQueryWrapper<CheckInRecord>()
            .eq(CheckInRecord::getCheckinId, checkinId).eq(CheckInRecord::getStudentId, studentId));
        if (cnt > 0) throw new BusinessException("已签到");

        CheckInRecord record = new CheckInRecord();
        record.setCheckinId(checkinId);
        record.setStudentId(studentId);
        record.setCheckinTime(LocalDateTime.now());
        recordMapper.insert(record);

        checkin.setCheckedCount((checkin.getCheckedCount() != null ? checkin.getCheckedCount() : 0) + 1);
        checkinMapper.updateById(checkin);
        return record;
    }

    @Override
    public List<CheckInRecord> getCheckInRecords(Long checkinId) {
        List<CheckInRecord> list = recordMapper.selectList(new LambdaQueryWrapper<CheckInRecord>()
            .eq(CheckInRecord::getCheckinId, checkinId));
        for (CheckInRecord r : list) {
            com.campus.module.sys.entity.SysUser student = userMapper.selectById(r.getStudentId());
            if (student != null) r.setStudentName(student.getRealName());
        }
        return list;
    }

    @Override
    public boolean getCheckInStatus(Long checkinId, Long studentId) {
        return recordMapper.selectCount(new LambdaQueryWrapper<CheckInRecord>()
            .eq(CheckInRecord::getCheckinId, checkinId)
            .eq(CheckInRecord::getStudentId, studentId)) > 0;
    }

    @Override
    public void closeCheckIn(Long id, Long teacherId) {
        CheckIn checkin = checkinMapper.selectById(id);
        if (checkin == null) throw new BusinessException("签到不存在");
        if (!checkin.getTeacherId().equals(teacherId)) throw new BusinessException("只能关闭自己发起的签到");
        if (checkin.getStatus() != 1) throw new BusinessException("签到已结束");
        checkin.setStatus(0);
        checkinMapper.updateById(checkin);
    }

    @Override
    public StudentProfile getProfileByStudentId(Long studentId) {
        return profileMapper.selectOne(
            new LambdaQueryWrapper<StudentProfile>().eq(StudentProfile::getStudentId, studentId));
    }

    @Override
    public void deleteCheckIn(Long id) { checkinMapper.deleteById(id); }
}
