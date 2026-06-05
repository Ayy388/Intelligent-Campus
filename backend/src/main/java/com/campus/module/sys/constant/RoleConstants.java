package com.campus.module.sys.constant;

/**
 * 系统角色 ID 常量表 — 与 sys_role 表 seed 数据一致
 * 注意: 若数据库重建导致自增 ID 变化，需同步更新此文件
 */
public interface RoleConstants {
    Long ADMIN = 3L;
    Long TEACHER = 2L;
    Long STUDENT = 1L;
    Long COUNSELOR = 4L;
}