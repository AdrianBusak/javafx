package hr.algebra.battleship.services;

import hr.algebra.battleship.model.enums.CellState;
import hr.algebra.battleship.model.game.Cell;
import hr.algebra.battleship.model.game.Player;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

/**
 * Servis za upravljanje Board UI-jem
 */
public class BoardUIService {

    /**
     * Kreiraj 10x10 grid
     */
    public void createBoardGrid(GridPane gridPane, boolean isOpponent,
                                CellClickHandler clickHandler) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Rectangle cell = createCell(row, col, isOpponent, clickHandler);
                GridPane.setRowIndex(cell, row);
                GridPane.setColumnIndex(cell, col);
                GridPane.setHalignment(cell, HPos.CENTER);
                GridPane.setValignment(cell, VPos.CENTER);
                gridPane.getChildren().add(cell);
            }
        }
    }

    /**
     * Kreiraj jednu ćeliju
     */
    private Rectangle createCell(int row, int col, boolean isOpponent,
                                 CellClickHandler clickHandler) {
        Rectangle cell = new Rectangle(35, 35);
        cell.getStyleClass().add("cell-rectangle");
        cell.setUserData(new CellData(row, col, isOpponent));

        // Hover
        cell.setOnMouseEntered(e -> cell.getStyleClass().add("cell-hover"));
        cell.setOnMouseExited(e -> cell.getStyleClass().remove("cell-hover"));

        // Click
        cell.setOnMouseClicked(e -> clickHandler.onCellClick(cell, row, col, isOpponent));

        return cell;
    }

    /**
     * Ažuriraj vizualni prikaz ćelije
     */
    public void updateCellStyle(Rectangle cell, CellState state) {
        cell.getStyleClass().clear();
        cell.getStyleClass().add("cell-rectangle");

        switch (state) {
            case SHIP -> cell.getStyleClass().add("cell-ship");
            case HIT -> cell.getStyleClass().add("cell-hit");
            case MISS -> cell.getStyleClass().add("cell-miss");
            case EMPTY -> cell.getStyleClass().add("cell-empty");
        }
    }

    /**
     * Ažuriraj sve ćelije boarda
     */
    public void updateBoardVisuals(GridPane gridPane, Player player, boolean hideShips) {
        for (var child : gridPane.getChildren()) {
            if (child instanceof Rectangle rect) {
                Integer row = GridPane.getRowIndex(rect);
                Integer col = GridPane.getColumnIndex(rect);

                if (row != null && col != null) {
                    Cell cell = player.getBoard().getCell(row, col);

                    if (hideShips && cell.getState() == CellState.SHIP) {
                        continue;
                    }

                    updateCellStyle(rect, cell.getState());
                }
            }
        }
    }

    /**
     * Očisti sve stilove
     */
    public void clearBoardStyles(GridPane grid) {
        for (var child : grid.getChildren()) {
            if (child instanceof Rectangle rect) {
                rect.getStyleClass().clear();
                rect.getStyleClass().add("cell-rectangle");
            }
        }
    }

    /**
     * Nađi Rectangle u GridPane
     */
    public Rectangle findCellInGrid(GridPane grid, int row, int col) {
        for (var child : grid.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(child);
            Integer nodeCol = GridPane.getColumnIndex(child);

            if (nodeRow != null && nodeCol != null && nodeRow == row && nodeCol == col) {
                return (Rectangle) child;
            }
        }
        return null;
    }

    /**
     * Callback za klik na ćeliju
     */
    public interface CellClickHandler {
        void onCellClick(Rectangle cell, int row, int col, boolean isOpponent);
    }
}

class CellData {
    public int row, col;
    public boolean isOpponent;

    public CellData(int row, int col, boolean isOpponent) {
        this.row = row;
        this.col = col;
        this.isOpponent = isOpponent;
    }
}
