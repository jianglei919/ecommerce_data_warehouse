package com.uwindsor.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.uwindsor.warehouse.domain.UnifiedOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统一订单项Mapper
 */
@Mapper
public interface UnifiedOrderItemMapper extends BaseMapper<UnifiedOrderItem> {

    /**
     * 按订单ID查询所有项目
     */
    @Select("""
                SELECT * FROM unified_order_items
                WHERE unified_order_id = #{unifiedOrderId}
                ORDER BY unified_item_id
            """)
    List<UnifiedOrderItem> selectByOrderId(Integer unifiedOrderId);

    /**
     * 商品销售统计
     */
    @Select("""
                SELECT
                    product_id,
                    product_name,
                    category,
                    COUNT(*) as sale_count,
                    SUM(quantity) as total_quantity,
                    SUM(subtotal) as total_revenue,
                    AVG(unit_price) as avg_price
                FROM unified_order_items
                GROUP BY product_id
                ORDER BY total_revenue DESC
            """)
    List<Map<String, Object>> selectProductSalesStats();

}
