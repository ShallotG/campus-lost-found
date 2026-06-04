package com.campus.lostfound.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.campus.lostfound.common.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class FileUploadUtil {

    // ========== 允许的文件类型 ==========
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList("image/jpeg", "image/png"));
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // ========== 本地存储配置 ==========
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    // ========== 存储模式 ==========
    @Value("${file.storage-mode:local}")
    private String storageMode;

    // ========== OSS 配置 ==========
    @Value("${file.oss.endpoint:}")
    private String ossEndpoint;
    @Value("${file.oss.access-key-id:}")
    private String ossAccessKeyId;
    @Value("${file.oss.access-key-secret:}")
    private String ossAccessKeySecret;
    @Value("${file.oss.bucket-name:}")
    private String ossBucketName;
    @Value("${file.oss.custom-domain:}")
    private String ossCustomDomain;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        if ("oss".equalsIgnoreCase(storageMode)) {
            if (ossEndpoint.isEmpty() || ossAccessKeyId.isEmpty() || ossBucketName.isEmpty()) {
                log.warn("OSS模式已启用但配置不完整，请检查 file.oss.* 配置项");
            } else {
                this.ossClient = new OSSClientBuilder()
                        .build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
                log.info("OSS客户端初始化成功: bucket={}, endpoint={}", ossBucketName, ossEndpoint);
            }
        } else {
            log.info("文件存储模式: 本地 (upload-dir={})", uploadDir);
        }
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS客户端已关闭");
        }
    }

    /**
     * 上传图片文件
     * <p>根据 storage-mode 自动选择本地或OSS存储</p>
     *
     * @param file 上传的文件
     * @return 文件访问URL
     */
    public String uploadImage(MultipartFile file) {
        validate(file);

        if ("oss".equalsIgnoreCase(storageMode)) {
            return uploadToOss(file);
        }
        return uploadToLocal(file);
    }

    /**
     * 仅校验文件，不上传
     */
    public void validateOnly(MultipartFile file) {
        validate(file);
    }

    /**
     * 获取文件字节数组（OSS模式下YOLO检测需要）
     */
    public byte[] getFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(500, "读取文件失败");
        }
    }

    // ==================== 本地存储 ====================

    private String uploadToLocal(MultipartFile file) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + "." + getExtension(file);
            Path targetDir = Paths.get(uploadDir, datePath);
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(fileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String accessUrl = "/uploads/" + datePath + "/" + fileName;
            log.info("本地存储成功: {} -> {}", file.getOriginalFilename(), accessUrl);
            return accessUrl;
        } catch (IOException e) {
            log.error("本地存储失败", e);
            throw new BusinessException(500, "文件保存失败，请稍后重试");
        }
    }

    // ==================== OSS 存储 ====================

    private String uploadToOss(MultipartFile file) {
        if (ossClient == null) {
            throw new BusinessException(500, "OSS客户端未初始化，请检查配置");
        }

        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + "." + getExtension(file);
            String objectKey = datePath + "/" + fileName;

            // 上传到OSS
            PutObjectRequest putRequest = new PutObjectRequest(
                    ossBucketName, objectKey,
                    new ByteArrayInputStream(file.getBytes())
            );
            // 设置公开读（或使用bucket策略）
            // putRequest.setCannedAcl(CannedAccessControlList.PublicRead);
            ossClient.putObject(putRequest);

            // 构建访问URL
            String accessUrl = buildOssUrl(objectKey);
            log.info("OSS上传成功: {} -> {}", file.getOriginalFilename(), accessUrl);
            return accessUrl;

        } catch (IOException e) {
            log.error("OSS上传失败", e);
            throw new BusinessException(500, "文件上传到OSS失败");
        }
    }

    /**
     * 构建OSS文件访问URL
     */
    private String buildOssUrl(String objectKey) {
        if (ossCustomDomain != null && !ossCustomDomain.isBlank()) {
            // 自定义域名
            String domain = ossCustomDomain.replaceAll("/+$", "");
            return domain + "/" + objectKey;
        }
        // 默认OSS域名
        return "https://" + ossBucketName + "." + ossEndpoint + "/" + objectKey;
    }

    // ==================== 文件校验 ====================

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(413, "文件大小超过限制(最大5MB)");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(415, "不支持的文件类型");
        }
        String extension = getExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(415, "不支持的文件类型，仅支持 jpg/png");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(415, "不支持的文件类型，仅支持 jpg/png");
        }

        if (!isValidImageMagicNumber(file)) {
            throw new BusinessException(415, "文件内容不是有效的图片格式");
        }
    }

    private boolean isValidImageMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4) return false;
            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) return true;
            // PNG: 89 50 4E 47
            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) return true;
            return false;
        } catch (IOException e) {
            log.warn("Magic Number校验失败", e);
            return false;
        }
    }

    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null && name.contains(".") ? name.substring(name.lastIndexOf(".") + 1).toLowerCase() : "";
    }
}
