package com.swiftgoal.app.repository.football;

import com.swiftgoal.app.repository.entity.football.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    Optional<Team> findByApiTeamId(Integer apiTeamId);
    Optional<Team> findByNameEn(String nameEn);
    Optional<Team> findByNameCn(String nameCn);
    
    // 多语言球队名搜索
    @Query("SELECT t FROM Team t WHERE " +
           "LOWER(t.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.nameCn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.shortNameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.shortNameCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Team> searchTeamsByName(@Param("query") String query);
    
    // 多语言国家搜索
    @Query("SELECT t FROM Team t WHERE LOWER(t.countryEn) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.countryCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Team> searchTeamsByCountry(@Param("query") String query);
    
    // 多语言球场搜索
    @Query("SELECT t FROM Team t WHERE LOWER(t.stadiumNameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.stadiumNameCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Team> searchTeamsByStadium(@Param("query") String query);
    
    // 全字段多语言搜索
    @Query("SELECT t FROM Team t WHERE " +
           "LOWER(t.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.nameCn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.shortNameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.shortNameCn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.countryEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.countryCn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.stadiumNameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.stadiumNameCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Team> searchTeamsByAnyField(@Param("query") String query);
} 