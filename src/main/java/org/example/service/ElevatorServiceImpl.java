package org.example.service;

import org.example.repository.ElevatorRepository;
import org.example.config.ElevatorsConfig;
import org.example.model.Direction;
import org.example.model.Elevator;

import java.util.*;
import java.util.logging.Logger;

public class ElevatorServiceImpl implements ElevatorService {
    private final static Logger LOG = Logger.getLogger(ElevatorServiceImpl.class.getName());
    private final ElevatorRepository elevatorRepository;
    private final ElevatorsConfig elevatorsConfig;

    public ElevatorServiceImpl(ElevatorRepository elevatorRepository, ElevatorsConfig elevatorsConfig) {
        this.elevatorRepository = elevatorRepository;
        this.elevatorsConfig = elevatorsConfig;
    }

    @Override
    public List<Elevator> getElevators() {
        return this.elevatorRepository.getElevators();
    }

    @Override
    public Optional<Elevator> getElevator(int elevatorId) {
        return getElevators().stream()
                .filter(elevator -> elevator.getId() == elevatorId)
                .findFirst();
    }

    /**
     * The `elevatorStep` method updates the state of the elevator. It checks if the elevator has any target floors to reach.
     * If there are target floors, the elevator is moved by one step. If there are no target floors in the current direction,
     * the method checks if there are any target floors in the opposite direction. If there are, the targets from the opposite
     * direction are moved to the main set of targets and direction is changed to the opposite.
     *
     * @param elevator The elevator to be updated.
     */
    @Override
    public void elevatorStep(Elevator elevator) {
        if (!elevator.getTargetFloors().isEmpty()) {
            moveElevatorByStep(elevator);
        } else if (!elevator.getTargetFloorsOtherDirection().isEmpty()) {
            moveTargetFloorsOtherDirectionToTargetFloors(elevator);
            fixElevatorDirection(elevator);
            moveElevatorByStep(elevator);
        }
    }

    @Override
    public Elevator getElevatorFromFloor(int floor) {
        if (!isFloorValid(floor)) {
            LOG.warning("Given floor is out of scope (given floor = %d, minFloor = %d, maxFloor = %d)"
                    .formatted(floor, this.elevatorsConfig.getMinFloorValue(), this.elevatorsConfig.getMaxFloorValue()));
            return null;
        }
        return getElevators().stream()
                .filter(elevator -> elevator.getCurrentFloor() == floor)
                .findFirst()
                .orElse(null);
    }

    /**
     * The `callElevator` method is used to call an elevator to a specified floor and direction. It first validates the selected direction,
     * then finds the closest elevator that can service the request. It adds the floor call to the proper target list of the elevator and
     * sets the proper direction for the elevator if needed.
     *
     * @param floor The floor from which the elevator is called.
     * @param selectedDirection The direction in which caller want to go.
     * @return The closest elevator that can service the request.
     */
    @Override
    public Elevator callElevator(int floor, Direction selectedDirection) {
        if (!isFloorValid(floor)) {
            LOG.warning("Given floor is out of scope (given floor = %d, minFloor = %d, maxFloor = %d)"
                    .formatted(floor, this.elevatorsConfig.getMinFloorValue(), this.elevatorsConfig.getMaxFloorValue()));
            return null;
        }
        validateSelectedDirection(selectedDirection);
        Elevator closestElevator = findClosestElevator(floor, selectedDirection);

        addFloorCallToProperTargetList(closestElevator, selectedDirection, floor);
        changeDirectionFromIdleToNewTarget(closestElevator, floor);

        return closestElevator;
    }

    @Override
    public boolean setElevatorCurrentFloor(Elevator elevator, int floor) {
        if (!isFloorValid(floor)) {
            LOG.warning("Given floor is out of scope (given floor = %d, minFloor = %d, maxFloor = %d)"
                    .formatted(floor, this.elevatorsConfig.getMinFloorValue(), this.elevatorsConfig.getMaxFloorValue()));
            return false;
        }

        elevator.setCurrentFloor(floor);
        elevator.removeTargetFloor(floor);

        fixElevatorDirection(elevator);
        return true;
    }

    @Override
    public boolean setElevatorTargetFloors(Elevator elevator, int[] targetFloors) {
        elevator.setTargetFloors(new HashSet<>());
        elevator.setTargetFloorsOtherDirection(new HashSet<>());
        if (targetFloors != null) {
            setupProperTargetFloors(elevator, targetFloors);
        }
        return true;
    }

    @Override
    public void selectFloor(Elevator elevator, int floor) {
        if (!isFloorValid(floor)) {
            LOG.warning("Given floor is out of scope (given floor = %d, minFloor = %d, maxFloor = %d)"
                    .formatted(floor, this.elevatorsConfig.getMinFloorValue(), this.elevatorsConfig.getMaxFloorValue()));
        } else if (elevator.getTargetFloors().isEmpty()) {
            elevator.addTargetFloor(floor);
            fixElevatorDirection(elevator);
        } else if (isElevatorPassingFloor(elevator, floor)) {
            elevator.addTargetFloor(floor);
        } else {
            elevator.addTargetFloorInOtherDirection(floor);
        }
    }

