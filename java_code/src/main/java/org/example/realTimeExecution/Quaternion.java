package org.example.realTimeExecution;

/**
 * Representa un cuaternión y ofrece operaciones básicas para combinar orientaciones
 * o convertirlas a ángulos de Euler.
 */
public class Quaternion {

    // q = w + xi + yi + zi
    private double w;
    private double x;
    private double y;
    private double z;

    public Quaternion() {
    }

    /**
     * Crea un nuevo cuaternión con los componentes indicados.
     */
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Multiplica el cuaternión actual por otro cuaternión.
     *
     * @param q cuaternión a aplicar.
     * @return resultado de la multiplicación {@code this * q}.
     */
    public Quaternion multiplication(Quaternion q){
        // w = w1*w2 - x1*x2 - y1*y2 - z1*z2
        double newW = this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z;
        // x = w1*x2 + x1*w2 + y1*z2 - z1*y2
        double newX = this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y;
        // y = w1*y2 - x1*z2 + y1*w2 + z1*x2
        double newY = this.w * q.y - this.x * q.z + this.y * q.w + this.z * q.x;
        // z = w1*z2 + x1*y2 + y1*x2 + z1*w2
        double newZ = this.w * q.z + this.x * q.y - this.y * q.x + this.z * q.w;
        return new Quaternion(newW, newX, newY, newZ);
    }

    //q^-1 = q* / |q^2|
    // q* = (w, -x, -y, -z)
    // |q^2| = w^2 + x^2 + y^2 + z^2
    /**
     * Calcula el inverso del cuaternión actual.
     *
     * @return cuaternión invertido.
     */
    public Quaternion inversion(){
        double Squared = w * w + x * x + y * y + z * z;
        return new Quaternion(w/Squared, -x/Squared, -y/Squared, -z/Squared);

    }

    /**
     * Convierte el cuaternión en ángulos de Euler expresados en grados (roll, pitch, yaw).
     */
    public Coord toEulerAngles(){
        double roll, pitch, yaw;
        Coord c = new Coord();

        roll = Math.atan2(2.0 * (w * x + y * z), 1.0 - 2.0 * (x * x + y * y)); //rotacion en el eje x
        pitch = Math.asin(2.0 * (w * y - z * x)); //rotacion en el eje Y
        yaw = Math.atan2(2.0 * (w * z + x * y), 1.0 - 2.0 * (y * y + z * z)); //rotacion en el eje Z

        roll = Math.toDegrees(roll);
        pitch = Math.toDegrees(pitch);
        yaw = Math.toDegrees(yaw);

        c.setX(roll);
        c.setY(pitch);
        c.setZ(yaw);

        return c;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

}
