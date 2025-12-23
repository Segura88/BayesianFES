package org.example.realTimeExecution;

/**
 * Estructura simple para almacenar ángulos de Euler (roll, pitch, yaw) en grados.
 */
public class Coord {
    private double x;
    private double y;
    private double z;

    /**
     * Crea una estructura con valores iniciales.
     */
    public Coord(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coord() {
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Coordenadas{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
