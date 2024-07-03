package org.example.factory;

import org.example.model.Direction;
import org.example.model.Elevator;

import java.util.HashSet;

public class ElevatorFactory {

    public static Elevator createElevator() {
        Elevator elevator = new Elevator();
        elevator.setCurrentFloor(0);
        elevator.setCurrentDirection(Direction.IDLE);
        elevator.setTargetFloors(new HashSet<>());
        elevator.setTargetFloorsOtherDirection(new HashSet<>());
        return elevator;
    }

    public static Elevator createElevator(int initialFloor) {
        Elevator elevator = createElevator();
        elevator.setCurrentFloor(initialFloor);
        return elevator;
    }

    public static Elevator createElevator(int initialFloor, int initialTargetFloor) {
        Elevator elevator = createElevator();
        elevator.setCurrentFloor(initialFloor);
        elevator.addTargetFloor(initialTargetFloor);
        elevator.setCurrentDirection(Direction.getDirectionByFloors(initialFloor, initialTargetFloor));
        return elevator;
    }

    public static Elevator createElevator(int initialFloor, int initialTargetFloor, int initialTargetFloorOtherDir) {
        Elevator elevator = createElevator();
        elevator.setCurrentFloor(initialFloor);
        elevator.addTargetFloor(initialTargetFloor);
        elevator.setCurrentDirection(Direction.getDirectionByFloors(initialFloor, initialTargetFloor));
        elevator.addTargetFloorInOtherDirection(initialTargetFloorOtherDir);
        return elevator;
    }
}
