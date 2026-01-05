package hr.algebra.battleship.model.ships;

import hr.algebra.battleship.model.enums.ShipType;

public class Carrier extends Ship {
    public Carrier() {
        super();
        this.type = ShipType.CARRIER;
    }
}
