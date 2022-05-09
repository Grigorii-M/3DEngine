package maths;

import java.util.ArrayList;
import java.util.List;

public class Vector3 {
    private final double[] values;
    
    public final double x;
    public final double y;
    public final double z;
    private Double magnitude = null;

    public Vector3(double x, double y, double z) {
        this.values = new double[] {x, y, z};
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector2 getAsVector2D() {
        return new Vector2(x, y);
    }

    public Vector3 minus(Vector3 v) {
        return new Vector3(x - v.x, y - v.y, z - v.z);
    }

    public static Vector3 crossProduct(Vector3 vector1, Vector3 vector2) {

        double x = vector1.y * vector2.z - vector1.z * vector2.y;
        double y = vector1.z * vector2.x - vector1.x * vector2.z;
        double z = vector1.x * vector2.y - vector1.y * vector2.x;

        return new Vector3(x, y, z);
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

    public Vector3 normalize() {
        double[] newValues = new double[values.length];
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = values[i] / getMagnitude();
        }

        return new Vector3(newValues[0], newValues[1], newValues[2]);
    }

    public double dotProduct(Vector3 vector) {
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
     * Uses vector in a row form to multiply by a matrix.
     * If a matrix has more rows than vector elements then the values are just added together
     * (use multiplyByMatrixStrict in order to avoid such behaviour).
     */
    public Vector3 multiplyByMatrix(Matrix matrix) {
        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            double value = 0;
            for (int j = 0; j < matrix.getRows(); j++) {
                value += (j < values.length ? values[j] : 1) * matrix.get(j, i);
            }
            newValues[i] = value;
        }

        return new Vector3(newValues[0], newValues[1], newValues[2]);
    }

    public Vector3 multiplyByMatrixStrict(Matrix matrix) {
        if (values.length != matrix.getRows()) {
            throw new IllegalArgumentException("Matrix rows does not equal to vector elements");
        }

        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            double value = 0;
            for (int j = 0; j < matrix.getRows(); j++) {
                value += values[j] * matrix.get(j, i);
            }
            newValues[i] = value;
        }

        return new Vector3(newValues[0], newValues[1], newValues[2]);
    }

    /**
     * Sum over the edges, (x2 − x1) * (y2 + y1).
     * If the result is positive the curve is clockwise, if it's negative the curve is counter-clockwise.
     * (The result is twice the enclosed area, with a +/- convention.)
     */
    public static boolean isClockwise(ArrayList<Vector3> points) {
        double sum = 0;
        for (int i = 1; i < points.size(); i++) {
            sum += (points.get(i).x - points.get(i - 1).x) * (points.get(i).y + points.get(i - 1).y);
        }
        sum += (points.get(0).x - points.get(points.size() - 1).x) * (points.get(0).y + points.get(points.size() - 1).y);
        return sum < 0; // In raster space Oy is pointing downwards, therefore the answer should be flipped
    }

    public static Vector3 findCentroid(List<Vector3> points) {
        double x = 0, y = 0, z = 0;
        for (Vector3 p : points) {
            x += p.x;
            y += p.y;
            z += p.z;
        }
        return new Vector3(x / points.size(), y / points.size(), z / points.size());
    }

    public static void sortPointsInCounterClockwiseFashion(ArrayList<Vector3> points) {
        Vector3 center = findCentroid(points);
        points.sort((a, b) -> {
            double a1 = (Math.toDegrees(Math.atan2(a.x - center.x, a.y - center.y)) + 360) % 360;
            double a2 = (Math.toDegrees(Math.atan2(b.x - center.x, b.y - center.y)) + 360) % 360;
            return (int) (a2 - a1);
        });
    }

    public static void sortPointsInClockwiseFashion(ArrayList<Vector3> points) {
        Vector3 center = findCentroid(points);
        points.sort((a, b) -> {
            double a1 = (Math.toDegrees(Math.atan2(a.x - center.x, a.y - center.y)) + 360) % 360;
            double a2 = (Math.toDegrees(Math.atan2(b.x - center.x, b.y - center.y)) + 360) % 360;
            return (int) (a1 - a2);
        });
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }
}
