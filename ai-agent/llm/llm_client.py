"""
金蝶苍穹 AI 平台 - 大模型调用封装
"""
import json
import os
from typing import Optional, AsyncGenerator
import httpx


class LLMConfig:
    """LLM 配置"""
    base_url: str = os.getenv("AI_BASE_URL", "https://ai.kingdee.com/api")
    api_key: str = os.getenv("AI_API_KEY", "your-api-key")
    model: str = os.getenv("AI_MODEL", "default")
    max_tokens: int = 2048
    temperature: float = 0.7


class LLMClient:
    """大模型调用客户端"""

    def __init__(self, config: Optional[LLMConfig] = None):
        self.config = config or LLMConfig()

    async def chat(self, messages: list[dict], **kwargs) -> dict:
        """同步对话（非流式）"""
        async with httpx.AsyncClient(timeout=60.0) as client:
            resp = await client.post(
                f"{self.config.base_url}/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {self.config.api_key}",
                    "Content-Type": "application/json",
                },
                json={
                    "model": self.config.model,
                    "messages": messages,
                    "max_tokens": kwargs.get("max_tokens", self.config.max_tokens),
                    "temperature": kwargs.get("temperature", self.config.temperature),
                },
            )
            resp.raise_for_status()
            return resp.json()

    async def chat_stream(self, messages: list[dict], **kwargs) -> AsyncGenerator[str, None]:
        """流式对话"""
        async with httpx.AsyncClient(timeout=120.0) as client:
            async with client.stream(
                "POST",
                f"{self.config.base_url}/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {self.config.api_key}",
                    "Content-Type": "application/json",
                },
                json={
                    "model": self.config.model,
                    "messages": messages,
                    "max_tokens": kwargs.get("max_tokens", self.config.max_tokens),
                    "temperature": kwargs.get("temperature", self.config.temperature),
                    "stream": True,
                },
            ) as resp:
                resp.raise_for_status()
                async for line in resp.aiter_lines():
                    if line.startswith("data: ") and line != "data: [DONE]":
                        chunk = json.loads(line[6:])
                        delta = chunk.get("choices", [{}])[0].get("delta", {})
                        if content := delta.get("content"):
                            yield content


# 全局单例
llm_client = LLMClient()
