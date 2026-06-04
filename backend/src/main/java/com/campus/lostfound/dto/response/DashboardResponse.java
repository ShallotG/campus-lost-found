package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private long todayNewItems;
    private long totalReturned;
    private long currentUnclaimed;
    private List<CategoryStat> categoryStats;
    private List<WeeklyTrend> weeklyTrend;

    @Data
    @Builder
    public static class CategoryStat {
        private String category;
        private long count;
    }

    @Data
    @Builder
    public static class WeeklyTrend {
        private String date;
        private long newItems;
        private long claimedItems;
    }
}
