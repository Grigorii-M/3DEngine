package maths;

public class Vector2 {
    private final double[] values;
    public final double x;
    public final double y;
    private Double magnitude = null;

    public Vector2(double x, double y) {
        this.values = new double[] {x, y};
        this.x = x;
        this.y = y;
    }

    public Vector3 getAsVector3() {
        return new Vector3(x, y, 0);
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

    public Vector2 normalize() {
        double[] newValues = new double[values.length];
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = values[i] / getMagnitude();
        }

        return new Vector2(newValues[0], newValues[1]);
    }

    public double dotProduct(Vector2 vector) {
        if (values.length != vector.values.length) {
            throw new IllegalArgumentException("Vectors are not of the same length");
        }

        double dotProduct = 0;
        for (int i = 0; i < values.length; i++) {
            dotProduct += values[i] * vector.values[i];
        }

        return dotProduct;
    }

    public Vector2 applyTransformation(Matrix transformation) {
        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            double value = 0;
            for (int j = 0; j < transformation.getRows(); j++) {
                value += (j < values.length ? values[j] : 1) * transformation.get(j, i);
            }
            newValues[i] = value;
        }

        return new Vector2(newValues[0], newValues[1]);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
