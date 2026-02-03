package hr.algebra.battleship.services;

import hr.algebra.battleship.model.enums.AttackResult;
import hr.algebra.battleship.model.game.Cell;
import hr.algebra.battleship.model.game.Player;
import hr.algebra.battleship.model.ships.Ship;
import javafx.animation.*;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class CellVisualizationService {

    private BoardUIService boardUIService;

    public CellVisualizationService(BoardUIService boardUIService) {
        this.boardUIService = boardUIService;
    }


    public void animateAttack(Rectangle cell, AttackResult result) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), cell);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.3);
        scale.setToY(1.3);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        RotateTransition rotate = new RotateTransition(Duration.millis(200), cell);
        rotate.setByAngle(15);
        rotate.setAutoReverse(true);
        rotate.setCycleCount(2);

        ParallelTransition parallel = new ParallelTransition(scale, rotate);
        parallel.play();
    }
}
