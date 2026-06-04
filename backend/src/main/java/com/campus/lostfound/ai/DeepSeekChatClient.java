package com.campus.lostfound.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek Chat API 客户端（可选）
 * 用于生成匹配解释文本
 */
@Slf4j
@Component
public class DeepSeekChatClient {

    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.chat-model:deepseek-chat}")
    private String model;

    @Value("${ai.deepseek.enabled:true}")
    private boolean enabled;

    public DeepSeekChatClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成匹配解释
     *
     * @param userQuery 失主的搜索描述
     * @param itemCategory 物品类别
     * @param itemLocation 存放位置
     * @param itemRemark 备注
     * @return 解释文本，失败返回null
     */
    /**
     * 从自然语言描述中提取搜索关键词
     * 使用 Chat API 理解语义，提取物品名称、颜色、品牌等关键词
     *
     * @param query 用户自然语言，如"我丢失了一个绿色水杯"
     * @return 关键词列表，如["绿色", "水杯"]；失败返回null
     */
    public List<String> extractKeywords(String query) {
        if (!enabled) {
            log.debug("DeepSeek Chat已禁用");
            return null;
        }

        try {
            String prompt = String.format(
                    "从失物描述中提取搜索关键词（物品名称、颜色、品牌、材质、特征等）。" +
                    "用逗号分隔。只输出关键词，不要解释，不要多余内容。\n\n" +
                    "描述：%s\n关键词：", query
            );

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 60,
                    "temperature", 0.1
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, requestEntity, String.class);

            if (response.getBody() != null) {
                Map<String, Object> responseMap = objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<Map<String, Object>>() {}
                );

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message =
                            (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        String content = ((String) message.get("content")).trim();
                        // 解析逗号分隔的关键词
                        List<String> keywords = new ArrayList<>();
                        for (String kw : content.split("[，,、\\n]")) {
                            kw = kw.trim().replaceAll("^[\"'“”‘’]|[\"'“”‘’]$", "");
                            if (!kw.isEmpty() && kw.length() <= 20) {
                                keywords.add(kw);
                            }
                        }
                        log.info("Chat API 关键词提取: \"{}\" → {}", query, keywords);
                        return keywords.isEmpty() ? null : keywords;
                    }
                }
            }

        } catch (RestClientException e) {
            log.warn("DeepSeek Chat 关键词提取失败: {}", e.getMessage());
        } catch (Exception e) {
            log.error("解析 Chat 关键词响应失败", e);
        }

        return null;
    }

    public String generateExplanation(String userQuery, String itemCategory,
                                       String itemLocation, String itemRemark) {
        if (!enabled) {
            log.debug("DeepSeek Chat已禁用，跳过生成解释");
            return null;
        }

        try {
            String prompt = String.format(
                    "你是一个校园失物招领系统的助手。请根据以下信息，用一句话（不超过50字）" +
                    "解释为什么这个拾物可能与失主的描述匹配：\n" +
                    "失主描述：%s\n" +
                    "拾物类别：%s\n" +
                    "存放位置：%s\n" +
                    "备注：%s\n\n" +
                    "请直接输出解释文本，不要加任何前缀。",
                    userQuery, itemCategory, itemLocation,
                    itemRemark != null ? itemRemark : "无"
            );

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 100,
                    "temperature", 0.3
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getBody() != null) {
                Map<String, Object> responseMap = objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<Map<String, Object>>() {}
                );

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        String content = (String) message.get("content");
                        log.debug("匹配解释生成成功");
                        return content;
                    }
                }
            }

        } catch (RestClientException e) {
            log.warn("DeepSeek Chat API调用失败: {}", e.getMessage());
        } catch (Exception e) {
            log.error("解析Chat响应失败", e);
        }

        return null;
    }
}
