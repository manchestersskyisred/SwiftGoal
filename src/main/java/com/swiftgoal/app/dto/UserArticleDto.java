package com.swiftgoal.app.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserArticleDto {

    @NotEmpty(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @NotEmpty(message = "内容不能为空")
    private String content;

    @Size(max = 100, message = "来源长度不能超过100个字符")
    private String source;

    @NotEmpty(message = "必须选择一个分类")
    private String category;
} 