package com.strategygamev2.strategygamev2.Repository;

import com.strategygamev2.strategygamev2.Model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
