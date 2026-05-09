"""
文档加载与切分模块
支持 PDF、TXT、Markdown 等格式
"""
import re
from typing import List


class TextSplitter:
    """文本切分器"""

    def __init__(self, chunk_size: int = 500, chunk_overlap: int = 50):
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap

    def split(self, text: str) -> List[str]:
        """按段落和句子切分文本"""
        # 先按段落切分
        paragraphs = [p.strip() for p in text.split('\n\n') if p.strip()]
        chunks = []

        for para in paragraphs:
            if len(para) <= self.chunk_size:
                chunks.append(para)
            else:
                # 按句子切分长段落
                sentences = re.split(r'(?<=[。！？.!?])\s*', para)
                current_chunk = ""
                for sent in sentences:
                    if len(current_chunk) + len(sent) <= self.chunk_size:
                        current_chunk += sent
                    else:
                        if current_chunk:
                            chunks.append(current_chunk.strip())
                        # 保留重叠部分
                        overlap_text = current_chunk[-self.chunk_overlap:] if len(current_chunk) > self.chunk_overlap else current_chunk
                        current_chunk = overlap_text + sent
                if current_chunk.strip():
                    chunks.append(current_chunk.strip())

        return [c for c in chunks if len(c) > 10]


class DocumentLoader:
    """文档加载器"""

    @staticmethod
    def load_txt(file_path: str) -> str:
        with open(file_path, 'r', encoding='utf-8') as f:
            return f.read()

    @staticmethod
    def load_md(file_path: str) -> str:
        return DocumentLoader.load_txt(file_path)

    @staticmethod
    def load(file_path: str) -> str:
        ext = file_path.rsplit('.', 1)[-1].lower()
        if ext in ('txt', 'md', 'markdown'):
            return DocumentLoader.load_txt(file_path)
        raise ValueError(f"Unsupported file type: {ext}")


class Retriever:
    """简易检索器（不依赖外部向量数据库的本地实现）"""

    def __init__(self):
        self.documents: List[dict] = []  # [{id, title, content, category, tags, chunks: [text]}]
        self.splitter = TextSplitter()

    def add_document(self, doc_id: str, title: str, content: str,
                     category: str = "", tags: str = ""):
        chunks = self.splitter.split(content)
        self.documents.append({
            "id": doc_id,
            "title": title,
            "content": content,
            "category": category,
            "tags": tags,
            "chunks": chunks,
        })

    def search(self, query: str, category: str = None, top_k: int = 5) -> List[dict]:
        """基于关键词的简单检索"""
        query_terms = set(query.lower().split())
        results = []

        for doc in self.documents:
            if category and doc["category"] != category:
                continue

            for chunk in doc["chunks"]:
                chunk_lower = chunk.lower()
                score = sum(1 for term in query_terms if term in chunk_lower)
                if score > 0:
                    results.append({
                        "doc_id": doc["id"],
                        "title": doc["title"],
                        "category": doc["category"],
                        "chunk": chunk,
                        "score": score,
                    })

        results.sort(key=lambda x: x["score"], reverse=True)
        return results[:top_k]

    def clear(self):
        self.documents.clear()


# 全局单例
retriever = Retriever()
