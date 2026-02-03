module hr.algebra.battleship {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.naming;
    requires static lombok;
    requires java.management;
    requires java.rmi;

    opens hr.algebra.battleship.controller to javafx.fxml;

    exports hr.algebra.battleship.controller;
    exports hr.algebra.battleship.gameEngine;
    exports hr.algebra.battleship.model.game;
    exports hr.algebra.battleship.model.ships;
    exports hr.algebra.battleship.model.enums;
    exports hr.algebra.battleship.services;
    exports hr.algebra.battleship.rmi;
    exports hr.algebra.battleship;
    opens hr.algebra.battleship to javafx.fxml;

}
