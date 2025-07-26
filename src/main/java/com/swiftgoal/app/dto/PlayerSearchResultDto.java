package com.swiftgoal.app.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerSearchResultDto {
    private Long id;
    private String displayName;
    private String fullNameEn;
    private String fullNameCn;
    private String nationalityEn;
    private String nationalityCn;
    private String positionEn;
    private String positionCn;
    private String dateOfBirth;
    private String photoUrl;
    
    // 用于构建显示名称的辅助方法
    public String getDisplayName() {
        if (fullNameCn != null && !fullNameCn.trim().isEmpty()) {
            return fullNameCn + " (" + fullNameEn + ")";
        }
        return fullNameEn;
    }

    public String getDisplayNationality() {
        if (nationalityCn != null && !nationalityCn.trim().isEmpty()) {
            return nationalityCn + " (" + nationalityEn + ")";
        }
        return nationalityEn;
    }

    public String getDisplayPosition() {
        if (positionCn != null && !positionCn.trim().isEmpty()) {
            return positionCn + " (" + positionEn + ")";
        }
        return positionEn;
    }
} 