package org.example.config;

/**
 * Centralizes the grid layout used to index and arrange pads.
 * <p>
 * The configuration is immutable so the total pad count and the mapping between
 * row/column positions and pad identifiers remains stable throughout a
 * simulation run.
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

    /**
     * Number of rows in the pad grid.
     *
     * @return configured row count
     */
    public int getRows() {
        return rows;
    }

    /**
     * Number of columns in the pad grid.
     *
     * @return configured column count
     */
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
