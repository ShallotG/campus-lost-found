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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Embedding 客户端
 * 调用 Flask AI 服务的 /embed 端点（本地中文语义模型）
 * 也支持 DeepSeek API 作为备选（需配置 ai.deepseek.api-key）
 */
@Slf4j
@Component
public class DeepSeekEmbeddingClient {

    /** 本地 Flask AI 服务地址 */
    private static final String LOCAL_EMBED_URL = "http://localhost:5000/embed";
    /** DeepSeek API 地址（备选） */
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/embeddings";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.deepseek.api-key:}")
    private String apiKey;

    @Value("${ai.deepseek.embedding-model:text-embedding-3-small}")
    private String model;

    @Value("${ai.deepseek.enabled:true}")
    private boolean enabled;

    public DeepSeekEmbeddingClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 将文本转换为向量（查询模式）
     *
     * @param text 输入文本
     * @return 向量(double[])，失败返回null
     */
    public double[] embed(String text) {
        if (!enabled) {
            log.warn("Embedding已禁用");
            return null;
        }

        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // 优先使用本地 Flask 服务（免费、可靠）
        double[] result = embedViaLocal(text, "query");
        if (result != null) {
            return result;
        }

        // 备选：DeepSeek API（需配置有效 API key）
        if (apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("sk-your-")) {
            log.info("本地 Embedding 不可用，尝试 DeepSeek API");
            return embedViaDeepSeek(text);
        }

        log.error("所有 Embedding 方式均不可用");
        return null;
    }

    /**
     * 对文档列表批量向量化（用于物品登记时预计算）
     */
    public List<double[]> embedDocuments(List<String> texts) {
        if (!enabled || texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        List<double[]> result = embedDocumentsViaLocal(texts);
        return result != null ? result : Collections.emptyList();
    }

    // ========== 本地 Flask 服务调用 ==========

    private double[] embedViaLocal(String text, String mode) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "text", text.trim(),
                    "mode", mode
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    LOCAL_EMBED_URL, HttpMethod.POST, requestEntity, String.class);

            if (response.getBody() != null) {
                return parseLocalResponse(response.getBody());
            }
        } catch (RestClientException e) {
            log.warn("本地 Embedding 服务不可用: {}", e.getMessage());
        } catch (Exception e) {
            log.error("解析本地 Embedding 响应失败", e);
        }
        return null;
    }

    private List<double[]> embedDocumentsViaLocal(List<String> texts) {
        try {
            Map<String, Object> requestBody = Map.of("texts", texts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    LOCAL_EMBED_URL, HttpMethod.POST, requestEntity, String.class);

            if (response.getBody() != null) {
                return parseLocalMultiResponse(response.getBody());
            }
        } catch (RestClientException e) {
            log.warn("本地 Embedding 批量调用失败: {}", e.getMessage());
        } catch (Exception e) {
            log.error("解析本地 Embedding 批量响应失败", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private double[] parseLocalResponse(String body) throws Exception {
        Map<String, Object> map = objectMapper.readValue(body,
                new TypeReference<Map<String, Object>>() {});
        List<List<Double>> embeddings = (List<List<Double>>) map.get("embeddings");
        if (embeddings != null && !embeddings.isEmpty()) {
            List<Double> vec = embeddings.get(0);
            double[] result = new double[vec.size()];
            for (int i = 0; i < vec.size(); i++) {
                result[i] = vec.get(i);
            }
            log.debug("本地 Embedding 成功: 维度={}", result.length);
            return result;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<double[]> parseLocalMultiResponse(String body) throws Exception {
        Map<String, Object> map = objectMapper.readValue(body,
                new TypeReference<Map<String, Object>>() {});
        List<List<Double>> embeddings = (List<List<Double>>) map.get("embeddings");
        if (embeddings == null) return null;
        List<double[]> results = new ArrayList<>();
        for (List<Double> vec : embeddings) {
            double[] arr = new double[vec.size()];
            for (int i = 0; i < vec.size(); i++) {
                arr[i] = vec.get(i);
            }
            results.add(arr);
        }
        return results;
    }

    // ========== DeepSeek API 调用（备选） ==========

    private double[] embedViaDeepSeek(String text) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "input", text.trim()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    DEEPSEEK_API_URL, HttpMethod.POST, requestEntity, String.class);

            return parseOpenAIResponse(response.getBody());

        } catch (RestClientException e) {
            log.error("DeepSeek API 调用失败: {}", e.getMessage());
        } catch (Exception e) {
            log.error("解析 DeepSeek 响应失败", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private double[] parseOpenAIResponse(String body) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(body,
                new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> data =
                (List<Map<String, Object>>) responseMap.get("data");
        if (data != null && !data.isEmpty()) {
            List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");
            if (embeddingList != null) {
                double[] embedding = new double[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embedding[i] = embeddingList.get(i);
                }
                log.debug("DeepSeek Embedding 成功: 维度={}", embedding.length);
                return embedding;
            }
        }
        return null;
    }
}
