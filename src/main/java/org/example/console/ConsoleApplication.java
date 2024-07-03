package org.example.console;

import org.example.system.ElevatorSystem;
import org.example.model.Direction;
import org.example.model.Elevator;

import java.util.Scanner;
import java.util.logging.Logger;

public class ConsoleApplication {
    private final static Logger LOG = Logger.getLogger(ConsoleApplication.class.getName());
    private final static String NULL_ARG = "null";
    private final static String EXIT_APPLICATION_COMMAND = "exit";
    private final static String DO_STEP_IN_SIMULATION_COMMAND = "step";
    private final static String SHOW_STATUS_COMMAND = "status";
    private final static String UPDATE_COMMAND = "update";
    private final static String HELP_COMMAND = "help";
    private final static String CALL_ELEVATOR_COMMAND = "call";
    private final ElevatorSystem elevatorSystem;

    public ConsoleApplication(ElevatorSystem elevatorSystem) {
        this.elevatorSystem = elevatorSystem;
    }

    public void runApplication() {
        Scanner scanner = new Scanner(System.in);
        String command;

        do {
            System.out.printf("Enter command (use `%s` command to display available commands)\n", HELP_COMMAND);
            command = scanner.nextLine();

            if (command.equals(EXIT_APPLICATION_COMMAND)) {
                handleExit();
            } else if (command.equals(DO_STEP_IN_SIMULATION_COMMAND)) {
                handleStep();
                handleStatus();
            } else if (command.equals(SHOW_STATUS_COMMAND)) {
                handleStatus();
            } else if (command.equals(HELP_COMMAND)) {
                handleHelp();
            } else if (command.startsWith(UPDATE_COMMAND)) {
                handleUpdate(command);
            } else if (command.startsWith(CALL_ELEVATOR_COMMAND)) {
                handleCallElevator(command);
            } else {
                LOG.warning("Unknown command `%s`".formatted(command));
            }

        } while (!command.equalsIgnoreCase(EXIT_APPLICATION_COMMAND));

    }

    private void handleExit() {
        System.out.println("Shutting down application");
    }

    private void handleStep() {
        this.elevatorSystem.step();
        System.out.println("Step in simulation has been finished");
    }

    private void handleStatus() {
        this.elevatorSystem.status();
    }

    private void handleCallElevator(String command) {
        String[] commandParts = command.split(" ");
        if (commandParts.length > 2) {
            int givenFloor = Integer.parseInt(commandParts[1]);
            Direction givenDirection = Direction.fromValue(
                    Integer.parseInt(commandParts[2])
            );

            if (givenDirection == Direction.IDLE) {

                System.out.println("Error - direction should not be IDLE");
                return;
            }

            Elevator calledElevator;
            if (commandParts.length > 3 && !commandParts[3].equals(NULL_ARG)) {
                int goToFloor = Integer.parseInt(commandParts[3]);
                calledElevator = this.elevatorSystem.pickup(givenFloor, givenDirection, goToFloor);
                System.out.printf("Elevator %d has been called on floor %d, and user wants to get on floor %d",
                        calledElevator.getId(), givenFloor, goToFloor);
            } else {
                calledElevator = this.elevatorSystem.pickup(givenFloor, givenDirection);
                System.out.printf("Elevator %d has been called on floor %d", calledElevator.getId(), givenFloor);
            }

            this.elevatorSystem.status(calledElevator);
        } else {
            System.out.println("Elevator call cannot be done because of invalid command use");
        }
    }

    private void handleUpdate(String command) {
        String[] commandParts = command.split(" ");
        if (commandParts.length > 1) {
            int givenId = Integer.parseInt(commandParts[1]);

            Integer toUpdateCurrentFloor = commandParts.length > 2 && !commandParts[2].equals(NULL_ARG)
                    ? Integer.parseInt(commandParts[2])
                    : null;

            int[] toUpdateTargetFloors = commandParts.length > 3 && !commandParts[3].equals(NULL_ARG)
                    ? mapStringToIntArray(commandParts[3])
                    : null;

            if (this.elevatorSystem.updateElevator(givenId, toUpdateCurrentFloor, toUpdateTargetFloors)) {
                System.out.printf("Elevator with id %d has been updated", givenId);
            } else {
                System.out.printf("Elevator with id %d has NOT been updated", givenId);
            }
        } else {
            System.out.println("Updated cannot be done because of lack of elevator id");
        }
    }

    private int[] mapStringToIntArray(String stringIntArray) {
        String[] parts = stringIntArray.split(",");
        int[] result = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i]);
        }

        return result;
    }

    private void handleHelp() {
        System.out.printf(
                """
                
                            ------------------------Available Commands------------------------
                    The `?` symbol means that the given argument is optional. You can pass `%s` or do not pass anything if this is the last argument.
                    %s - displays available commands.
                    %s - stops the application.
                    %s - performs a step in the simulation. Status is displayed after each step.
                    %s - displays the statuses of the elevators.
                    %s <id> <updatedCurrentFloor?> <updatedTargetFloors?> - updates the elevator with the given id.
                    %s <floorNumber> <direction> <desiredFloor?> - calls the elevator on the given floor number to move in the direction (1==UP and -1==DOWN). DesiredFloor is the floor number where the user wants to go.
                
                """,
                NULL_ARG,
                HELP_COMMAND,
                EXIT_APPLICATION_COMMAND,
                DO_STEP_IN_SIMULATION_COMMAND,
                SHOW_STATUS_COMMAND,
                UPDATE_COMMAND,
                CALL_ELEVATOR_COMMAND
        );
    }
}
