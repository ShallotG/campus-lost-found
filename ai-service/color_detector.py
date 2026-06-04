"""
图片颜色分析模块
使用 HSV 色彩空间分析物品主色调，生成更细致的描述（如"白色水杯"）
"""
import io
import logging
import numpy as np
from PIL import Image

logger = logging.getLogger(__name__)

# HSV 颜色范围定义 (H: 0-180, S: 0-255, V: 0-255, OpenCV风格)
# 每个颜色由 (lower, upper) 元组定义
COLOR_RANGES_HSV = {
    "白色":   ([0,   0, 180], [180,  30, 255]),
    "黑色":   ([0,   0,   0], [180, 255,  60]),
    "灰色":   ([0,   0,  60], [180,  40, 200]),
    "银色":   ([0,   0, 150], [180,  20, 230]),
    "红色":   ([0,  50,  50], [ 10, 255, 255]),
    "深红":   ([170, 50,  50], [180, 255, 255]),
    "橙色":   ([11,  50,  50], [ 25, 255, 255]),
    "黄色":   ([26,  50,  50], [ 35, 255, 255]),
    "绿色":   ([36,  50,  50], [ 85, 255, 255]),
    "蓝色":   ([86,  50,  50], [125, 255, 255]),
    "深蓝":   ([110, 50,  50], [130, 255, 255]),
    "紫色":   ([126, 50,  50], [155, 255, 255]),
    "粉色":   ([156, 40,  50], [170, 255, 255]),
    "棕色":   ([10,  50,  20], [ 25, 200, 120]),
    "卡其":   ([20,  30,  80], [ 40, 120, 200]),
}


def _classify_pixel_hsv(h, s, v):
    """将单个HSV像素归类到颜色名"""
    for name, (lower, upper) in COLOR_RANGES_HSV.items():
        if lower[0] <= h <= upper[0] and lower[1] <= s <= upper[1] and lower[2] <= v <= upper[2]:
            # 特殊处理：深红和红色合并
            if name == "深红":
                return "红色"
            if name == "深蓝":
                return "蓝色"
            if name == "银色":
                return "白色"
            if name == "卡其":
                return "棕色"
            return name
    return None


def get_color_description(image_bytes: bytes) -> dict:
    """
    分析图片主色调，返回颜色描述信息

    Args:
        image_bytes: 图片二进制数据

    Returns:
        {
            "dominant_color": "白色",    # 主色调中文名
            "color_confidence": 0.72,    # 主色调占比
            "colors": [                  # Top3 颜色分布
                {"name": "白色", "ratio": 0.72},
                {"name": "灰色", "ratio": 0.15},
                {"name": "蓝色", "ratio": 0.05}
            ]
        }
    """
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB")

    # 缩小图片加速（保留中心区域，因为物品通常在画面中央）
    w, h = img.size
    # 取中心 60% 区域（排除边缘背景）
    crop_left = int(w * 0.2)
    crop_top = int(h * 0.2)
    crop_right = int(w * 0.8)
    crop_bottom = int(h * 0.8)
    img = img.crop((crop_left, crop_top, crop_right, crop_bottom))

    # 缩放到 100x100 以内
    img.thumbnail((100, 100))

    # 转为 HSV 数组
    hsv = img.convert("HSV")
    pixels = np.array(hsv).reshape(-1, 3)

    # 统计每种颜色的像素数
    color_counts = {}
    total_pixels = len(pixels)

    for h, s, v in pixels:
        # 跳过极暗/接近背景的像素（可能是阴影或背景）
        if v < 20:
            continue
        # 跳过饱和度极低的像素（接近纯白/灰/黑，单独处理）
        if s < 15:
            if v > 200:
                name = "白色"
            elif v < 50:
                name = "黑色"
            else:
                name = "灰色"
        else:
            name = _classify_pixel_hsv(int(h), int(s), int(v))

        if name:
            color_counts[name] = color_counts.get(name, 0) + 1

    if not color_counts:
        return {"dominant_color": "未知", "color_confidence": 0.0, "colors": []}

    # 按占比排序
    sorted_colors = sorted(color_counts.items(), key=lambda x: x[1], reverse=True)
    total_colored = sum(color_counts.values())

    top_colors = []
    for name, count in sorted_colors[:3]:
        ratio = round(count / total_colored, 4)
        top_colors.append({"name": name, "ratio": ratio})

    dominant = top_colors[0]

    top3_str = ", ".join(f"{c['name']}({c['ratio']:.1%})" for c in top_colors)
    logger.info("颜色分析: 主色调=%s(%.1f%%), Top3=[%s]",
                dominant['name'], dominant['ratio'] * 100, top3_str)

    return {
        "dominant_color": dominant["name"],
        "color_confidence": dominant["ratio"],
        "colors": top_colors
    }


def generate_description(category_cn: str, color_info: dict) -> str:
    """
    结合分类和颜色生成细致描述

    Args:
        category_cn: YOLO识别中文类别，如"水杯"
        color_info: get_color_description 返回的颜色信息

    Returns:
        细致描述，如"白色水杯"
    """
    dominant = color_info.get("dominant_color", "")
    confidence = color_info.get("color_confidence", 0)

    # 颜色置信度太低时不附加颜色
    if not dominant or dominant == "未知" or confidence < 0.15:
        return category_cn

    # 避免颜色名和类别名重复（如"白色白色T恤"）
    if dominant in category_cn:
        return category_cn

    return f"{dominant}{category_cn}"
