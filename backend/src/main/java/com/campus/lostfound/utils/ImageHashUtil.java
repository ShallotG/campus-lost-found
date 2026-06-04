package com.campus.lostfound.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * 感知哈希(pHash)工具类
 * 用于图片重复检测
 */
@Slf4j
public class ImageHashUtil {

    /**
     * 计算图片的感知哈希 (pHash)
     * 基于DCT（离散余弦变换）的感知哈希算法
     *
     * @param imagePath 图片文件路径
     * @return 64位哈希字符串，失败返回null
     */
    public static String phash(String imagePath) {
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                log.warn("图片文件不存在: {}", imagePath);
                return null;
            }
            return computeHash(ImageIO.read(file));

        } catch (IOException e) {
            log.error("计算pHash失败: {}", imagePath, e);
            return null;
        }
    }

    /**
     * 计算图片的感知哈希（字节数组模式，OSS上传时使用）
     */
    public static String phash(byte[] imageBytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img == null) {
                log.warn("无法解析图片字节数据");
                return null;
            }
            return computeHash(img);
        } catch (IOException e) {
            log.error("计算pHash失败", e);
            return null;
        }
    }

    private static String computeHash(BufferedImage img) {
        if (img == null) return null;

        // 缩放到8x8灰度图
        BufferedImage resized = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, 8, 8, null);
        g.dispose();

            // 2. 计算像素平均值
            int total = 0;
            int[] pixels = new int[64];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    int pixel = new Color(resized.getRGB(j, i)).getRed();
                    pixels[i * 8 + j] = pixel;
                    total += pixel;
                }
            }
            double avg = (double) total / 64;

            // 3. 生成哈希：大于平均值为1，否则为0
            StringBuilder hash = new StringBuilder();
            for (int pixel : pixels) {
                hash.append(pixel > avg ? "1" : "0");
            }

            return hash.toString();
    }

    /**
     * 比较两个pHash的汉明距离
     *
     * @param hash1 pHash字符串1
     * @param hash2 pHash字符串2
     * @return 汉明距离 [0, 64]，值越小越相似；-1表示比较失败
     */
    public static int hammingDistance(String hash1, String hash2) {
        if (hash1 == null || hash2 == null || hash1.length() != hash2.length()) {
            return -1;
        }

        int distance = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                distance++;
            }
        }
        return distance;
    }

    /**
     * 判断两个pHash是否相似
     *
     * @param hash1 pHash字符串1
     * @param hash2 pHash字符串2
     * @param threshold 汉明距离阈值（≤此值视为相似，默认10）
     * @return true表示相似
     */
    public static boolean isSimilar(String hash1, String hash2, int threshold) {
        int distance = hammingDistance(hash1, hash2);
        return distance >= 0 && distance <= threshold;
    }
}
