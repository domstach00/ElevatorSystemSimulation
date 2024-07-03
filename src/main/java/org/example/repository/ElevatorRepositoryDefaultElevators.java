package org.example.repository;

import org.example.config.ElevatorsConfig;
import org.example.factory.ElevatorFactory;
import org.example.model.Elevator;

import java.util.ArrayList;
import java.util.List;

/**
 * The `ElevatorRepositoryDefaultElevators` class generates default Elevators based
 * on given configuration
 */
public class ElevatorRepositoryDefaultElevators implements ElevatorRepository {
    private final ElevatorsConfig elevatorsConfig;

    private List<Elevator> elevators = null;

    public ElevatorRepositoryDefaultElevators(ElevatorsConfig elevatorsConfig) {
        this.elevatorsConfig = elevatorsConfig;
    }

    @Override
    public List<Elevator> getElevators() {
        if (elevators == null) {
            generateElevatorsBasedOnConfig();
        }

        return elevators;
    }

    private void generateElevatorsBasedOnConfig() {
        this.elevators = new ArrayList<>();

        for (int i = 0; i < elevatorsConfig.getNumberOfElevators(); i++) {
            elevators.add(ElevatorFactory.createElevator());
        }
    }
}
