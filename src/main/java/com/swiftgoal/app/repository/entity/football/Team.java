package com.swiftgoal.app.repository.entity.football;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teams", indexes = {
    @Index(name = "idx_teams_name_en", columnList = "name_en"),
    @Index(name = "idx_teams_name_cn", columnList = "name_cn"),
    @Index(name = "idx_teams_short_name_en", columnList = "short_name_en"),
    @Index(name = "idx_teams_short_name_cn", columnList = "short_name_cn")
})
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_team_id", unique = true)
    private Integer apiTeamId;

    // 英文全名
    @Column(name = "name_en", nullable = false, unique = true)
    private String nameEn;

    // 中文全名
    @Column(name = "name_cn", unique = true)
    private String nameCn;

    // 英文简称
    @Column(name = "short_name_en")
    private String shortNameEn;

    // 中文简称
    @Column(name = "short_name_cn")
    private String shortNameCn;

    // 英文国家名
    @Column(name = "country_en", nullable = false)
    private String countryEn;

    // 中文国家名
    @Column(name = "country_cn")
    private String countryCn;

    // 英文球场名
    @Column(name = "stadium_name_en")
    private String stadiumNameEn;

    // 中文球场名
    @Column(name = "stadium_name_cn")
    private String stadiumNameCn;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    // 为了向后兼容，保留原有的getter方法
    public String getName() {
        return nameEn;
    }

    public String getCountry() {
        return countryEn;
    }

    public String getStadiumName() {
        return stadiumNameEn;
    }

    // 获取显示名称的辅助方法
    public String getDisplayName() {
        if (nameCn != null && !nameCn.trim().isEmpty()) {
            return nameCn + " (" + nameEn + ")";
        }
        return nameEn;
    }

    public String getDisplayShortName() {
        if (shortNameCn != null && !shortNameCn.trim().isEmpty()) {
            return shortNameCn + " (" + shortNameEn + ")";
        }
        return shortNameEn != null ? shortNameEn : nameEn;
    }

    public String getDisplayCountry() {
        if (countryCn != null && !countryCn.trim().isEmpty()) {
            return countryCn + " (" + countryEn + ")";
        }
        return countryEn;
    }

    public String getDisplayStadiumName() {
        if (stadiumNameCn != null && !stadiumNameCn.trim().isEmpty()) {
            return stadiumNameCn + " (" + stadiumNameEn + ")";
        }
        return stadiumNameEn;
    }
} 