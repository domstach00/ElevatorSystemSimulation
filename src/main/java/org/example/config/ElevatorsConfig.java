package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ElevatorsConfig {
    private final static Logger LOG = Logger.getLogger(ElevatorsConfig.class.getName());
    private final static String configFileName = "config/elevators.properties";

    private int numberOfElevators;
    private int minFloorValue;
    private int maxFloorValue;

    public ElevatorsConfig() {
        readConfig(configFileName);
    }

    public ElevatorsConfig(int numberOfElevators, int minFloorValue, int maxFloorValue) {
        this.numberOfElevators = numberOfElevators;
        this.minFloorValue = minFloorValue;
        this.maxFloorValue = maxFloorValue;
    }

    public void readConfig(String path) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            validateInputStream(input);

            Properties properties = new Properties();
            properties.load(input);

            this.numberOfElevators = Integer.parseInt(properties.getProperty("numberOfElevators"));
            validateElevatorNumber(numberOfElevators);

            this.maxFloorValue = Integer.parseInt(properties.getProperty("maxFloorValue"));
            this.minFloorValue = Integer.parseInt(properties.getProperty("minFloorValue"));
            validateMinMaxValues(this.minFloorValue, this.maxFloorValue);
        } catch (IOException e) {
            String errorMsg = "Error while reading config file";
            LOG.severe(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public int getNumberOfElevators() {
        return numberOfElevators;
    }

    public int getMaxFloorValue() {
        return maxFloorValue;
    }

    public int getMinFloorValue() {
        return minFloorValue;
    }

    private void validateMinMaxValues(int minValue, int maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minFloorValue is bigger then maxFloorValue");
        } else if (minValue == maxValue) {
            LOG.warning("Lowest and highest floor levels are the same ( %d == %d )".formatted(minValue, maxValue));
        }
    }

    private void validateElevatorNumber(int numberOfElevators) {
        if (numberOfElevators < 0) {
            String errorMsg = "Number of elevators cannot be lower then 0 (numberOfElevators = %d)".formatted(numberOfElevators);
            LOG.severe(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        } else if (numberOfElevators == 0) {
            LOG.warning("Number of elevators is 0");
        }
    }

    private void validateInputStream(InputStream inputStream) {
        if (inputStream == null) {
            String errorMsg = "File in given resource path does not exists";
            LOG.severe(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }
}
