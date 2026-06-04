"""
本地中文文本 Embedding 服务
使用 BAAI/bge-small-zh-v1.5 模型（384维，专为中文优化，约100MB）
首次启动自动下载模型，之后缓存到本地
"""
import logging
import numpy as np
from sentence_transformers import SentenceTransformer

logger = logging.getLogger(__name__)

# 使用 BGE 中文小模型（轻量、效果好）
MODEL_NAME = "BAAI/bge-small-zh-v1.5"

# 全局模型实例（懒加载）
_model = None


def get_model():
    """获取或初始化Embedding模型（懒加载，首次调用时下载）"""
    global _model
    if _model is None:
        logger.info(f"正在加载 Embedding 模型: {MODEL_NAME}")
        _model = SentenceTransformer(MODEL_NAME)
        logger.info(f"Embedding 模型加载完成，维度={_model.get_sentence_embedding_dimension()}")
    return _model


def embed(texts: list[str]) -> list[list[float]]:
    """
    将文本列表转换为向量列表

    Args:
        texts: 文本列表，如 ["白色水杯 存放位置:A柜台 备注:无"]

    Returns:
        向量列表，每个向量为 float 列表
    """
    model = get_model()

    # BGE 模型建议对短文本加前缀以提升效果
    # (对检索场景，query加"为这个句子生成表示以用于检索相关文章：")
    embeddings = model.encode(texts, normalize_embeddings=True)

    # 转为 Python list
    result = []
    for emb in embeddings:
        result.append(emb.tolist())

    logger.debug(f"Embedding 完成: 文本数={len(texts)}, 维度={len(result[0]) if result else 0}")
    return result


def embed_query(text: str) -> list[float]:
    """
    对单个查询文本生成向量（query专用，加检索前缀）
    """
    model = get_model()
    # BGE 模型推荐：查询时加指令前缀
    embedding = model.encode(
        text,
        normalize_embeddings=True,
        prompt="为这个句子生成表示以用于检索相关文章："
    )
    return embedding.tolist()


def embed_documents(texts: list[str]) -> list[list[float]]:
    """
    对文档文本批量生成向量（文档专用，加文档前缀）
    """
    model = get_model()
    embedding = model.encode(
        texts,
        normalize_embeddings=True,
        prompt=""  # BGE 文档侧不需要特殊指令
    )
    return [e.tolist() for e in embedding]
