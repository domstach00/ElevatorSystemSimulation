package org.example.system;

import org.example.model.Direction;
import org.example.model.Elevator;

public interface ElevatorSystem {
    void step();
    void status();
    void status(Elevator elevator);
    Elevator pickup(int pickupFloor, Direction direction);
    Elevator pickup(int pickupFloor, Direction direction, int targetFloor);
    void selectFloor(Elevator elevator, int floor);
    boolean updateElevator(int id, Integer currentFloor, int[] targetFloors);
}
