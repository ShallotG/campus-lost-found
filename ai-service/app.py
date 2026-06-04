"""
Flask YOLO 物品检测微服务
提供 POST /detect 端点，接收图片返回物品类别
"""
import logging
from flask import Flask, request, jsonify
from detector import detect_category
from embedder import embed_query, embed_documents
from config import PORT

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(name)s: %(message)s'
)
logger = logging.getLogger(__name__)

app = Flask(__name__)


@app.route('/health', methods=['GET'])
def health():
    """健康检查"""
    return jsonify({"status": "ok", "service": "yolo-detector"})


@app.route('/detect', methods=['POST'])
def detect():
    """
    物品检测端点

    接收: multipart/form-data, file字段包含图片
    返回: { "category": "backpack", "category_cn": "书包", "confidence": 0.95 }
    """
    # 校验文件
    if 'file' not in request.files:
        return jsonify({"error": "缺少file字段"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "文件名为空"}), 400

    # 校验文件类型
    allowed_extensions = {'jpg', 'jpeg', 'png', 'bmp', 'webp'}
    ext = file.filename.rsplit('.', 1)[-1].lower() if '.' in file.filename else ''
    if ext not in allowed_extensions:
        return jsonify({"error": f"不支持的文件类型: {ext}"}), 400

    try:
        # 读取图片二进制数据
        image_bytes = file.read()

        # 执行检测
        result = detect_category(image_bytes)

        return jsonify(result)

    except Exception as e:
        logger.error(f"检测失败: {e}", exc_info=True)
        return jsonify({
            "category": "unknown",
            "category_cn": "未知物品",
            "confidence": 0.0,
            "error": str(e)
        }), 500


@app.route('/embed', methods=['POST'])
def embed():
    """
    文本向量化端点

    请求体 JSON:
      - 单文本:   { "text": "白色水杯 存放位置:A柜台" }
      - 多文本:   { "texts": ["文本1", "文本2"] }
      - 查询模式: { "text": "...", "mode": "query" }

    返回:
      { "embeddings": [[0.1, -0.2, ...], ...], "dimension": 384 }
    """
    data = request.get_json(silent=True)
    if not data:
        return jsonify({"error": "请求体需为JSON"}), 400

    try:
        # 批量文本
        if "texts" in data:
            texts = data["texts"]
            if not isinstance(texts, list) or len(texts) == 0:
                return jsonify({"error": "texts 需为非空数组"}), 400
            embeddings = embed_documents(texts)

        # 单文本
        elif "text" in data:
            text = data["text"]
            mode = data.get("mode", "document")
            if mode == "query":
                embeddings = [embed_query(text)]
            else:
                embeddings = embed_documents([text])

        else:
            return jsonify({"error": "缺少 text 或 texts 字段"}), 400

        dimension = len(embeddings[0]) if embeddings else 0
        return jsonify({
            "embeddings": embeddings,
            "dimension": dimension
        })

    except Exception as e:
        logger.error(f"Embedding 失败: {e}", exc_info=True)
        return jsonify({"error": str(e)}), 500


@app.errorhandler(413)
def too_large(e):
    return jsonify({"error": "图片文件过大，最大支持10MB"}), 413


if __name__ == '__main__':
    logger.info(f"YOLO检测服务启动在端口 {PORT}")
    app.run(host='0.0.0.0', port=PORT, debug=False)
