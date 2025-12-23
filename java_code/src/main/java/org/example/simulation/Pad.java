package org.example.simulation;

import org.example.config.GridConfig;

/**
 * Represents a single stimulation pad in the grid along with its geometric
 * relationship to the rotation axis and the Bayesian probabilities tracked
 * during the simulation. Row/column indices are immutable after construction
 * so callers can treat the identifier and grid location as stable keys while
 * freely mutating probabilities and displacement distance.
 */
public class Pad {
    private final int id;
    private final int row;
    private final int col;
    private double radiusFromAxis; //distancia en cm del centro de cada pad al eje de rotacion del antebraxo
    private double displacementDistance;
    private double probability;
    private double initialProb; //kflexion en la posición inicial (0 grados)


    /**
     * Creates a pad in the default grid using the provided radial distance.
     *
     * @param id              1-based pad identifier within the grid.
     * @param radiusFromAxis  distance in centimeters from the rotation axis
     *                        to the pad center.
     */
    public Pad(int id, double radiusFromAxis) {
        this(id, radiusFromAxis, GridConfig.defaultConfig());
    }

    /**
     * Creates a pad using an explicit grid configuration.
     *
     * @param id             1-based pad identifier within the grid.
     * @param radiusFromAxis distance in centimeters from the rotation axis
     *                       to the pad center.
     * @param gridConfig     grid layout reference used to derive row/column.
     */
    public Pad(int id, double radiusFromAxis, GridConfig gridConfig) {
        this.id = id;
        this.col = (id - 1) / gridConfig.getRows();
        this.row = (id - 1) % gridConfig.getRows();
        this.radiusFromAxis = radiusFromAxis;
        this.displacementDistance = 0;
        this.probability = 0;
        this.initialProb = 0;

    }


    public int getId() {
        return id;
    }
    public int getRow() {
        return row;
    }
    public int getCol() {
        return col;
    }
    public double getRadiusFromAxis() {
        return radiusFromAxis;
    }

    public double getDisplacementDistance() {
        return displacementDistance;
    }

    public double getInitialProb() { return initialProb; }
    public double getProbability() { return probability; }

    public void setDisplacementDistance(double displacementDistance) {
        this.displacementDistance = displacementDistance;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * Sets the initial probability and aligns the current probability with it.
     */
    public void setInitialProb(double initialProb) {
        this.initialProb = initialProb;
        this.probability = initialProb;
    }

    public static void main(String[] args) {
        System.out.println("Pad ID | Row | Distance to Radius (cm)");
        System.out.println("-------------------------------------");

        GridConfig gridConfig = GridConfig.defaultConfig();
        for (int id = 1; id <= gridConfig.getPadCount(); id++) {
            Pad pad = new Pad(id,2.86, gridConfig);
            int row = (id - 1) % gridConfig.getRows();
            System.out.printf("  %2d   |  %d  |        %.2f cm\n", pad.getId(), row, pad.getRadiusFromAxis());
        }
    }
}


