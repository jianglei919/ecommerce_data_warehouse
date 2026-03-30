package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Review;
import org.springframework.stereotype.Repository;

/**
 * 评论 Mapper
 */
@Repository
public interface ReviewMapper extends BaseMapper<Review> {
}
