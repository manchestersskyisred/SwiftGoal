# translation_server.py
import torch
from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForCausalLM
import uvicorn

# --- 1. 初始化和模型加载 ---
print("Starting server setup...")

# 确定运行设备：优先使用 CUDA (NVIDIA GPU)，否则使用 CPU
device = "cuda" if torch.cuda.is_available() else "cpu"
# --- 修改开始 ---
# 原来的代码指向 Hugging Face Hub
# model_name = "Qwen/Qwen2.5-0.5B"

# 修改后的代码指向本地文件夹，实现完全离线加载
# 注意：您需要先手动下载模型文件到这个路径
model_name = "./Qwen2.5-0.5B"
# --- 修改结束 ---

print(f"Using device: {device}")
print(f"Loading model from local path: {model_name}")

# 加载分词器和模型
# 这个过程现在会从您本地的文件夹加载模型
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForCausalLM.from_pretrained(model_name).to(device)

print("Model and tokenizer loaded successfully.")

# 创建 FastAPI 应用实例
app = FastAPI()


# --- 2. 定义 API 的请求和响应格式 ---

# Pydantic 模型用于数据验证，确保API接收到的数据格式正确
class TranslationRequest(BaseModel):
    text: str
    source_lang: str = "Chinese"  # 默认为中文
    target_lang: str = "English"  # 默认为英文


class TranslationResponse(BaseModel):
    translated_text: str


# --- 3. 创建翻译 API 端点 ---

@app.post("/translate/", response_model=TranslationResponse)
def translate(request: TranslationRequest):
    """
    接收翻译请求，调用模型生成翻译结果。
    """
    # 构造专门用于翻译的提示
    # 这是让通用模型执行特定任务的关键
    prompt = f"Translate the following text from {request.source_lang} to {request.target_lang}. \n\n{request.source_lang}: {request.text}\n\n{request.target_lang}:"

    messages = [
        {"role": "user", "content": prompt}
    ]

    # 使用分词器处理输入
    inputs = tokenizer.apply_chat_template(
        messages,
        add_generation_prompt=True,
        tokenize=True,
        return_dict=True,
        return_tensors="pt",
    ).to(device)

    # 模型生成文本
    # max_new_tokens 限制了生成文本的最大长度，防止无限生成
    outputs = model.generate(**inputs, max_new_tokens=256)

    # 解码生成的文本，并跳过输入的提示部分
    start_index = inputs["input_ids"].shape[-1]
    response_text = tokenizer.decode(outputs[0][start_index:], skip_special_tokens=True)

    # 返回翻译结果
    return TranslationResponse(translated_text=response_text.strip())


@app.get("/")
def read_root():
    return {"message": "Qwen Translation Server is running. Use the /translate endpoint for translations."}


# --- 4. 启动服务器 ---

if __name__ == "__main__":
    print("Starting Uvicorn server...")
    # 监听 0.0.0.0 表示接受来自网络中任何地址的请求
    uvicorn.run(app, host="0.0.0.0", port=8000) 