    @Override
    public boolean hasReachedTargetFloor(Elevator elevator) {
        return elevator.getTargetFloors().contains(elevator.getCurrentFloor());
    }

    @Override
    public void removeCurrentFloorFromTarget(Elevator elevator) {
        elevator.removeTargetFloor(elevator.getCurrentFloor());
        updateDirection(elevator);
    }

    private void updateDirection(Elevator elevator) {
        if (!hasElevatorAnyCall(elevator)) {
            elevator.setCurrentDirection(Direction.IDLE);
        } else if (elevator.getTargetFloors().isEmpty() && !elevator.getTargetFloorsOtherDirection().isEmpty()) {
            changeToOppositeElevatorDirection(elevator);
        }
    }

    /**
     * The `findClosestElevator` method finds the closest elevator that can service a request. It first validates the selected direction,
     * then checks if there is only one elevator in the repository. If there is, it returns that elevator. Otherwise, it tries to find an
     * elevator on the same floor and direction, a passing elevator, or the fastest finishing elevator.
     *
     * @param floor The floor from which the elevator is called.
     * @param selectedDirection The direction in which the elevator is supposed to go.
     * @return The closest elevator that can service the request.
     */
    private Elevator findClosestElevator(int floor, Direction selectedDirection) {
        validateSelectedDirection(selectedDirection);
        Elevator closestElevator;

        if (getElevators().size() == 1) {
            return getElevators().get(0);
        }

        closestElevator = getElevatorOnSameFloorAndDirection(floor, selectedDirection)
                .or(() -> getClosestPassingElevator(floor, selectedDirection))
                .orElse(getFastestFinishingElevator());

        return closestElevator;
    }

    /**
     * The `getElevatorOnSameFloorAndDirection` method tries to find an elevator that is on the same floor and going in the same direction
     * as the request.
     *
     * @param floor The floor from which the elevator is called.
     * @param direction The direction in which the elevator is supposed to go.
     * @return An Optional that may contain an elevator if one is found that meets the criteria.
     */
    private Optional<Elevator> getElevatorOnSameFloorAndDirection(int floor, Direction direction) {
        validateSelectedDirection(direction);

        return getElevators().stream()
                .filter(elevator -> elevator.getCurrentFloor() == floor
                        && hasElevatorValidDirection(elevator, direction))
                .findFirst();
    }

    /**
     * The `getClosestPassingElevator` method tries to find the closest passing elevator to the requested floor.
     *
     * @param floor The floor from which the elevator is called.
     * @param direction The direction in which the elevator is supposed to go.
     * @return An Optional that may contain an elevator if one is found that meets the criteria.
     */
    private Optional<Elevator> getClosestPassingElevator(int floor, Direction direction) {
        return getElevators().stream()
                .filter(elevator -> hasElevatorValidDirection(elevator, direction)
                        && isElevatorPassingFloor(elevator, floor))
                .min(Comparator.comparing(elevator -> Math.abs(elevator.getCurrentFloor() - floor)));
    }

    /**
     * The `getFastestFinishingElevator` method tries to find the elevator that will finish its current tasks the fastest.
     *
     * @return The elevator that will finish its current tasks the fastest.
     */
    private Elevator getFastestFinishingElevator() {
        if (getElevators().isEmpty()) {
            LOG.warning("There are no elevators");
            return null;
        }

        return getElevators().stream()
                .min(Comparator.comparing(elevator ->
                        elevator.getTargetFloors().size() + elevator.getTargetFloorsOtherDirection().size()))
                .get();
    }

    private boolean hasElevatorAnyCall(Elevator elevator) {
        return !elevator.getTargetFloors().isEmpty() || !elevator.getTargetFloorsOtherDirection().isEmpty();
    }

    /**
     * The `moveElevatorByStep` method moves the elevator by one step in the current direction. It first validates if the move
     * to the next floor is allowed, then updates the current floor of the elevator by adding the value corresponding to the
     * current direction.
     *
     * @param elevator The elevator to be moved.
     */
    private void moveElevatorByStep(Elevator elevator) {
        validateMovingToFloor(elevator);
        elevator.setCurrentFloor(elevator.getCurrentFloor() + elevator.getCurrentDirection().mapDirectionToValue());
    }

    private boolean hasElevatorValidDirection(Elevator elevator, Direction direction) {
        return elevator.getCurrentDirection() == Direction.IDLE
                || elevator.getCurrentDirection() == direction;
    }

