package org.example.service;

import org.example.model.Direction;
import org.example.model.Elevator;

import java.util.List;
import java.util.Optional;

public interface ElevatorService {
    List<Elevator> getElevators();
    Optional<Elevator> getElevator(int elevatorId);
    void elevatorStep(Elevator elevator);
    Elevator getElevatorFromFloor(int floor);
    Elevator callElevator(int floor, Direction selectedDirection);
    boolean setElevatorCurrentFloor(Elevator elevator, int floor);
    boolean setElevatorTargetFloors(Elevator elevator, int[] targetFloors);
    void selectFloor(Elevator elevator, int floor);
    boolean hasReachedTargetFloor(Elevator elevator);
    void removeCurrentFloorFromTarget(Elevator elevator);
}
