package maths;

public class Vector4 {

    private final double[] values;

    public final double x;
    public final double y;
    public final double z;

    public final double w;

    public Vector4(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 1;
        this.values = new double[] {x, y, z, w};
    }

    public Vector4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.values = new double[] {x, y, z, w};
    }

    public Vector3 getAsVector3() {
        return new Vector3(x / w, y / w, z / w);
    }

    public Vector4 multiplyByMatrix(Matrix matrix) {
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

        return new Vector4(newValues[0], newValues[1], newValues[2], newValues[3]);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }
}
