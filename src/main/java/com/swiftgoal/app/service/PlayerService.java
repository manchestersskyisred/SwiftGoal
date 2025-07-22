package com.swiftgoal.app.service;

import com.swiftgoal.app.repository.entity.Player;
import com.swiftgoal.app.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public List<Player> findAllPlayers() {
        return playerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Player> findPlayerById(Long id) {
        return playerRepository.findById(id);
    }

    @Transactional
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Transactional
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Player> searchPlayers(String name) {
        return playerRepository.findByfullNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public Page<Player> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        return playerRepository.findAll(pageable);
    }
}