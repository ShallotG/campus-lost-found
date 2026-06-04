package com.campus.lostfound.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 余弦相似度计算工具类
 * 用于比对DeepSeek Embedding向量之间的相似度
 */
@Slf4j
public class CosineSimilarityUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 计算两个向量的余弦相似度 [0, 1]
     */
    public static double cosine(double[] vecA, double[] vecB) {
        if (vecA == null || vecB == null || vecA.length != vecB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 计算两个List<Double>向量（从JSON解析）的余弦相似度
     */
    public static double cosine(List<Double> vecA, List<Double> vecB) {
        if (vecA == null || vecB == null || vecA.size() != vecB.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.size(); i++) {
            dotProduct += vecA.get(i) * vecB.get(i);
            normA += vecA.get(i) * vecA.get(i);
            normB += vecB.get(i) * vecB.get(i);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 将JSON字符串解析为double[]数组
     */
    public static double[] parseEmbedding(String jsonEmbedding) {
        if (jsonEmbedding == null || jsonEmbedding.isEmpty()) {
            return null;
        }
        try {
            List<Double> list = objectMapper.readValue(jsonEmbedding, new TypeReference<List<Double>>() {});
            double[] array = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        } catch (Exception e) {
            log.error("解析embedding JSON失败", e);
            return null;
        }
    }

    /**
     * 将double[]数组序列化为JSON字符串
     */
    public static String toJson(double[] embedding) {
        if (embedding == null) return null;
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            log.error("序列化embedding失败", e);
            return null;
        }
    }
}
