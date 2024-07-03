package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.ElevatorsConfig;
import org.example.model.Elevator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The `ElevatorRepositoryFromFile` class loads elevators from json file
 */
public class ElevatorRepositoryFromFile implements ElevatorRepository {
    private final static Logger LOG = Logger.getLogger(ElevatorRepositoryFromFile.class.getName());
    private final static String elevatorsFilePath = "/elevators/elevators.json";
    private final ElevatorsConfig elevatorsConfig;
    private List<Elevator> elevators;

    public ElevatorRepositoryFromFile(ElevatorsConfig elevatorsConfig) {
        this.elevatorsConfig = elevatorsConfig;
    }

    @Override
    public List<Elevator> getElevators() {
        if (this.elevators == null) {
            this.elevators = readElevatorsFromJsonFile(elevatorsFilePath);
        }
        return this.elevators;
    }

    private List<Elevator> readElevatorsFromJsonFile(String path) {
        ObjectMapper mapper = new ObjectMapper();
        List<Elevator> elevatorList;

        if (!doesFileExist(path)) {
            String errorMsg = "File in given resource path does not exists = %s"
                    .formatted(path);
            LOG.severe(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        try(InputStream is = getClass().getResourceAsStream(path)) {
            elevatorList = mapper.readValue(is, new TypeReference<>() {});

            if (elevatorList != null) {
                validateNumberOfElevators(elevatorList);
                elevatorList.forEach(this::validateElevator);
            }


        } catch (IOException e) {
            String errorMsg = "Elevators json file was not found in resources, path = %s".formatted(path);
            LOG.severe(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
        return elevatorList;
    }

    private boolean doesFileExist(String filePath) {
        return getClass().getResource(filePath) != null;
    }

    private void validateNumberOfElevators(List<Elevator> elevatorList) {
        if (elevatorList.size() > elevatorsConfig.getNumberOfElevators()) {
            String errorMsg = "Too many elevators in file (%d) config=( numberOfElevators = %d ), file = %s"
                    .formatted(elevatorList.size(), elevatorsConfig.getNumberOfElevators(), elevatorsFilePath);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    private void validateElevator(Elevator elevator) {
        if (!isGivenFloorWithinSystemFloorRange(elevator.getCurrentFloor())
                || !areAllTargetFloorsValid(elevator.getTargetFloors())
                || !areAllTargetFloorsValid(elevator.getTargetFloorsOtherDirection())) {
            String errorMsg = "Elevator in file has illegal floor value ( elevatorId = %d ) config=( minFloorValue = %d, maxFloorValue = %d ), file = %s"
                    .formatted(elevator.getId(), elevatorsConfig.getMinFloorValue(), elevatorsConfig.getMaxFloorValue(), elevatorsFilePath);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    private boolean areAllTargetFloorsValid(Set<Integer> targets) {
        return targets.stream()
                .allMatch(this::isGivenFloorWithinSystemFloorRange);
    }

    private boolean isGivenFloorWithinSystemFloorRange(int givenFloor) {
        return givenFloor >= this.elevatorsConfig.getMinFloorValue()
                && givenFloor <= this.elevatorsConfig.getMaxFloorValue();
    }
}
