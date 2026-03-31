package com.uwindsor.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uwindsor.warehouse.domain.UnifiedOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统一订单Mapper
 */
@Mapper
public interface UnifiedOrderMapper extends BaseMapper<UnifiedOrder> {

    /**
     * 查询所有统一订单及其项目详情
     */
    @Select("""
                SELECT
                    uo.unified_order_id,
                    uo.source,
                    uo.app_order_id,
                    uo.web_order_no,
                    uo.user_id,
                    uo.order_date,
                    uo.total_amount,
                    uo.status,
                    COUNT(uoi.unified_item_id) as item_count,
                    uo.created_at,
                    uo.updated_at
                FROM unified_orders uo
                LEFT JOIN unified_order_items uoi ON uo.unified_order_id = uoi.unified_order_id
                GROUP BY uo.unified_order_id
                ORDER BY uo.order_date DESC, uo.created_at DESC
            """)
    List<Map<String, Object>> selectOrdersWithItemCount();

    /**
     * 按来源统计订单
     */
    @Select("""
                SELECT
                    source,
                    COUNT(*) as order_count,
                    SUM(total_amount) as total_sales,
                    AVG(total_amount) as avg_order_value
                FROM unified_orders
                GROUP BY source
            """)
    List<Map<String, Object>> selectOrderStatsBySource();

}
