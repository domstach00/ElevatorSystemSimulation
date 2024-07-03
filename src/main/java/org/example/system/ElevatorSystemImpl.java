package org.example.system;

import org.example.model.Direction;
import org.example.model.Elevator;
import org.example.service.ElevatorService;
import org.example.util.ElevatorStatusSummary;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ElevatorSystemImpl implements ElevatorSystem {
    private final static Logger LOG = Logger.getLogger(ElevatorSystemImpl.class.getName());
    private final ElevatorService elevatorService;

    /**
     * A map representing the floors users want to move to.
     * The Key is the floor where the user calls the elevator.
     * The Value is a set of floors that users from a given floor want to get to.
     */
    private final Map<Integer, Set<Integer>> calledFloorToDestinationFloors;

    public ElevatorSystemImpl(ElevatorService elevatorService) {
        this.elevatorService = elevatorService;
        this.calledFloorToDestinationFloors = new HashMap<>();
    }

    @Override
    public void step() {
        this.elevatorService.getElevators().stream()
                .peek(this.elevatorService::elevatorStep)
                .peek(this.elevatorService::removeCurrentFloorFromTarget)
                .forEach(this::processElevatorCallOnCurrentFloor);

        System.out.println("Elevators have been updated\n");
        System.out.println();
    }

    private void processElevatorCallOnCurrentFloor(Elevator elevator) {
        final int currentFloor = elevator.getCurrentFloor();
        if (isFloorCalled(currentFloor)) {
            Set<Integer> floorsThatMeetsCurrentDirection = getFloorsMeetingCurrentDirection(elevator, currentFloor);
            floorsThatMeetsCurrentDirection.forEach(floor -> this.selectFloor(elevator, floor));
            removeFloorsFromCalledFloors(currentFloor, floorsThatMeetsCurrentDirection);
        }
    }

    private boolean isFloorCalled(int floor) {
        return this.calledFloorToDestinationFloors.containsKey(floor);
    }

    private Set<Integer> getFloorsMeetingCurrentDirection(Elevator elevator, int currentFloor) {
        return this.calledFloorToDestinationFloors.get(currentFloor).stream()
                .filter(floor -> isElevatorMovingToFloor(elevator, floor))
                .collect(Collectors.toSet());
    }

    private void removeFloorsFromCalledFloors(int currentFloor, Set<Integer> floorsToRemove) {
        this.calledFloorToDestinationFloors.get(currentFloor).removeAll(floorsToRemove);
        this.calledFloorToDestinationFloors.get(currentFloor).remove(currentFloor);

        if (this.calledFloorToDestinationFloors.get(currentFloor).isEmpty()) {
            this.calledFloorToDestinationFloors.remove(currentFloor);
        }
    }

    private boolean isElevatorMovingToFloor(Elevator elevator, int floor) {
        return elevator.getCurrentDirection() == Direction.IDLE
                || (elevator.getCurrentDirection() == Direction.UP && floor >= elevator.getCurrentFloor())
                || (elevator.getCurrentDirection() == Direction.DOWN && floor <= elevator.getCurrentFloor());
    }

    /**
     * The `status` method prints (in console) status summary of all elevators as table
     */
    @Override
    public void status() {
        System.out.println("\n");
        System.out.println(
                ElevatorStatusSummary.getSummaryText(this.elevatorService.getElevators())
        );
    }

    @Override
    public void status(Elevator elevator) {
        System.out.println("\n");
        System.out.println(
                ElevatorStatusSummary.getSummaryText(
                        List.of(elevator)
                )
        );
    }

    /**
     * The `pickup` method calls an elevator but do not select the floor that user would like to go.
     * @param pickupFloor The floor from where elevator should pick up user.
     * @param direction The Direction selected when user called an elevator.
     * @return Returns the elevator that will pick up the user. Returns null when calling an elevator is not possible.
     */
    @Override
    public Elevator pickup(int pickupFloor, Direction direction) {
        return this.elevatorService.callElevator(pickupFloor, direction);
    }

    /**
     * The `pickup` method calls an elevator but do not select the floor that user would like to go.
     * This method also specifies where user would like to go (which floor) when the elevator arrives.
     * @param pickupFloor The floor from where elevator should pick up user.
     * @param direction The Direction selected when user called an elevator.
     * @param targetFloor The targetFloor specifies where user wants to go when elevator arrives.
     * @return Returns the elevator that will pick up the user. Returns null when calling an elevator is not possible.
     */
    @Override
    public Elevator pickup(int pickupFloor, Direction direction, int targetFloor) {
        Elevator elevatorToPickUp = this.pickup(pickupFloor, direction);
        if (elevatorToPickUp != null) {
            if (this.calledFloorToDestinationFloors.containsKey(pickupFloor)) {
                Set<Integer> newTargetValue = this.calledFloorToDestinationFloors.get(pickupFloor);
                newTargetValue.add(targetFloor);

                this.calledFloorToDestinationFloors.put(pickupFloor, newTargetValue);
            } else {
                Set<Integer> targetValue = new HashSet<>();
                targetValue.add(targetFloor);
                this.calledFloorToDestinationFloors.put(pickupFloor, targetValue);
            }
        }
        return elevatorToPickUp;
    }

    @Override
    public void selectFloor(Elevator elevator, int floor) {
        this.elevatorService.selectFloor(elevator, floor);
    }

    /**
     * The `updateElevator` methods updates the properties of the selected elevator
     * @param id The id of elevator that you want to update.
     * @param updatedCurrentFloor The updated current floor, if you do not want to update then pass null.
     * @param updatedTargetFloors The list of target floors, if you do not want to update then pass null.
     */
    @Override
    public boolean updateElevator(int id, Integer updatedCurrentFloor, int[] updatedTargetFloors) {
        Elevator elevatorToUpdate = this.elevatorService.getElevator(id)
                .orElse(null);

        if (elevatorToUpdate == null) {
            LOG.warning("Elevator with id %d was not found%n".formatted(id));
            return false;
        }
        boolean isAnythingUpdated = false;

        if (updatedCurrentFloor != null) {
            isAnythingUpdated = this.elevatorService.setElevatorCurrentFloor(elevatorToUpdate, updatedCurrentFloor);
        }

        if (updatedTargetFloors != null) {
            isAnythingUpdated = this.elevatorService.setElevatorTargetFloors(elevatorToUpdate, updatedTargetFloors) || isAnythingUpdated;
        }

        return isAnythingUpdated;
    }
}
