/**
 * Coordinates of a vertex in a right-handed coordinate system,
 * with positive z pointing towards the viewer
 */
public class Vertex {

    public double x, y, z;

    public Vertex(double x, double y, double z) {
        this.x = 100 * x;
        this.y = 100 * y;
        this.z = 100 * z;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }
}
