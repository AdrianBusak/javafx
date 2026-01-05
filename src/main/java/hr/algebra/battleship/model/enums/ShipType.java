package hr.algebra.battleship.model.enums;

public enum ShipType {
    CARRIER(5),
    BATTLESHIP(4),
    CRUISER(3),
    SUBMARINE(3),
    DESTROYER(2);

    private final int value;
    private ShipType(int value) {
        this.value = value;
    }

    public int getSize() {
        return value;
    }
}
