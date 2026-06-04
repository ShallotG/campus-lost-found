"""
YOLOv8 物品检测器
使用预训练模型对图片进行目标检测，返回置信度最高的物品类别
同时分析图片主色调，生成更细致的描述（如"白色水杯"）
"""
import io
import logging
from PIL import Image
from ultralytics import YOLO
from config import MODEL_NAME, COCO_CATEGORIES_CN, IGNORE_CATEGORIES
from color_detector import get_color_description, generate_description

logger = logging.getLogger(__name__)

# 全局模型实例（懒加载）
_model = None


def get_model():
    """获取或初始化YOLO模型（懒加载）"""
    global _model
    if _model is None:
        logger.info(f"正在加载YOLO模型: {MODEL_NAME}")
        _model = YOLO(MODEL_NAME)
        logger.info("YOLO模型加载完成")
    return _model


def detect_category(image_bytes: bytes) -> dict:
    """
    检测图片中的物品类别

    Args:
        image_bytes: 图片二进制数据

    Returns:
        {
            "category": "bottle",           # 最佳匹配英文名
            "category_cn": "水杯",          # 最佳匹配中文名
            "confidence": 0.85,             # 最佳匹配置信度
            "top3": [                       # Top3候选项
                {"category": "bottle", "category_cn": "水杯", "confidence": 0.85},
                {"category": "cup", "category_cn": "杯子", "confidence": 0.72},
                {"category": "bowl", "category_cn": "碗", "confidence": 0.45}
            ]
        }
    """
    model = get_model()

    # 将 bytes 转为 PIL Image（YOLO 不接受原始 bytes）
    img = Image.open(io.BytesIO(image_bytes))

    # 执行推理
    results = model(img, verbose=False)

    # 如果没有检测到任何物体
    if not results or len(results) == 0 or results[0].boxes is None:
        return _empty_result()

    # 获取所有检测结果
    boxes = results[0].boxes
    if len(boxes) == 0:
        return _empty_result()

    # 按置信度降序排序，收集有效失物类别（跳过人物/动物等）
    confidences = boxes.conf.cpu().numpy()
    class_ids = boxes.cls.cpu().numpy().astype(int)
    sorted_indices = confidences.argsort()[::-1]

    # 收集 Top3 有效类别
    top3 = []
    top3_seen = set()
    for idx in sorted_indices:
        cid = class_ids[idx]
        name_en = model.names[cid]
        if name_en not in IGNORE_CATEGORIES and name_en not in top3_seen:
            name_cn = COCO_CATEGORIES_CN.get(name_en, name_en)
            top3.append({
                "category": name_en,
                "category_cn": name_cn,
                "confidence": round(float(confidences[idx]), 4)
            })
            top3_seen.add(name_en)
        if len(top3) >= 3:
            break

    # 如果 Top3 不够，用被忽略的类别补足（至少给用户一点参考）
    if len(top3) < 3:
        for idx in sorted_indices:
            cid = class_ids[idx]
            name_en = model.names[cid]
            if name_en not in top3_seen:
                name_cn = COCO_CATEGORIES_CN.get(name_en, name_en)
                top3.append({
                    "category": name_en,
                    "category_cn": name_cn,
                    "confidence": round(float(confidences[idx]), 4)
                })
                top3_seen.add(name_en)
            if len(top3) >= 3:
                break

    # 未检测到任何物体
    if len(top3) == 0:
        return _empty_result()

    # 最佳匹配
    best = top3[0]
    logger.info(f"检测结果(Top3): {[(t['category_cn'], t['confidence']) for t in top3]}")

    # 颜色分析——生成更细致的描述
    color_info = get_color_description(image_bytes)
    description = generate_description(best["category_cn"], color_info)

    logger.info(f"细致描述: {description}")

    return {
        "category": best["category"],
        "category_cn": best["category_cn"],
        "confidence": best["confidence"],
        "description": description,
        "color_info": color_info,
        "top3": top3
    }


def _empty_result():
    return {
        "category": "unknown",
        "category_cn": "未知物品",
        "confidence": 0.0,
        "description": "未知物品",
        "color_info": {"dominant_color": "未知", "color_confidence": 0.0, "colors": []},
        "top3": []
    }
