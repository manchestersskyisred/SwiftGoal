package com.swiftgoal.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 懂球帝翻译爬虫返回结果的DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResultDto {
    private String playerNameCn;
    private String teamNameCn;
    // 可以在此添加更多需要的信息, 如国家、位置等
} 