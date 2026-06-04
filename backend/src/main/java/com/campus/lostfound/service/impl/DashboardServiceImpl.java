package com.campus.lostfound.service.impl;

import com.campus.lostfound.dto.response.DashboardResponse;
import com.campus.lostfound.mapper.ClaimRecordMapper;
import com.campus.lostfound.mapper.LostItemMapper;
import com.campus.lostfound.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final LostItemMapper lostItemMapper;
    private final ClaimRecordMapper claimRecordMapper;

    @Override
    public DashboardResponse getStats() {
        long todayNewItems = lostItemMapper.countTodayNewItems();
        long totalReturned = claimRecordMapper.countTotalReturned();
        long currentUnclaimed = claimRecordMapper.countCurrentUnclaimed();

        // 热门类别
        List<Map<String, Object>> categoryStatsRaw = lostItemMapper.getCategoryStats();
        List<DashboardResponse.CategoryStat> categoryStats = categoryStatsRaw.stream()
                .map(m -> DashboardResponse.CategoryStat.builder()
                        .category((String) m.get("category"))
                        .count(((Number) m.get("cnt")).longValue())
                        .build())
                .collect(Collectors.toList());

        // 近7天趋势
        List<Map<String, Object>> weeklyTrendRaw = lostItemMapper.getWeeklyTrend();
        List<DashboardResponse.WeeklyTrend> weeklyTrend = weeklyTrendRaw.stream()
                .map(m -> DashboardResponse.WeeklyTrend.builder()
                        .date(m.get("date").toString())
                        .newItems(((Number) m.get("new_items")).longValue())
                        .claimedItems(((Number) m.get("claimed_items")).longValue())
                        .build())
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .todayNewItems(todayNewItems)
                .totalReturned(totalReturned)
                .currentUnclaimed(currentUnclaimed)
                .categoryStats(categoryStats)
                .weeklyTrend(weeklyTrend)
                .build();
    }
}
