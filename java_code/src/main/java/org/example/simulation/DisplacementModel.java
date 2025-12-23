package org.example.simulation;

import java.util.List;

/**
 * Calculates pad displacement distances based on rotation around the forearm
 * axis. The model leaves probability handling to {@link Simulation}.
 */
public class DisplacementModel {

    public DisplacementModel() {

    }

    /**
     * Updates each pad's displacement distance using the arc-length equation
     * {@code s = r * theta}, preserving the sign convention from the original
     * implementation.
     *
     * @param angleDiff angular displacement in degrees. Positive angles move
     *                  pads in the positive axis direction, matching the
     *                  original sign handling.
     * @param pads      mutable list of pads whose displacement distances are
     *                  updated in place.
     */
    public void upadteDisplacementDistance(double angleDiff, List<Pad> pads){
        double theta = Math.toRadians(angleDiff);
        for(Pad pad: pads){
            double radius = pad.getRadiusFromAxis();
            double displacement = radius * theta; //si thetha > 0, displacement < 0
            pad.setDisplacementDistance(-displacement);
        }
    }
}
