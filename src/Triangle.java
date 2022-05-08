import java.awt.*;
import java.util.Objects;

public final class Triangle {
    public Vertex v1;
    public Vertex v2;
    public Vertex v3;
    public Color color;

    public Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }

    public Vector3D getNormal() {
        Vector3D vectorA = new Vector3D(v2.x() - v1.x(), v2.y() - v1.y(), v2.z() - v1.z());
        Vector3D vectorB = new Vector3D(v3.x() - v1.x(), v3.y() - v1.y(), v3.z() - v1.z());
        return Vector3D.crossProduct(vectorA, vectorB);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Triangle) obj;
        return Objects.equals(this.v1, that.v1) &&
                Objects.equals(this.v2, that.v2) &&
                Objects.equals(this.v3, that.v3) &&
                Objects.equals(this.color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v1, v2, v3, color);
    }

    @Override
    public String toString() {
        return "Triangle[" +
                "v1=" + v1 + ", " +
                "v2=" + v2 + ", " +
                "v3=" + v3 + ", " +
                "color=" + color + ']';
    }
}
