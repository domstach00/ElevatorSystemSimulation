package org.example;

import org.example.config.ElevatorsConfig;
import org.example.console.ConsoleApplication;
import org.example.repository.ElevatorRepository;
import org.example.repository.ElevatorRepositoryFromFile;
import org.example.service.ElevatorService;
import org.example.service.ElevatorServiceImpl;
import org.example.system.ElevatorSystem;
import org.example.system.ElevatorSystemImpl;

public class Main {

    public static void main(String[] args) {
//        int numberOfElevators = 5;
//        int minFloorValue = -2;
//        int maxFloorValue = 10;

        // Config
//        ElevatorsConfig predefinedConfig = new ElevatorsConfig(numberOfElevators, minFloorValue, maxFloorValue);
        ElevatorsConfig configFromFile = new ElevatorsConfig();

        // Repository
//        ElevatorRepository elevatorRepositoryDefaultElevators = new ElevatorRepositoryDefaultElevators(predefinedConfig);
        ElevatorRepository elevatorRepositoryFromFile = new ElevatorRepositoryFromFile(configFromFile);

        // Service
        ElevatorService elevatorService = new ElevatorServiceImpl(elevatorRepositoryFromFile, configFromFile);

        // System
        ElevatorSystem elevatorSystem = new ElevatorSystemImpl(elevatorService);

        // Console app
        ConsoleApplication consoleApplication = new ConsoleApplication(elevatorSystem);

        consoleApplication.runApplication();
    }
}