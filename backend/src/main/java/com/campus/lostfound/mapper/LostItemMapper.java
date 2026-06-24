package com.campus.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.lostfound.entity.LostItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface LostItemMapper extends BaseMapper<LostItem> {

    @Select("SELECT COUNT(*) FROM lost_item WHERE DATE(create_time) = CURDATE()")
    long countTodayNewItems();

    @Select("SELECT category, COUNT(*) AS cnt FROM lost_item GROUP BY category ORDER BY cnt DESC LIMIT 10")
    List<Map<String, Object>> getCategoryStats();

    @Select("SELECT DATE(create_time) AS date, " +
            "SUM(CASE WHEN lost_item.create_time IS NOT NULL THEN 1 ELSE 0 END) AS new_items, " +
            "SUM(CASE WHEN status = 'CLAIMED' THEN 1 ELSE 0 END) AS claimed_items " +
            "FROM lost_item " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getWeeklyTrend();

    @Select("SELECT DISTINCT category FROM lost_item WHERE status = 'UNCLAIMED' AND category IS NOT NULL AND category != '' ORDER BY category")
    List<String> getDistinctCategories();
}