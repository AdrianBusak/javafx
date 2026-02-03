package hr.algebra.battleship.model.game;

import hr.algebra.battleship.model.enums.AttackResult;
import hr.algebra.battleship.model.enums.CellState;
import hr.algebra.battleship.model.ships.Ship;

import java.io.Serializable;

public class Cell implements Serializable {
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

    public int getX() { return x; }
    public int getY() { return y; }
    public CellState getState() { return state; }
    public Ship getShip() { return ship; }

    public void setState(CellState state) { this.state = state; }
    public void setShip(Ship ship) { this.ship = ship; }

    public void markAsHit() {
        if (this.ship != null) {
            this.state = CellState.HIT;
        }
    }

    public void markAsMiss() {
        this.state = CellState.MISS;
    }

    public AttackResult attack() {
        if (state == CellState.HIT || state == CellState.MISS) {
            return AttackResult.ALREADY_ATTACKED;
        }

        if (ship != null) {
            markAsHit();

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

