package com.campus.lostfound.ai;

import com.campus.lostfound.dto.response.YoloDetectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * YOLO检测服务HTTP客户端
 * 调用Flask微服务进行物品类别检测
 */
@Slf4j
@Component
public class YoloClient {

    private final RestTemplate restTemplate;

    @Value("${ai.yolo.service-url:http://localhost:5000}")
    private String yoloServiceUrl;

    @Value("${ai.yolo.enabled:true}")
    private boolean enabled;

    public YoloClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 调用YOLO服务检测图片（本地文件路径模式）
     *
     * @param imagePath 图片本地路径
     * @return 检测结果
     */
    public YoloDetectResponse detect(String imagePath) {
        if (!enabled) {
            log.info("YOLO服务已禁用，跳过检测");
            return createUnknownResult();
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            log.warn("图片文件不存在: {}", imagePath);
            return createUnknownResult();
        }

        return doDetect("file", new FileSystemResource(imageFile), String.valueOf(imageFile.length()));
    }

    /**
     * 调用YOLO服务检测图片（字节数组模式，OSS上传时使用）
     *
     * @param imageBytes 图片字节数组
     * @param filename   原始文件名
     * @return 检测结果
     */
    public YoloDetectResponse detect(byte[] imageBytes, String filename) {
        if (!enabled) {
            log.info("YOLO服务已禁用，跳过检测");
            return createUnknownResult();
        }

        if (imageBytes == null || imageBytes.length == 0) {
            log.warn("图片数据为空");
            return createUnknownResult();
        }

        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        return doDetect("file", resource, String.valueOf(imageBytes.length));
    }

    /**
     * 实际的HTTP调用逻辑
     */
    private YoloDetectResponse doDetect(String fieldName, Object resource, String sizeHint) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add(fieldName, resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<YoloDetectResponse> response = restTemplate.exchange(
                    yoloServiceUrl + "/detect",
                    HttpMethod.POST,
                    requestEntity,
                    YoloDetectResponse.class
            );

            if (response.getBody() != null) {
                log.info("YOLO检测成功: category={}, confidence={}",
                        response.getBody().getCategoryCn(), response.getBody().getConfidence());
                return response.getBody();
            }

        } catch (RestClientException e) {
            log.warn("YOLO服务调用失败(将降级为手动填写): {}", e.getMessage());
        }

        return createUnknownResult();
    }

    private YoloDetectResponse createUnknownResult() {
        YoloDetectResponse resp = new YoloDetectResponse();
        resp.setCategory("unknown");
        resp.setCategoryCn("未知物品");
        resp.setConfidence(0.0);
        return resp;
    }
}
