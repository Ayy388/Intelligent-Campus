package com.campus.module.edu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.edu.entity.TrainingPlan;
import com.campus.module.edu.entity.TrainingPlanItem;
import com.campus.module.edu.service.TrainingPlanService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/edu/training-plans")
@RequiredArgsConstructor
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    // ===== Plan CRUD =====

    @GetMapping
    public Result<PageResult<TrainingPlan>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<TrainingPlan> p = trainingPlanService.pageWithDetail(page, size, keyword);
        PageResult<TrainingPlan> pr = new PageResult<>();
        pr.setRecords(p.getRecords());
        pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent());
        pr.setSize(p.getSize());
        pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/{id}")
    public Result<TrainingPlan> get(@PathVariable Long id) {
        return Result.ok(trainingPlanService.getWithDetail(id));
    }

    @PostMapping
    public Result<TrainingPlan> add(@RequestBody TrainingPlan plan) {
        TrainingPlan existing = trainingPlanService.lambdaQuery()
                .eq(TrainingPlan::getMajorId, plan.getMajorId())
                .eq(TrainingPlan::getGradeId, plan.getGradeId()).one();
        if (existing != null) {
            return Result.error(400, "该专业和年级已存在培养方案");
        }
        if (plan.getTotalSemesters() == null) plan.setTotalSemesters(8);
        if (plan.getStatus() == null) plan.setStatus(0);
        trainingPlanService.save(plan);
        return Result.ok(plan);
    }

    @PutMapping("/{id}")
    public Result<TrainingPlan> update(@PathVariable Long id, @RequestBody TrainingPlan plan) {
        plan.setId(id);
        trainingPlanService.updateById(plan);
        return Result.ok(plan);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        trainingPlanService.deletePlan(id);
        return Result.ok();
    }

    // ===== Plan Items =====

    @GetMapping("/{planId}/items")
    public Result<List<TrainingPlanItem>> getItems(
            @PathVariable Long planId,
            @RequestParam Integer semesterNumber) {
        return Result.ok(trainingPlanService.getItems(planId, semesterNumber));
    }

    @PostMapping("/{planId}/items")
    public Result<TrainingPlanItem> addItem(
            @PathVariable Long planId,
            @RequestBody TrainingPlanItem item) {
        item.setPlanId(planId);
        return Result.ok(trainingPlanService.addItem(item));
    }

    @PutMapping("/items/{itemId}")
    public Result<TrainingPlanItem> updateItem(
            @PathVariable Long itemId,
            @RequestBody TrainingPlanItem item) {
        item.setId(itemId);
        return Result.ok(trainingPlanService.updateItem(item));
    }

    @DeleteMapping("/items/{itemId}")
    public Result<Void> deleteItem(@PathVariable Long itemId) {
        trainingPlanService.deleteItem(itemId);
        return Result.ok();
    }

    // ===== Batch Generation =====

    @PostMapping("/{planId}/generate/{semesterNumber}")
    public Result<Map<String, Object>> generateSemester(
            @PathVariable Long planId,
            @PathVariable Integer semesterNumber) {
        return Result.ok(trainingPlanService.generateSemester(planId, semesterNumber));
    }

    // ===== Student View =====

    @GetMapping("/my-plan")
    public Result<Map<String, Object>> myPlan(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long userId = Long.parseLong(claims.getSubject());
        return Result.ok(trainingPlanService.getMyPlan(userId));
    }
}
