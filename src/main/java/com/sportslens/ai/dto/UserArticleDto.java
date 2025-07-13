package com.sportslens.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserArticleDto {

    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 200, message = "标题长度必须在5-200个字符之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 50, max = 10000, message = "内容长度必须在50-10000个字符之间")
    private String content;

    @Size(max = 100, message = "来源长度不能超过100个字符")
    private String source;

    // Constructors
    public UserArticleDto() {}

    public UserArticleDto(String title, String content, String source) {
        this.title = title;
        this.content = content;
        this.source = source;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "UserArticleDto{" +
                "title='" + title + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 50)) + "..." : null) + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}