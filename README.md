# Elevator System Simulation

## Introduction
This project simulates an elevator control system in a building. It is capable of handling multiple elevators at the same 
time for a building that may also have underground floors (level below 0). You can configure basic information about 
the elevators such as their quantity, and also define the initial state for each elevator.

## Table of Contents
1. [Introduction](#introduction)
2. [Installation](#installation)
3. [Application start](#application-start)
4. [Features](#features)
5. [Dependencies](#dependencies)
6. [Configuration](#configuration)
7. [Elevator Selection Algorithm](#elevator-selection-algorithm)
8. [Problem of floor selection upon elevator arrival](#problem-of-floor-selection-upon-elevator-arrival)
9. [Usage](#usage)
10. [Class diagram](#class-diagram)
11. [Edge cases examples](#edge-cases-examples)
12. [Contributor](#contributor)

## Installation
To install and run this project, follow these steps:
1. Clone the repository:
    ```bash
    git clone https://github.com/domstach00/ElevatorSystemSimulation.git
    ```
2. Navigate to the project directory:
    ```bash
    cd ElevatorSystemSimulation
    ```
3. Build the project using Maven:
    ```bash
    mvn clean install
    ```

## Application start
To run the simulation, execute the following command:
```bash
java -jar .\target\ElevatorSystemSimulation-1.0-jar-with-dependencies.jar
```

## Features
- Manage multiple elevators
- Real-time status updates
- Configurable parameters
- Configurable elevators start status 

## Dependencies
- Java 17
- Maven 3.9.8 or higher

## Configuration
### Basic system configuration
Configuration options can be modified in the `src/main/resources/config/elevators.properties` file. Here are some common settings:
- `numberOfElevators`: Number of elevators in the system
- `minFloorValue`: Lowest floor number
- `maxFloorValue`: Highest floor number

### Predefined elevator start status
The startup status off elevators can be modified in the `src/main/resources/elevators/elevators.json` file. Here is template for this file:
```json
[
   {
      "id": "<Integer>",
      "currentFloor": "<Integer>",
      "currentDirection": "<UP | IDLE | DOWN>",
      "targetFloors": ["<Integer>"],
      "targetFloorsOtherDirection": ["<Integer>"]
   }
]
```

## Elevator Selection Algorithm
The elevator selection algorithm is responsible for choosing the most suitable elevator to service a user's request. The process is as follows:
1. **Validate the floor**: The algorithm checks if the requested floor is within the valid range of floors.
2. **Find the closest elevator**: In class `ElevatorServiceImpl` the method `findClosestElevator` is used to find the nearest elevator that can service the request. It considers the current position of the elevators and their moving direction.
   1. At the beginning algorithm looks for elevator that is on same floor and do not move, or it goes to the same direction.
   2. If there is no such elevator then algorithm looks for the elevator that is passing the caller floor and moves to same direction.
   3. In last case when no elevator is passing the caller floor then the elevator with the fewest active calls will be called.
3. **Add floor request to elevator's target list**: Once the nearest elevator is identified, the floor where the user called the elevator is added to the appropriate target list of the elevator.
4. **Change elevator direction if idle**: If the selected elevator is currently idle, its direction is changed to the new target.

## Problem of floor selection upon elevator arrival
To solve the problem of floor selection by the user after entering the elevator, I decided to use the `Set<Integer, Set<Integer>>` structure (in the `ElevatorSystemImpl` class).
- The key value represents the floor number from where the elevator was called.
- The list of values represents the floors that the user/users want to reach.

The algorithm handling the above problem works as follows:
- Only calls in which the user defines the floor they want to go to are added to the above structure.
- If the elevator is on the floor with the key value, then only values that match the direction of the elevator are added to `targetFloors`
   - The rest of the users wait for the next elevator
- Used values are removed from the original structure

## Usage
When application is running the following **commands** are available:
- `help` - displays available commands.
- `exit` - stops the application.
- `step` - performs a step in the simulation. Status is displayed after each step.
- `status` - displays the statuses of the elevators.
- `update <id> <updatedCurrentFloor?> <updatedTargetFloors?>` - updates the elevator with the given id.
- `call <floorNumber> <direction> <desiredFloor?>` - calls the elevator on the given floor number to move in the direction (`1 == UP` and `-1 == DOWN`). DesiredFloor is the floor number where the user wants to go.  

<br>The `?` symbol means that the given argument is optional. You can pass `null` or do not pass anything if this is the last argument.

## Class diagram
![classDiagram](classDiagram.png)

## Edge cases examples
Test cases that represents edge cases usage can be found in the `src/test/java` directory.

# Contributor
- Dominik Stachowiak