package com.strategygamev2.strategygamev2.Repository;

import com.strategygamev2.strategygamev2.Model.MapCell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MapCellRepository extends JpaRepository<MapCell, Long> {
    MapCell findByXAndY(int x, int y);
}
