public class Vector2D {
    private final double[] values;
    private Double magnitude = null;

    public Vector2D(double x, double y) {
        this.values = new double[] {x, y};
    }

    public double x() {
        return values[0];
    }

    public double y() {
        return values[1];
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

    public Vector2D normalize() {
        double[] newValues = new double[values.length];
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = values[i] / getMagnitude();
        }

        return new Vector2D(newValues[0], newValues[1]);
    }

    public double dotProduct(Vector2D vector) {
        if (values.length != vector.values.length) {
            throw new IllegalArgumentException("Vectors are not of the same length");
        }

        double dotProduct = 0;
        for (int i = 0; i < values.length; i++) {
            dotProduct += values[i] * vector.values[i];
        }

        return dotProduct;
    }

    public double multiply(Vector2D v) {
        double res = 0;
        for (int i = 0; i < values.length; i++) {
            res += v.values[i] * values[i];
        }

        return res;
    }

    public Vector2D applyTransformation(Matrix transformation) {
        double[] newValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            double value = 0;
            for (int j = 0; j < transformation.getRows(); j++) {
                value += (j < values.length ? values[j] : 1) * transformation.get(j, i);
            }
            newValues[i] = value;
        }

        return new Vector2D(newValues[0], newValues[1]);
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
