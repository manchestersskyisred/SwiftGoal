package com.swiftgoal.app.repository.entity.football;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "players", indexes = {
    @Index(name = "idx_players_full_name_en", columnList = "full_name_en"),
    @Index(name = "idx_players_full_name_cn", columnList = "full_name_cn"),
    @Index(name = "idx_players_known_as", columnList = "known_as"),
    @Index(name = "idx_players_nationality_en", columnList = "nationality_en"),
    @Index(name = "idx_players_nationality_cn", columnList = "nationality_cn"),
    @Index(name = "idx_players_position_en", columnList = "position_en"),
    @Index(name = "idx_players_position_cn", columnList = "position_cn")
})
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "api_player_id", unique = true)
    private Integer apiPlayerId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    // 英文全名，作为主要标识符
    @Column(name = "full_name_en", nullable = false)
    private String fullNameEn;

    // 中文全名
    @Column(name = "full_name_cn")
    private String fullNameCn;

    // 常用名/昵称，可以是任何语言
    @Column(name = "known_as")
    private String knownAs;

    // 英文国籍
    @Column(name = "nationality_en", nullable = false)
    private String nationalityEn;

    // 中文国籍
    @Column(name = "nationality_cn")
    private String nationalityCn;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    // 英文位置
    @Column(name = "position_en")
    private String positionEn;

    // 中文位置
    @Column(name = "position_cn")
    private String positionCn;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "photo_url", length = 1024)
    private String photoUrl;

    // 为了向后兼容，保留原有的getter方法
    public String getFullName() {
        return fullNameEn;
    }

    public String getNationality() {
        return nationalityEn;
    }

    public String getPosition() {
        return positionEn;
    }

    // 获取显示名称的辅助方法
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