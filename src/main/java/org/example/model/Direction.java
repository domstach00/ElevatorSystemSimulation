package org.example.model;

public enum Direction {
    UP(1),
    IDLE(0),
    DOWN(-1);

    private final int directionValue;

    Direction(int directionValue) {
        this.directionValue = directionValue;
    }

    public int mapDirectionToValue() {
        return directionValue;
    }

    public static Direction getDirectionByFloors(int startFloor, int targetFloor) {
        final int valueDirection = Integer.compare(targetFloor, startFloor);
        return fromValue(valueDirection);
    }

    public static Direction fromValue(int value) {
        for (Direction direction : values()) {
            if (direction.directionValue == value) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Invalid direction value");
    }
}
