package hr.algebra.battleship.model.game;

import hr.algebra.battleship.model.enums.AttackResult;
import hr.algebra.battleship.model.enums.CellState;
import hr.algebra.battleship.model.ships.Ship;

public class Cell {
    private int x;
    private int y;
    private CellState state;
    private Ship ship;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.state = CellState.EMPTY;
        this.ship = null;
    }

    // Getteri
    public int getX() { return x; }
    public int getY() { return y; }
    public CellState getState() { return state; }
    public Ship getShip() { return ship; }

    // Setteri
    public void setState(CellState state) { this.state = state; }
    public void setShip(Ship ship) { this.ship = ship; }

    // Akcije
    public void markAsHit() {
        if (this.ship != null) {
            this.state = CellState.HIT;
        }
    }

    public void markAsMiss() {
        this.state = CellState.MISS;
    }

    // Napad na ovu ćeliju
    public AttackResult attack() {
        if (state == CellState.HIT || state == CellState.MISS) {
            return AttackResult.ALREADY_ATTACKED;  // Već napadnuta
        }

        if (ship != null) {
            markAsHit();

            // Provjeri je li brod potopljen
            if (ship.checkIfSunk()) {
                return AttackResult.SUNK;
            }
            return AttackResult.HIT;
        } else {
            markAsMiss();
            return AttackResult.MISS;
        }
    }
}

