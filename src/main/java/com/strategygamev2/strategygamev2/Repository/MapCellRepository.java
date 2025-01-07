package com.strategygamev2.strategygamev2.Repository;

import com.strategygamev2.strategygamev2.Model.MapCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MapCellRepository extends JpaRepository<MapCell, Long> {
    @Query("SELECT m FROM MapCell m WHERE m.x = :x AND m.y = :y")
    MapCell findByXAndY(@Param("x") int x, @Param("y") int y);
}
