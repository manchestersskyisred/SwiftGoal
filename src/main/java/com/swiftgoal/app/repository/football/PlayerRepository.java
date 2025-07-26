package com.swiftgoal.app.repository.football;

import com.swiftgoal.app.repository.entity.football.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findByApiPlayerId(Integer apiPlayerId);
    
    List<Player> findByTeam_Id(Long teamId);

    // 新的多语言搜索方法 - 支持中英文名搜索
    @Query("SELECT p FROM Player p WHERE " +
           "LOWER(p.fullNameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.fullNameCn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.knownAs) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Player> searchPlayersByName(@Param("query") String query);
    
    // 向后兼容的搜索方法（使用原有的fullName字段）
    @Query("SELECT p FROM Player p WHERE LOWER(p.fullNameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.knownAs) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Player> searchPlayersByNameLegacy(@Param("query") String query);
    
    // 多语言国籍搜索
    @Query("SELECT p FROM Player p WHERE LOWER(p.nationalityEn) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.nationalityCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Player> searchPlayersByNationality(@Param("query") String query);
    
    // 多语言位置搜索
    @Query("SELECT p FROM Player p WHERE LOWER(p.positionEn) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.positionCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Player> searchPlayersByPosition(@Param("query") String query);
    
    // 全字段多语言搜索
    @Query("SELECT p FROM Player p WHERE " +
           "LOWER(p.fullNameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.fullNameCn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.knownAs) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.nationalityEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.nationalityCn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.positionEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.positionCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Player> searchPlayersByAnyField(@Param("query") String query);
} 