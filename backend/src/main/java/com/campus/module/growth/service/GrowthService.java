package com.campus.module.growth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.growth.entity.*;

public interface GrowthService {
    StudentProfile getProfile(Long studentId);
    void saveOrUpdateProfile(StudentProfile p);
    void addEvaluation(Long studentId, String content, Long teacherId);

    Page<CheckIn> pageCheckIns(Long teacherId, int page, int size);
    CheckIn createCheckIn(CheckIn c);
    CheckInRecord doCheckIn(Long checkinId, Long studentId);
    java.util.List<CheckInRecord> getCheckInRecords(Long checkinId);
    boolean getCheckInStatus(Long checkinId, Long studentId);
}
