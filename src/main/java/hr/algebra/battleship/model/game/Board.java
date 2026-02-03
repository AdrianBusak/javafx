package hr.algebra.battleship.model.game;

import hr.algebra.battleship.model.enums.CellState;
import hr.algebra.battleship.model.enums.Orientation;
import hr.algebra.battleship.model.ships.Ship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board implements Serializable {
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

    public boolean placeShip(Ship ship, int startX, int startY,
                             Orientation orientation) {

        if (!isValidPlacement(ship, startX, startY, orientation)) {
            return false;
        }

        List<Cell> cellsToOccupy = new ArrayList<>();
        int size = ship.getType().getSize();

        for (int i = 0; i < size; i++) {
            int x, y;

            if (orientation == Orientation.HORIZONTAL) {
                x = startX;
                y = startY + i;
            } else {
                x = startX + i;
                y = startY;
            }

            Cell cell = grid[x][y];
            cellsToOccupy.add(cell);
            cell.setState(CellState.SHIP);
            cell.setShip(ship);
        }

        ship.setCells(cellsToOccupy);
        ships.add(ship);

        return true;
    }


    private boolean isValidPlacement(Ship ship, int startX, int startY,
                                     Orientation orientation) {
        int size = ship.getType().getSize();

        if (startX < 0 || startY < 0) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            int cellX, cellY;

            if (orientation == Orientation.HORIZONTAL) {
                cellX = startX;
                cellY = startY + i;
            } else {  // VERTICAL
                cellX = startX + i;
                cellY = startY;
            }

            if (cellX >= SIZE || cellY >= SIZE) {
                return false;
            }

            if (grid[cellX][cellY].getState() == CellState.SHIP) {
                return false;
            }
        }

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


    public Cell getCell(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IndexOutOfBoundsException(
                    "Koordinate moraju biti između 0 i " + (SIZE - 1)
            );
        }
        return grid[x][y];
    }

    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

}
