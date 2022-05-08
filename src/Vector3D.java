import java.util.Arrays;

public class Vector3D {
    private final double[] values;
    private Double magnitude = null;

    public Vector3D(double... values) {
        if (values.length != 3) {
            throw new IllegalArgumentException("");
        }
        this.values = values;
    }

    public double x() {
        return values[0];
    }

    public double y() {
        return values[1];
    }

    public double z() {
        return values[2];
    }

    public static Vector3D crossProduct(Vector3D vector1, Vector3D vector2) {

        double[] newValues = new double[vector1.values.length];
        newValues[0] = vector1.values[1] * vector2.values[2] - vector1.values[2] * vector2.values[1];
        newValues[1] = vector1.values[2] * vector2.values[0] - vector1.values[0] * vector2.values[2];
        newValues[2] = vector1.values[0] * vector2.values[1] - vector1.values[1] * vector2.values[0];

        return new Vector3D(newValues);
    }

    public double getMagnitude() {
        if (magnitude != null) {
            return magnitude;
        }
        int mg = 0;

        for (double value : values) {
            mg += value * value;
        }

        magnitude = Math.sqrt(mg);
        return magnitude;
    }

    public Vector3D normalize() {
        double[] newValues = new double[values.length];
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = values[i] / getMagnitude();
        }

        return new Vector3D(newValues);
    }

    public double dotProduct(Vector3D vector) {
        if (values.length != vector.values.length) {
            throw new IllegalArgumentException("Vectors are not of the same length");
        }

        double dotProduct = 0;
        for (int i = 0; i < values.length; i++) {
            dotProduct += values[i] * vector.values[i];
        }

        return dotProduct;
    }

    /**
     * @return new Vector3D of the size of the input vector with all values set to 0
     */
    public static Vector3D one(Vector3D vector) {
        double[] newValues = new double[vector.values.length];
        Arrays.fill(newValues, 1);
        return new Vector3D(newValues);
    }
}
