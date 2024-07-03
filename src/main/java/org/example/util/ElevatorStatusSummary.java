package org.example.util;

import org.example.model.Elevator;

import java.util.List;

public class ElevatorStatusSummary {
    private static final String format = "| %-12s | %-15s | %-20s | %-30s | %-35s |%n";
    private static final String[] headers = {
            "Elevator ID", "Current Floor", "Current Direction", "Target Floors", "Target Floors (Other Direction)"};
    private static final String rowSeparator =
            "+--------------+-----------------+----------------------+--------------------------------+-------------------------------------+%n";

    public static String getSummaryText(List<Elevator> elevators) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(rowSeparator));
        sb.append(String.format(format, headers));
        sb.append(String.format(rowSeparator));

        for (Elevator elevator : elevators) {
            sb.append(String.format(format,
                    elevator.getId(),
                    elevator.getCurrentFloor(),
                    elevator.getCurrentDirection(),
                    elevator.getTargetFloors(),
                    elevator.getTargetFloorsOtherDirection()
            ));

            sb.append(String.format(rowSeparator));
        }

        return sb.toString();
    }
}
