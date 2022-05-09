import java.util.ArrayList;
import java.util.List;

public class Vector3D {
    private final double[] values;
    private Double magnitude = null;

    public Vector3D(double x, double y, double z) {
        this.values = new double[] {x, y, z};
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

        return new Vector3D(newValues[0], newValues[1], newValues[2]);
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

        return new Vector3D(newValues[0], newValues[1], newValues[2]);
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

    public double multiply(Vector3D v) {
        double res = 0;
        for (int i = 0; i < values.length; i++) {
            res += v.values[i] * values[i];
        }

        return res;
    }

    public Vector3D applyTransformation(Matrix transformation) {
        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            double value = 0;
            for (int j = 0; j < transformation.getRows(); j++) {
                value += (j < values.length ? values[j] : 1) * transformation.get(j, i);
            }
            newValues[i] = value;
        }

        return new Vector3D(newValues[0], newValues[1], newValues[2]);
    }

    /**
     * Sum over the edges, (x2 − x1)(y2 + y1).
     * If the result is positive the curve is clockwise, if it's negative the curve is counter-clockwise.
     * (The result is twice the enclosed area, with a +/- convention.)
     */
    public static boolean isClockwise(ArrayList<Vector3D> points) {
        double sum = 0;
        for (int i = 1; i < points.size(); i++) {
            sum += (points.get(i).x() - points.get(i - 1).x()) * (points.get(i).y() + points.get(i - 1).y());
        }
        sum += (points.get(0).x() - points.get(points.size() - 1).x()) * (points.get(0).y() + points.get(points.size() - 1).y());
        return sum > 0;
    }

    public static Vector3D findCentroid(List<Vector3D> points) {
        double x = 0, y = 0, z = 0;
        for (Vector3D p : points) {
            x += p.x();
            y += p.y();
            z += p.z();
        }
        return new Vector3D(x / points.size(), y / points.size(), z / points.size());
    }

    public static ArrayList<Vector3D> sortPointsInCounterClockwiseFashion(ArrayList<Vector3D> points) {
        Vector3D center = findCentroid(points);
        points.sort((a, b) -> {
            double a1 = (Math.toDegrees(Math.atan2(a.x() - center.x(), a.y() - center.y())) + 360) % 360;
            double a2 = (Math.toDegrees(Math.atan2(b.x() - center.x(), b.y() - center.y())) + 360) % 360;
            return (int) (a2 - a1);
        });
        return points;
    }

    public static ArrayList<Vector3D> sortPointsInClockwiseFashion(ArrayList<Vector3D> points) {
        Vector3D center = findCentroid(points);
        points.sort((a, b) -> {
            double a1 = (Math.toDegrees(Math.atan2(a.x() - center.x(), a.y() - center.y())) + 360) % 360;
            double a2 = (Math.toDegrees(Math.atan2(b.x() - center.x(), b.y() - center.y())) + 360) % 360;
            return (int) (a1 - a2);
        });
        return points;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("[");
        for (double value : values) {
            output.append(value).append(", ");
        }
        output.replace(output.length() - 2, output.length(), "]");
        return String.valueOf(output);
    }
}
