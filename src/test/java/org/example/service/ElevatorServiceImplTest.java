package org.example.service;

import org.example.factory.ElevatorFactory;
import org.example.repository.ElevatorRepository;
import org.example.config.ElevatorsConfig;
import org.example.model.Direction;
import org.example.model.Elevator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ElevatorServiceImplTest {
    private ElevatorService elevatorService;
    private ElevatorRepository elevatorRepositoryMock;
    private ElevatorsConfig elevatorsConfig;


    @BeforeEach
    void setUp() {
        elevatorRepositoryMock = Mockito.mock(ElevatorRepository.class);
        elevatorsConfig = new ElevatorsConfig(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

        elevatorService = new ElevatorServiceImpl(elevatorRepositoryMock, elevatorsConfig);
    }

    @Test
    void updateElevatorWhenMovingUp() {
        // given
        Elevator elevatorMovingUp = ElevatorFactory.createElevator(-1, 3);
        int expectedFloor = elevatorMovingUp.getCurrentFloor() + 1;
        Direction expectedDirection = Direction.UP;

        // when
        this.elevatorService.elevatorStep(elevatorMovingUp);

        // then
        assertEquals(expectedFloor, elevatorMovingUp.getCurrentFloor());
        assertEquals(expectedDirection, elevatorMovingUp.getCurrentDirection());
    }

    @Test
    void updateElevatorWhenMovingDown() {
        // given
        Elevator elevatorMovingDown = ElevatorFactory.createElevator(3, 0);
        int expectedFloor = elevatorMovingDown.getCurrentFloor() - 1;
        Direction expectedDirection = Direction.DOWN;

        // when
        this.elevatorService.elevatorStep(elevatorMovingDown);

        // then
        assertEquals(expectedFloor, elevatorMovingDown.getCurrentFloor());
        assertEquals(expectedDirection, elevatorMovingDown.getCurrentDirection());
    }

    @Test
    void updateElevatorWhenNotMoving() {
        // given
        Elevator elevatorNotMoving = ElevatorFactory.createElevator(1, 1);
        int expectedFloor = elevatorNotMoving.getCurrentFloor();
        Direction expectedDirection = Direction.IDLE;

        // when
        this.elevatorService.elevatorStep(elevatorNotMoving);

        // then
        assertEquals(expectedFloor, elevatorNotMoving.getCurrentFloor());
        assertEquals(expectedDirection, elevatorNotMoving.getCurrentDirection());
    }

    @Test
    void updateElevatorWhenChangingDirection() {
        // given
        Elevator elevator = ElevatorFactory.createElevator(0, 1, -2);
        int expectedFloor = 0;
        Direction expectedDirection = Direction.DOWN;

        // when
        this.elevatorService.elevatorStep(elevator); // move to 1st floor
        if (this.elevatorService.hasReachedTargetFloor(elevator)) {
            // if reached target floor, then remove it from target list
            this.elevatorService.removeCurrentFloorFromTarget(elevator);
        }
        this.elevatorService.elevatorStep(elevator);

        // then
        assertEquals(expectedFloor, elevator.getCurrentFloor());
        assertEquals(expectedDirection, elevator.getCurrentDirection());
    }

    @Test
    void getElevatorFromFloor() {
        // given
        Elevator elevatorOnFloor0 = ElevatorFactory.createElevator(0);
        Elevator elevatorOnFloor1 = ElevatorFactory.createElevator(1);

        when(elevatorRepositoryMock.getElevators()).thenReturn(List.of(elevatorOnFloor0, elevatorOnFloor1));

        // when
        Elevator result = this.elevatorService.getElevatorFromFloor(1);

        // then
        assertEquals(elevatorOnFloor1, result);
    }

    @Test
    void getElevatorFromFloorWhenMoving() {
        // given
        Elevator elevatorOnFloor0 = ElevatorFactory.createElevator(0, 4);
        Elevator elevatorOnFloor1 = ElevatorFactory.createElevator(1, 6);

        when(elevatorRepositoryMock.getElevators()).thenReturn(List.of(elevatorOnFloor0, elevatorOnFloor1));

        // when
        Elevator result = this.elevatorService.getElevatorFromFloor(1);

        // then
        assertEquals(elevatorOnFloor1, result);
    }

    @Test
    void getElevatorFromFloorNoElevators() {
        // given
        when(elevatorRepositoryMock.getElevators()).thenReturn(List.of());

        // when
        Elevator result = this.elevatorService.getElevatorFromFloor(1);

        // then
        assertNull(result);
    }

    @Test
    void getElevatorFromFloorWhenNoElevatorsOnGivenFloor() {
        // given
        Elevator elevatorOnFloor0 = ElevatorFactory.createElevator(0, 4);
        Elevator elevatorOnFloor2 = ElevatorFactory.createElevator(2, 6);

        when(elevatorRepositoryMock.getElevators()).thenReturn(List.of(elevatorOnFloor0, elevatorOnFloor2));

        // when
        Elevator result = this.elevatorService.getElevatorFromFloor(1);

        // then
        assertNull(result);
    }


    @Test
    void callElevatorWhenElevatorIsOnSameFloorAndSameDirection() {
        // given
        int callFloor = 3;
        Elevator elevatorOnFloor0 = ElevatorFactory.createElevator(0);
        Elevator elevatorOnFloor2 = ElevatorFactory.createElevator(2, 6);
        Elevator elevatorOnFloor3 = ElevatorFactory.createElevator(callFloor, 5);

        when(elevatorRepositoryMock.getElevators()).thenReturn(
                List.of(elevatorOnFloor0, elevatorOnFloor2, elevatorOnFloor3));

        // when
        Elevator result = elevatorService.callElevator(callFloor, Direction.UP);

        // then
        assertEquals(elevatorOnFloor3, result);
        assertEquals(Direction.IDLE, elevatorOnFloor0.getCurrentDirection());
        assertEquals(Direction.UP, elevatorOnFloor2.getCurrentDirection());
        assertEquals(Direction.UP, elevatorOnFloor3.getCurrentDirection());
    }

    @Test
    void callElevatorWhenElevatorIsOnSameFloorAndNotMoving() {
        // given
        int callFloor = 3;
        Elevator elevatorOnFloor0 = ElevatorFactory.createElevator(0);
        Elevator elevatorOnFloor2 = ElevatorFactory.createElevator(2);
        Elevator elevatorOnFloor3 = ElevatorFactory.createElevator(callFloor);

        when(elevatorRepositoryMock.getElevators()).thenReturn(
                List.of(elevatorOnFloor0, elevatorOnFloor2, elevatorOnFloor3));

        // when
        Elevator result = elevatorService.callElevator(callFloor, Direction.UP);

        // then
        assertEquals(elevatorOnFloor3, result);
        assertEquals(Direction.IDLE, elevatorOnFloor0.getCurrentDirection());
        assertEquals(Direction.IDLE, elevatorOnFloor2.getCurrentDirection());
        assertEquals(Direction.IDLE, elevatorOnFloor3.getCurrentDirection());
    }

    @Test
    void callElevatorWhenElevatorIsOnSameFloorAndDifferentDirection() {
        // given
        int callFloor = 3;
        Elevator elevatorOnFloor0 = ElevatorFactory.createElevator(0);
        Elevator elevatorOnFloor2 = ElevatorFactory.createElevator(2, 6);
        Elevator elevatorOnFloor3 = ElevatorFactory.createElevator(callFloor, 0);

        when(elevatorRepositoryMock.getElevators()).thenReturn(
                List.of(elevatorOnFloor0, elevatorOnFloor2, elevatorOnFloor3));

        // when
        Elevator result = elevatorService.callElevator(callFloor, Direction.UP);

        // then
        assertEquals(elevatorOnFloor2, result);
        assertEquals(Direction.IDLE, elevatorOnFloor0.getCurrentDirection());
        assertEquals(Direction.UP, elevatorOnFloor2.getCurrentDirection());
        assertEquals(Direction.DOWN, elevatorOnFloor3.getCurrentDirection());
    }

    @Test
    void callElevatorWhenElevatorsDoNotPassGivenFloor() {
        // given
        int callFloor = 3;
        Elevator elevatorMovingDown = ElevatorFactory.createElevator(2, 0);
        Elevator elevatorMovingUpWithMoreTargets = ElevatorFactory.createElevator(4, 6);
        elevatorMovingUpWithMoreTargets.addTargetFloor(8);

        when(elevatorRepositoryMock.getElevators()).thenReturn(
                List.of(elevatorMovingDown, elevatorMovingUpWithMoreTargets));

        // when
        Elevator result = elevatorService.callElevator(callFloor, Direction.UP);

        // then
        assertEquals(elevatorMovingDown, result);
        assertEquals(elevatorMovingDown.getTargetFloors().size(), 1);
        assertEquals(elevatorMovingDown.getTargetFloorsOtherDirection().size(), 1);
        assertTrue(elevatorMovingDown.getTargetFloorsOtherDirection().contains(callFloor));
        assertEquals(elevatorMovingUpWithMoreTargets.getTargetFloors().size(), 2);
        assertEquals(Direction.DOWN, elevatorMovingDown.getCurrentDirection());
        assertEquals(Direction.UP, elevatorMovingUpWithMoreTargets.getCurrentDirection());
    }

    @Test
    void removeCurrentFloorFromTargetWhenThereAreMoreTargets() {
        // given
        Elevator elevator = ElevatorFactory.createElevator(1, 2);
        elevator.addTargetFloor(1);

        // when
        elevatorService.removeCurrentFloorFromTarget(elevator);

        // then
        assertEquals(1, elevator.getTargetFloors().size());
        assertEquals(0, elevator.getTargetFloorsOtherDirection().size());
        assertEquals(Direction.UP, elevator.getCurrentDirection());
    }

    @Test
    void removeCurrentFloorFromTargetWhenThereAreMoreTargetsInDifferentDirection() {
        // given
        Elevator elevator = ElevatorFactory.createElevator(1, 2, -1);
        elevator.setCurrentFloor(2);
        Direction expectedDirection = Direction.DOWN;

        // when
        elevatorService.removeCurrentFloorFromTarget(elevator);

        // then
        assertEquals(0, elevator.getTargetFloors().size());
        assertEquals(1, elevator.getTargetFloorsOtherDirection().size());
        assertEquals(expectedDirection, elevator.getCurrentDirection());
    }

    @Test
    void removeCurrentFloorFromTargetWhenThereAreNoMoreTargets() {
        // given
        Elevator elevator = ElevatorFactory.createElevator(1, 1);

        // when
        elevatorService.removeCurrentFloorFromTarget(elevator);

        // then
        assertEquals(0, elevator.getTargetFloors().size());
        assertEquals(0, elevator.getTargetFloorsOtherDirection().size());
        assertEquals(Direction.IDLE, elevator.getCurrentDirection());
    }

}