    /**
     * The `setupProperTargetFloors` method assign given target floors to proper target list based
     * on the current elevator direction
     * @param elevator The elevator whose target floors are to be updated.
     * @param targetFloors The array of new target floors for given elevator.
     */
    private void setupProperTargetFloors(Elevator elevator, int[] targetFloors) {
        for (int floor : targetFloors) {
            if (!isFloorValid(floor)) {
                LOG.warning("Given floor is out of range (given floor = %d, minFloor = %d, maxFloor = %d)"
                        .formatted(floor, this.elevatorsConfig.getMinFloorValue(), this.elevatorsConfig.getMaxFloorValue()));
                continue;
            }
            if (elevator.getCurrentDirection() == Direction.IDLE) {
                elevator.addTargetFloor(floor);
                elevator.setCurrentDirection(
                        Direction.getDirectionByFloors(elevator.getCurrentFloor(), floor)
                );
            } else if (isElevatorPassingFloor(elevator, floor)) {
                elevator.addTargetFloor(floor);
            } else {
                elevator.addTargetFloorInOtherDirection(floor);
            }
        }
    }

    private boolean isElevatorPassingFloor(Elevator elevator, int floor) {
        return elevator.getCurrentDirection() == Direction.IDLE
                || (elevator.getCurrentDirection() == Direction.UP && floor >= elevator.getCurrentFloor())
                || (elevator.getCurrentDirection() == Direction.DOWN && floor <= elevator.getCurrentFloor());
    }

    private void validateSelectedDirection(Direction selectedDirection) {
        if (selectedDirection == Direction.IDLE) {
            LOG.warning("Selected direction should be UP or DOWN, current selected direction is " + selectedDirection);
        }
    }

    private void validateMovingToFloor(Elevator elevator) {
        final int reachedFloor = elevator.getCurrentFloor() + elevator.getCurrentDirection().mapDirectionToValue();
        if (reachedFloor < this.elevatorsConfig.getMinFloorValue() || reachedFloor > this.elevatorsConfig.getMaxFloorValue()) {
            String errorMsg = "Elevator cannot do this invalid move (Elevator id = %d, currentFloor = %d, direction = %s) (minFloor = %d, maxFloor = %d)"
                    .formatted(elevator.getId(), elevator.getCurrentFloor(), elevator.getCurrentDirection(),
                            this.elevatorsConfig.getMinFloorValue(), this.elevatorsConfig.getMaxFloorValue());
            LOG.severe(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    private boolean isFloorValid(int floor) {
        return floor >= this.elevatorsConfig.getMinFloorValue()
                && floor <= this.elevatorsConfig.getMaxFloorValue();
    }

    private void changeDirectionFromIdleToNewTarget(Elevator elevator, int newTargetFloor) {
        if (elevator.getCurrentDirection() == Direction.IDLE) {
            elevator.setCurrentDirection(
                    Direction.getDirectionByFloors(elevator.getCurrentFloor(), newTargetFloor)
            );
        }
    }

    /**
     * The `addFloorCallToProperTargetList` ensures that elevator will not stop on floor where caller expect to move in different direction then
     * it is currently moving
     * @param elevator - selected elevator
     * @param selectedDirection - selected direction on elevator
     * @param callFloor - from witch floor user called elevator
     */
    private void addFloorCallToProperTargetList(Elevator elevator, Direction selectedDirection, int callFloor) {
        if (hasElevatorValidDirection(elevator, selectedDirection)) {
            elevator.addTargetFloor(callFloor);
        } else {
            elevator.addTargetFloorInOtherDirection(callFloor);
        }
    }

    /**
     * The `moveTargetFloorsOtherDirectionToTargetFloors` method moves the targets from the opposite direction to the main set
     * of targets. It then clears all the targets from the set of targets in the opposite direction.
     *
     * @param elevator The elevator whose target floors are to be updated.
     */
    private void moveTargetFloorsOtherDirectionToTargetFloors(Elevator elevator) {
        elevator.setTargetFloors(elevator.getTargetFloorsOtherDirection());
        elevator.setTargetFloorsOtherDirection(new HashSet<>());
    }

    private void changeToOppositeElevatorDirection(Elevator elevator) {
        final int oppositeDirection = elevator.getCurrentDirection().mapDirectionToValue() * -1;

        elevator.setCurrentDirection(
                Direction.fromValue(oppositeDirection)
        );
    }

    /**
     * The `fixElevatorDirection` method sets direction based on current elevator floor and its targets.
     * @param elevator The elevator whose direction is to be updated.
     */
    private void fixElevatorDirection(Elevator elevator) {
        if (elevator.getTargetFloors().isEmpty() && !elevator.getTargetFloorsOtherDirection().isEmpty()) {
            moveTargetFloorsOtherDirectionToTargetFloors(elevator);
        }

        if (!elevator.getTargetFloors().isEmpty()) {
            elevator.setCurrentDirection(
                    Direction.getDirectionByFloors(elevator.getCurrentFloor(), elevator.getTargetFloors().iterator().next())
            );
        } else {
            elevator.setCurrentDirection(Direction.IDLE);
        }
    }
}
