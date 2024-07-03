package org.example.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Elevator {
    private final static AtomicInteger nextId = new AtomicInteger(1);

    private final int id;
    private int currentFloor;
    private Direction currentDirection;

    /**
     * Target floor for current direction
     */
    private Set<Integer> targetFloors;

    /**
     * Used when elevator is currently moving in different direction then called require but is called.
     */
    private Set<Integer> targetFloorsOtherDirection;

    public Elevator() {
        id = nextId.getAndIncrement();
        currentDirection = Direction.IDLE;
        targetFloors = new HashSet<>();
    }

    public void addTargetFloor(int floor) {
        if (targetFloors == null) {
            targetFloors = new HashSet<>();
        }
        targetFloors.add(floor);
    }

    public void addTargetFloorInOtherDirection(int floor) {
        if (targetFloorsOtherDirection == null) {
            targetFloorsOtherDirection = new HashSet<>();
        }
        targetFloorsOtherDirection.add(floor);
    }

    public void removeTargetFloor(int floor) {
        if (targetFloors != null) {
           targetFloors.remove(floor);
        }
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public Set<Integer> getTargetFloors() {
        return targetFloors;
    }

    public void setTargetFloors(Set<Integer> targetFloors) {
        this.targetFloors = targetFloors;
    }

    public Set<Integer> getTargetFloorsOtherDirection() {
        return targetFloorsOtherDirection;
    }

    public void setTargetFloorsOtherDirection(Set<Integer> targetFloorsOtherDirection) {
        this.targetFloorsOtherDirection = targetFloorsOtherDirection;
    }
}
