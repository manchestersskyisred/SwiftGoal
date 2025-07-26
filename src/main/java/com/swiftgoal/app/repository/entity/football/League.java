package com.swiftgoal.app.repository.entity.football;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "leagues", indexes = {
    @Index(name = "idx_leagues_name_en", columnList = "name_en"),
    @Index(name = "idx_leagues_name_cn", columnList = "name_cn")
})
public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_league_id", unique = true)
    private Integer apiLeagueId;

    // 英文名称
    @Column(name = "name_en", nullable = false, unique = true)
    private String nameEn;

    // 中文名称
    @Column(name = "name_cn", unique = true)
    private String nameCn;

    // 英文国家名
    @Column(name = "country_en", nullable = false)
    private String countryEn;

    // 中文国家名
    @Column(name = "country_cn")
    private String countryCn;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    // 为了向后兼容，保留原有的getter方法
    public String getName() {
        return nameEn;
    }

    public String getCountry() {
        return countryEn;
    }

    // 获取显示名称的辅助方法
    public String getDisplayName() {
        if (nameCn != null && !nameCn.trim().isEmpty()) {
            return nameCn + " (" + nameEn + ")";
        }
        return nameEn;
    }

    public String getDisplayCountry() {
        if (countryCn != null && !countryCn.trim().isEmpty()) {
            return countryCn + " (" + countryEn + ")";
        }
        return countryEn;
    }
} 