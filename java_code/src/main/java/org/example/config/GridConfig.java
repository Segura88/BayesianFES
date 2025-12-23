package org.example.config;

/**
 * Centralizes the grid layout used to index and arrange pads.
 */
public final class GridConfig {
    private final int rows;
    private final int cols;

    private GridConfig(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Creates the default grid configuration used by the simulation.
     *
     * @return a grid configuration with the current default dimensions
     */
    public static GridConfig defaultConfig() {
        return new GridConfig(5, 3);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    /**
     * Total number of pads in the grid.
     *
     * @return rows multiplied by columns
     */
    public int getPadCount() {
        return rows * cols;
    }
}
