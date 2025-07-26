package com.swiftgoal.app.repository.football;

import com.swiftgoal.app.repository.entity.football.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {
    
    Optional<League> findByApiLeagueId(Integer apiLeagueId);
    Optional<League> findByNameEn(String nameEn);
    Optional<League> findByNameCn(String nameCn);
    
    // 多语言联赛名搜索
    @Query("SELECT l FROM League l WHERE " +
           "LOWER(l.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.nameCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<League> searchLeaguesByName(@Param("query") String query);
    
    // 多语言国家搜索
    @Query("SELECT l FROM League l WHERE LOWER(l.countryEn) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(l.countryCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<League> searchLeaguesByCountry(@Param("query") String query);
    
    // 全字段多语言搜索
    @Query("SELECT l FROM League l WHERE " +
           "LOWER(l.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.nameCn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.countryEn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.countryCn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<League> searchLeaguesByAnyField(@Param("query") String query);
} 