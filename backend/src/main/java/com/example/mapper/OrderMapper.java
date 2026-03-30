package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Order;
import org.springframework.stereotype.Repository;

/**
 * 订单 Mapper
 */
@Repository
public interface OrderMapper extends BaseMapper<Order> {
}
