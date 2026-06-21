package com.campus.lostfound.utils;

import com.campus.lostfound.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList("image/jpeg", "image/png"));
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public String uploadImage(MultipartFile file) {
        validate(file);
        return uploadToLocal(file);
    }

    public void validateOnly(MultipartFile file) {
        validate(file);
    }

    public byte[] getFileBytes(MultipartFile file) {
        try { return file.getBytes(); }
        catch (IOException e) { throw new BusinessException(500, "读取文件失败"); }
    }

    private String uploadToLocal(MultipartFile file) {
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + "." + getExtension(file);
            Path targetDir = Paths.get(uploadDir, datePath);
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            String accessUrl = "/uploads/" + datePath + "/" + fileName;
            log.info("本地存储成功: {} -> {}", file.getOriginalFilename(), accessUrl);
            return accessUrl;
        } catch (IOException e) {
            log.error("本地存储失败", e);
            throw new BusinessException(500, "文件保存失败");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("上传文件不能为空");
        if (file.getSize() > MAX_FILE_SIZE) throw new BusinessException(413, "文件大小超过限制(最大5MB)");
        String fn = file.getOriginalFilename();
        if (fn == null || !fn.contains(".")) throw new BusinessException(415, "不支持的文件类型");
        String ext = getExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(ext)) throw new BusinessException(415, "不支持的文件类型，仅支持 jpg/png");
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_MIME_TYPES.contains(ct.toLowerCase())) throw new BusinessException(415, "不支持的文件类型，仅支持 jpg/png");
        if (!isValidImageMagicNumber(file)) throw new BusinessException(415, "文件内容不是有效的图片格式");
    }

    private boolean isValidImageMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            if (is.read(header) < 4) return false;
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) return true;
            if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) return true;
            return false;
        } catch (IOException e) { return false; }
    }

    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null && name.contains(".") ? name.substring(name.lastIndexOf(".") + 1).toLowerCase() : "";
    }
}