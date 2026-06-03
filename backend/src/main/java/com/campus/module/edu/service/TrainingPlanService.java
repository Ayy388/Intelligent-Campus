package com.campus.module.edu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.edu.entity.TrainingPlan;
import com.campus.module.edu.entity.TrainingPlanItem;

import java.util.List;
import java.util.Map;

public interface TrainingPlanService extends IService<TrainingPlan> {
    Page<TrainingPlan> pageWithDetail(int page, int size, String keyword);
    TrainingPlan getWithDetail(Long id);

    List<TrainingPlanItem> getItems(Long planId, Integer semesterNumber);
    TrainingPlanItem addItem(TrainingPlanItem item);
    TrainingPlanItem updateItem(TrainingPlanItem item);
    void deleteItem(Long itemId);

    void deletePlan(Long id);

    Map<String, Object> generateSemester(Long planId, Integer semesterNumber);

    Map<String, Object> getMyPlan(Long studentId);
}
