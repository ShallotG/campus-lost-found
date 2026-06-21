package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.ai.DeepSeekChatClient;
import com.campus.lostfound.dto.request.SearchRequest;
import com.campus.lostfound.dto.response.SearchResultResponse;
import com.campus.lostfound.entity.LostItem;
import com.campus.lostfound.mapper.LostItemMapper;
import com.campus.lostfound.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final LostItemMapper lostItemMapper;
    private final DeepSeekChatClient chatClient;

    @Override
    public SearchResultResponse search(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        int topK = request.getTopK() != null ? request.getTopK() : 5;
        String query = request.getQuery();

        // 1. DeepSeek Chat API 提取关键词
        List<String> keywords = null;
        try {
            keywords = chatClient.extractKeywords(query);
        } catch (Exception e) {
            log.warn("Chat API 关键词提取异常: {}", e.getMessage());
        }

        // 2. 降级：本地提取
        if (keywords == null || keywords.isEmpty()) {
            keywords = extractKeywords(query);
        }

        // 3. 关键词搜索
        List<LostItem> candidates = keywordSearch(keywords, Math.max(topK * 3, 20));

        // 4. 关键词无结果时，字符级回退搜索
        if (candidates.isEmpty() && keywords.stream().anyMatch(k -> k.length() >= 2)) {
            log.info("关键词无结果，启用字符级回退搜索");
            List<String> charKeys = new ArrayList<>();
            for (String kw : keywords) {
                if (kw.length() >= 2) {
                    for (char c : kw.toCharArray()) {
                        charKeys.add(String.valueOf(c));
                    }
                }
            }
            if (!charKeys.isEmpty()) {
                candidates = keywordSearch(charKeys, Math.max(topK * 3, 20));
            }
        }

        // 5. 计算匹配度并排序
        List<SearchResultResponse.SearchResultItem> scoredItems = new ArrayList<>();
        for (LostItem item : candidates) {
            double score = calculateMatchScore(keywords, item);
            scoredItems.add(SearchResultResponse.SearchResultItem.builder()
                    .id(item.getId())
                    .imageUrl(item.getImageUrl())
                    .category(item.getCategory())
                    .storageLocation(item.getStorageLocation())
                    .remark(item.getRemark())
                    .createTime(item.getCreateTime())
                    .matchScore(Math.round(score * 10000.0) / 10000.0)
                    .build());
        }

        List<SearchResultResponse.SearchResultItem> results = scoredItems.stream()
                .sorted(Comparator.comparingDouble(
                        SearchResultResponse.SearchResultItem::getMatchScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());

        // 6. Top1 生成匹配解释
        if (!results.isEmpty()) {
            SearchResultResponse.SearchResultItem top1 = results.get(0);
            try {
                String explanation = chatClient.generateExplanation(
                        query, top1.getCategory(),
                        top1.getStorageLocation(), top1.getRemark());
                top1.setExplanation(explanation);
            } catch (Exception e) {
                log.warn("解释生成失败: {}", e.getMessage());
            }
        }

        long queryTime = System.currentTimeMillis() - startTime;
        log.info("检索完成: query={}, 匹配数={}, 耗时={}ms",
                query.substring(0, Math.min(30, query.length())),
                results.size(), queryTime);

        return SearchResultResponse.builder()
                .results(results)
                .totalCompared(candidates.size())
                .queryTime(queryTime)
                .build();
    }

    private double calculateMatchScore(List<String> keywords, LostItem item) {
        if (keywords.isEmpty()) return 0.0;

        String category = item.getCategory() != null ? item.getCategory() : "";
        String location = item.getStorageLocation() != null ? item.getStorageLocation() : "";
        String remark = item.getRemark() != null ? item.getRemark() : "";

        int categoryHits = 0;
        int locationHits = 0;
        int remarkHits = 0;
        int kwCount = keywords.size();

        for (String kw : keywords) {
            if (kw.isEmpty()) continue;
            if (category.contains(kw)) categoryHits++;
            if (location.contains(kw)) locationHits++;
            if (remark.contains(kw)) remarkHits++;
        }

        double categoryScore = Math.min(1.0, (double) categoryHits / kwCount) * 0.5;
        double locationScore = Math.min(1.0, (double) locationHits / kwCount) * 0.25;
        double remarkScore = Math.min(1.0, (double) remarkHits / kwCount) * 0.25;

        return Math.min(1.0, categoryScore + locationScore + remarkScore);
    }

    private List<LostItem> keywordSearch(List<String> keywords, int topK) {
        if (keywords.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> finalKeywords = keywords.size() > 8
                ? new ArrayList<>(keywords.subList(0, 8))
                : new ArrayList<>(keywords);

        LambdaQueryWrapper<LostItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LostItem::getStatus, "UNCLAIMED");

        wrapper.and(w -> {
            for (int i = 0; i < finalKeywords.size(); i++) {
                String kw = finalKeywords.get(i).trim();
                if (kw.isEmpty()) continue;
                if (i == 0) {
                    w.like(LostItem::getCategory, kw)
                     .or().like(LostItem::getStorageLocation, kw)
                     .or().like(LostItem::getRemark, kw);
                } else {
                    w.or().like(LostItem::getCategory, kw)
                     .or().like(LostItem::getStorageLocation, kw)
                     .or().like(LostItem::getRemark, kw);
                }
            }
        });
        wrapper.orderByDesc(LostItem::getCreateTime);
        wrapper.last("LIMIT " + topK);

        return lostItemMapper.selectList(wrapper);
    }

    private static final Set<String> STOP_WORDS = Set.of(
        "我", "你", "他", "她", "它", "我们", "你们", "他们", "我的", "你的",
        "的", "了", "是", "在", "有", "和", "不", "也", "个", "都", "就",
        "这个", "那个", "哪个", "什么", "怎么", "如何", "为什么",
        "帮忙", "帮", "找", "寻找", "找到", "看看", "看见", "看到",
        "丢失", "遗失", "掉了", "弄丢", "不见了", "找不到",
        "捡到", "捡到一个", "有没有", "是否有", "能不能", "可以",
        "一个", "一件", "一把", "一条", "一台",
        "请问", "麻烦", "谢谢", "你好", "您好", "想要"
    );

    private static final int MIN_KEYWORD_LENGTH = 2;

    private List<String> extractKeywords(String query) {
        Set<String> keywords = new LinkedHashSet<>();

        String[] segments = query.split("[\\s，。！？；：、,.;:!?()（）\\[\\]【】]+");

        for (String seg : segments) {
            String cleaned = seg;
            for (String stop : STOP_WORDS) {
                cleaned = cleaned.replace(stop, " ");
            }
            cleaned = cleaned.trim().replaceAll("\\s+", " ");

            if (!cleaned.isEmpty()) {
                keywords.add(cleaned);

                if (cleaned.length() >= 4 && !cleaned.contains(" ")) {
                    for (int len = 3; len >= MIN_KEYWORD_LENGTH; len--) {
                        for (int i = 0; i <= cleaned.length() - len; i++) {
                            String sub = cleaned.substring(i, i + len);
                            if (!STOP_WORDS.contains(sub)) {
                                keywords.add(sub);
                            }
                        }
                    }
                }

                for (String part : cleaned.split(" ")) {
                    if (part.length() >= MIN_KEYWORD_LENGTH && !STOP_WORDS.contains(part)) {
                        keywords.add(part);
                    }
                }
            }
        }

        log.info("关键词提取: \"{}\" -> {}", query, keywords);
        return new ArrayList<>(keywords);
    }

}