package hr.algebra.battleship.model.ships;

import hr.algebra.battleship.model.enums.CellState;
import hr.algebra.battleship.model.enums.Orientation;
import hr.algebra.battleship.model.enums.ShipType;
import hr.algebra.battleship.model.game.Cell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Ship implements Serializable {
    private static final long serialVersionUID = 1L;

    protected ShipType type;
    protected Orientation orientation;
    protected List<Cell> cells;
    protected boolean isSunk;

    public Ship() {
        this.cells = new ArrayList<>();
        this.isSunk = false;
    }

    // ✅ GETTER samo - type se ne može mijenjati nakon kreiranja
    public ShipType getType() {
        return type;
    }

    // Orientation getter
    public Orientation getOrientation() {
        return orientation;
    }

    // ✅ Setter s validacijom
    public void setOrientation(Orientation orientation) {
        if (orientation != null) {
            this.orientation = orientation;
        }
    }

    // ✅ getCells() - vraća kopiju da se ne može vanjski promijeniti
    public List<Cell> getCells() {
        return new ArrayList<>(cells);
    }

    // ✅ setCells() - jedina metoda za postavljanje ćelija
    public void setCells(List<Cell> cells) {
        if (cells != null) {
            this.cells = new ArrayList<>(cells);
        }
    }

    // ✅ Alias ako trebam drugačije ime
    public void setOccupiedCells(List<Cell> cellsToOccupy) {
        setCells(cellsToOccupy);
    }

    // ✅ Vraća Array ako trebam
    public Cell[] getOccupiedCells() {
        return cells.toArray(new Cell[0]);
    }

    // ✅ Getter za isSunk
    public boolean isSunk() {
        return isSunk;
    }

    // ✅ Setter za isSunk
    public void setSunk(boolean sunk) {
        isSunk = sunk;
    }

    // ✅ ZAPRAVO provjerava je li brod potopljen
    public boolean checkIfSunk() {
        // Provjeri jesu li sve ćelije pogođene
        boolean allHit = cells.stream()
                .allMatch(cell -> cell.getState() == CellState.HIT);

        // Ako su sve pogođene, označi kao potopljen
        if (allHit) {
            this.isSunk = true;
        }

        return this.isSunk;
    }

    // ✅ Dodatna metoda - koliko ćelija preostaje
    public int getHealthPoints() {
        return (int) cells.stream()
                .filter(cell -> cell.getState() != CellState.HIT)
                .count();
    }

    // ✅ Dodatna metoda - koliko ćelija je pogođeno
    public int getHitCount() {
        return (int) cells.stream()
                .filter(cell -> cell.getState() == CellState.HIT)
                .count();
    }
}
