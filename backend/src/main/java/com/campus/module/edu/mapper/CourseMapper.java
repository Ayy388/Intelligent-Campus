package com.campus.module.edu.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.edu.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    @Update("UPDATE edu_course SET enrolled = enrolled + 1 WHERE id = #{id} AND enrolled < capacity")
    int updateEnrolled(@Param("id") Long id);
}
