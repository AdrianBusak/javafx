package hr.algebra.battleship.model.game;

import hr.algebra.battleship.model.enums.CellState;
import hr.algebra.battleship.model.enums.Orientation;
import hr.algebra.battleship.model.ships.Ship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {
    private static final int SIZE = 10;
    private Cell[][] grid;
    private List<Ship> ships;

    public Board() {
        grid = new Cell[SIZE][SIZE];
        ships = new ArrayList<>();
        initializeGrid();
    }

    private void initializeGrid() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
    }

    /**
     * Postavi brod na ploču
     *
     * @param ship brod koji se postavlja
     * @param startX početna X koordinata
     * @param startY početna Y koordinata
     * @param orientation orijentacija (HORIZONTAL ili VERTICAL)
     * @return true ako je uspješno postavljeno, false inače
     */
    public boolean placeShip(Ship ship, int startX, int startY,
                             Orientation orientation) {

        // ✅ Validiraj sve - isValidPlacement() provjerava sve
        if (!isValidPlacement(ship, startX, startY, orientation)) {
            return false;
        }

        // Kreiraj listu ćelija
        List<Cell> cellsToOccupy = new ArrayList<>();
        int size = ship.getType().getSize();

        for (int i = 0; i < size; i++) {
            int x, y;

            if (orientation == Orientation.HORIZONTAL) {
                x = startX;
                y = startY + i;
            } else {  // VERTICAL
                x = startX + i;
                y = startY;
            }

            Cell cell = grid[x][y];
            cellsToOccupy.add(cell);
            cell.setState(CellState.SHIP);  // Označi da sadrži brod
            cell.setShip(ship);
        }

        // Spremi ćelije u brod
        ship.setCells(cellsToOccupy);  // ✅ Koristim setCells umjesto setOccupiedCells
        ships.add(ship);

        return true;
    }

    /**
     * Provjeri je li postavljanje broda validno
     * Provjerava: granice, preklapanja i susjedne brodove
     */
    private boolean isValidPlacement(Ship ship, int startX, int startY,
                                     Orientation orientation) {
        int size = ship.getType().getSize();

        // ✅ Provjera 1: Negativne koordinate
        if (startX < 0 || startY < 0) {
            return false;
        }

        // Iterira kroz sve ćelije broda
        for (int i = 0; i < size; i++) {
            int cellX, cellY;

            if (orientation == Orientation.HORIZONTAL) {
                cellX = startX;
                cellY = startY + i;
            } else {  // VERTICAL
                cellX = startX + i;
                cellY = startY;
            }

            // ✅ Provjera 2: Izvan granica
            if (cellX >= SIZE || cellY >= SIZE) {
                return false;
            }

            // ✅ Provjera 3: Preklapanje s drugim brodom
            if (grid[cellX][cellY].getState() == CellState.SHIP) {
                return false;
            }
        }

        // ✅ Provjera 4: Provjeri susjedne ćelije (bez dodira s drugim brodovima)
        for (int i = -1; i <= size; i++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int checkX, checkY;

                    if (orientation == Orientation.HORIZONTAL) {
                        checkX = startX + dx;
                        checkY = startY + i + dy;
                    } else {  // VERTICAL
                        checkX = startX + i + dx;
                        checkY = startY + dy;
                    }

                    // Provjeri je li u granicama
                    if (checkX >= 0 && checkX < SIZE &&
                            checkY >= 0 && checkY < SIZE) {

                        // Ako nije dio samog broda, provjeri nema li drugog broda
                        if (orientation == Orientation.HORIZONTAL) {
                            if (i < 0 || i >= size) {
                                if (grid[checkX][checkY].getState() == CellState.SHIP) {
                                    return false;
                                }
                            }
                        } else {  // VERTICAL
                            if (i < 0 || i >= size) {
                                if (grid[checkX][checkY].getState() == CellState.SHIP) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Dohvati ćeliju na određenim koordinatama
     *
     * @param x X koordinata
     * @param y Y koordinata
     * @return ćelija na zadatim koordinatama
     */
    public Cell getCell(int x, int y) {
        // ✅ Provjera granica
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IndexOutOfBoundsException(
                    "Koordinate moraju biti između 0 i " + (SIZE - 1)
            );
        }
        return grid[x][y];
    }

    /**
     * Dohvati neumodifiljivu listu svih brodova
     *
     * @return unmodifiable list od brodova
     */
    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);  // ✅ Zaštita
    }

    /**
     * Provjeri je li svi brodovi potopljeni (pobjeda)
     *
     * @return true ako su svi brodovi potopljeni
     */
    public boolean areAllShipsSunk() {
        return ships.stream()
                .allMatch(Ship::checkIfSunk);
    }

    /**
     * Dohvati veličinu ploče
     *
     * @return veličina ploče (10 za standardnu igru)
     */
    public static int getSize() {
        return SIZE;
    }
}
