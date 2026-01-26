package hr.algebra.battleship.model.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Predstavlja jedan potez u igri BattleShip
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMove implements Serializable {
    private static final long serialVersionUID = 1L;

    private int row;
    private int column;
    private String result; // HIT, MISS, SINK
    private String playerSymbol; // X, O
    private long timestamp;
}