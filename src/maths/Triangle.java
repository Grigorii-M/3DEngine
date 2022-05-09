package maths;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

public class Triangle {
    public final Vector3 v0;
    public final Vector3 v1;
    public final Vector3 v2;
    public final Color color;

    public Triangle(Vector3 v0, Vector3 v1, Vector3 v2, Color color) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.color = color;
    }

    public Triangle(Vector3 v0, Vector3 v1, Vector3 v2) {
        this(v0, v1, v2, getRandomColor());
    }

    private static Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextFloat(1), random.nextFloat(1), random.nextFloat(1));
    }

    public static double edgeFunction(Vector3 v0, Vector3 v1, Vector3 point) {
        Matrix m = new Matrix(new double[] {
                point.x - v0.x, point.y - v0.y,
                v1.x - v0.x, v1.y - v0.y}, 2, 2);
        return m.getDeterminant();
    }

    public static double edgeFunction(Vector3 v0, Vector3 v1, Vector2 point) {
        Matrix m = new Matrix(new double[] {
                point.x - v0.x, point.y - v0.y,
                v1.x - v0.x, v1.y - v0.y}, 2, 2);
        return m.getDeterminant();
    }

    public Vector3 getNormal() {
        Vector3 vectorA = new Vector3(v1.x - v0.x, v1.y - v0.y, v1.z - v0.z);
        Vector3 vectorB = new Vector3(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
        return Vector3.crossProduct(vectorA, vectorB);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Triangle) obj;
        return Objects.equals(this.v0, that.v0) &&
                Objects.equals(this.v1, that.v1) &&
                Objects.equals(this.v2, that.v2) &&
                Objects.equals(this.color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v0, v1, v2, color);
    }

    @Override
    public String toString() {
        return "maths.Triangle[" +
                "v0=" + v0 + ", " +
                "v1=" + v1 + ", " +
                "v2=" + v2 + ", " +
                "color=" + color + ']';
    }
}
