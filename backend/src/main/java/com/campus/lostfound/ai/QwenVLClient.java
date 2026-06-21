package com.campus.lostfound.ai;

import com.campus.lostfound.dto.response.DetectResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 千问VL视觉识别客户端
 * 调用 DashScope 千问视觉模型识别物品类别、颜色、材质
 */
@Slf4j
@Component
public class QwenVLClient {

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.qwen.vl.api-key}")
    private String apiKey;

    @Value("${ai.qwen.vl.model:qwen-vl-plus}")
    private String model;

    @Value("${ai.qwen.vl.enabled:true}")
    private boolean enabled;

    public QwenVLClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 调用千问VL识别图片中的物品（本地文件路径模式）
     */
    public DetectResponse detect(String imagePath) {
        if (!enabled) {
            log.info("千问VL已禁用，返回未知物品");
            return createUnknownResult();
        }

        java.io.File imageFile = new java.io.File(imagePath);
        if (!imageFile.exists()) {
            log.warn("图片文件不存在: {}", imagePath);
            return createUnknownResult();
        }

        try {
            byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
            return doDetect(imageBytes);
        } catch (Exception e) {
            log.error("读取图片文件失败: {}", e.getMessage());
            return createUnknownResult();
        }
    }

    /**
     * 调用千问VL识别图片中的物品（字节数组模式，OSS上传时使用）
     */
    public DetectResponse detect(byte[] imageBytes, String filename) {
        if (!enabled) {
            log.info("千问VL已禁用，返回未知物品");
            return createUnknownResult();
        }

        if (imageBytes == null || imageBytes.length == 0) {
            log.warn("图片数据为空");
            return createUnknownResult();
        }

        return doDetect(imageBytes);
    }

    /**
     * 实际HTTP调用逻辑
     */
    private DetectResponse doDetect(byte[] imageBytes) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String dataUri = "data:image/jpeg;base64," + base64Image;

            String prompt = ""
                + "请识别这张图片中的物品。按照以下JSON格式返回，只返回JSON，不要加任何解释：\n"
                + "{\n"
                + "  \"category\": \"物品英文名(使用COCO数据集命名)\",\n"
                + "  \"category_cn\": \"中文名\",\n"
                + "  \"confidence\": 0.95,\n"
                + "  \"color\": \"颜色\",\n"
                + "  \"material\": \"材质\",\n"
                + "  \"description\": \"颜色+材质+类别,如'黑色皮质双肩背包'\",\n"
                + "  \"top3\": [\n"
                + "    {\"category\": \"backpack\", \"category_cn\": \"书包\", \"confidence\": 0.95},\n"
                + "    {\"category\": \"handbag\", \"category_cn\": \"手提包\", \"confidence\": 0.70},\n"
                + "    {\"category\": \"suitcase\", \"category_cn\": \"行李箱\", \"confidence\": 0.45}\n"
                + "  ]\n"
                + "}\n"
                + "注意：如果图片中没有任何物品，返回category为\"unknown\",category_cn为\"未知物品\",confidence为0.0,top3为空数组。\n"
                + "top3中的物品类别英文名请使用COCO的80类英文名。";

            Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", List.of(
                    Map.of("type", "image_url", "image_url",
                        Map.of("url", dataUri)),
                    Map.of("type", "text", "text", prompt)
                )
            );

            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(userMessage),
                "max_tokens", 300,
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
                        String content = (String) message.get("content");
                        return parseVLResponse(content);
                    }
                }
            }

        } catch (RestClientException e) {
            log.warn("千问VL调用失败(降级为手动填写): {}", e.getMessage());
        } catch (Exception e) {
            log.error("解析千问VL响应失败", e);
        }

        return createUnknownResult();
    }

    /**
     * 解析千问VL返回的JSON，提取结构化检测结果
     */
    private DetectResponse parseVLResponse(String content) {
        try {
            // 提取JSON块（千问可能在JSON外面包了```json...```）
            String json = content.trim();
            Pattern p = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
            Matcher m = p.matcher(json);
            if (m.find()) {
                json = m.group(1).trim();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(json,
                new TypeReference<Map<String, Object>>() {});

            DetectResponse resp = new DetectResponse();
            resp.setCategory(getString(result, "category", "unknown"));
            resp.setCategoryCn(getString(result, "category_cn", "未知物品"));
            resp.setConfidence(getDouble(result, "confidence", 0.0));

            // 组合描述：颜色 + 材质 + 类别
            String color = getString(result, "color", "");
            String material = getString(result, "material", "");
            String desc = getString(result, "description", "");
            if (desc.isEmpty()) {
                desc = (color + material + resp.getCategoryCn()).trim();
            }
            resp.setDescription(desc.isEmpty() ? resp.getCategoryCn() : desc);

            // 解析 top3
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> top3Raw =
                (List<Map<String, Object>>) result.get("top3");
            if (top3Raw != null) {
                List<DetectResponse.CategoryOption> top3 = new ArrayList<>();
                for (Map<String, Object> opt : top3Raw) {
                    DetectResponse.CategoryOption co = new DetectResponse.CategoryOption();
                    co.setCategory(getString(opt, "category", "unknown"));
                    co.setCategoryCn(getString(opt, "category_cn", "未知"));
                    co.setConfidence(getDouble(opt, "confidence", 0.0));
                    top3.add(co);
                }
                resp.setTop3(top3);
            }

            log.info("千问VL识别成功: category={}, description={}, confidence={}",
                resp.getCategoryCn(), resp.getDescription(), resp.getConfidence());
            return resp;

        } catch (Exception e) {
            log.warn("解析千问VL JSON失败，原始返回: {}", content, e);
        }
        return createUnknownResult();
    }

    private String getString(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }

    private double getDouble(Map<String, Object> map, String key, double defaultVal) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }

    private DetectResponse createUnknownResult() {
        DetectResponse resp = new DetectResponse();
        resp.setCategory("unknown");
        resp.setCategoryCn("未知物品");
        resp.setConfidence(0.0);
        resp.setDescription("未知物品");
        resp.setTop3(Collections.emptyList());
        return resp;
    }
}
