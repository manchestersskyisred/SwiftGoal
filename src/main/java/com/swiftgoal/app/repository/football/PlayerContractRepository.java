package com.swiftgoal.app.repository.football;

import com.swiftgoal.app.repository.entity.football.Player;
import com.swiftgoal.app.repository.entity.football.PlayerContract;
import com.swiftgoal.app.repository.entity.football.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PlayerContractRepository extends JpaRepository<PlayerContract, Long> {
    Optional<PlayerContract> findByPlayerAndTeam(Player player, Team team);
    List<PlayerContract> findByPlayer(Player player);

    // 查找身价前N的球员ID
    @Query("SELECT pc.player.id FROM PlayerContract pc WHERE pc.marketValueEur IS NOT NULL ORDER BY pc.marketValueEur DESC")
    List<Long> findTopPlayerIdsByMarketValueDesc(org.springframework.data.domain.Pageable pageable);

    List<PlayerContract> findByPlayerId(Long playerId);

    List<PlayerContract> findByPlayerIdOrderByIdDesc(Long playerId);

    List<PlayerContract> findByMarketValueEurIsNull();

    long countByMarketValueEurIsNotNull();

    @Query("SELECT pc.player.id FROM PlayerContract pc WHERE pc.marketValueEur IS NOT NULL")
    List<Long> findPlayerIdsWithMarketValue();
} 