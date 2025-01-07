package com.strategygamev2.strategygamev2.Repository;

import com.strategygamev2.strategygamev2.Model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPlayerName(String playerName);
}
