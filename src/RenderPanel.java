import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class RenderPanel extends JPanel {
        Matrix cameraToWorld = new Matrix(new double[] {
                0.871214, 0, -0.490904, 0,
                -0.192902, 0.919559, -0.342346, 0,
                0.451415, 0.392953, 0.801132, 0,
                14.777467, 29.361945, 27.993464, 1}, 4, 4);

//    private final Matrix cameraToWorld = new Matrix(new double[] {
//            1, 0, 0, 0,
//            0, 1, 0, 0,
//            0, 1, 1, 0,
//            0, 3, 3, 1}, 4, 4);
    private final Matrix woldToCamera = cameraToWorld.getInverse();
    private ArrayList<Triangle> currentObject;

    private final JSlider yawSlider;
    private final JSlider pitchSlider;
    private final JSlider rollSlider;

    // Distance from the 'eye' to the screen
    private final double nearClippingPlane = 1;

    // FOV ???
    double canvasWidth = 2;
    double canvasHeight = 2;

    public RenderPanel(JSlider yawSlider, JSlider pitchSlider, JSlider rollSlider) {
        this.yawSlider = yawSlider;
        this.pitchSlider = pitchSlider;
        this.rollSlider = rollSlider;
        setPreferredSize(new Dimension(512, 512));
    }


    public void paintComponent(Graphics g) {
        Matrix xzRotation = Matrix.getXZRightHandedRotationMatrix(yawSlider.getValue());
        Matrix yzRotation = Matrix.getYZRightHandedRotationMatrixAlt(pitchSlider.getValue());
        Matrix xyRotation = Matrix.getXYRightHandedRotationMatrix(rollSlider.getValue());
        Matrix rotationMatrix = xzRotation.multiply(yzRotation).multiply(xyRotation);

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.setColor(Color.WHITE);

        // Fancy camera position


        if (currentObject == null || currentObject.isEmpty()) {
            currentObject = getXMasTreeTriangles();
        }

        for (Triangle triangle : currentObject) {
            Path2D edgePath = new Path2D.Double();

            // Get triangle vertices as vectors
            Vector3D v0Space = triangle.v0.getAsVector3D();
            Vector3D v1Space = triangle.v1.getAsVector3D();
            Vector3D v2Space = triangle.v2.getAsVector3D();

            // Apply transformation
            // Todo: add zoom and translation
            v0Space = v0Space.applyTransformation(rotationMatrix);
            v1Space = v1Space.applyTransformation(rotationMatrix);
            v2Space = v2Space.applyTransformation(rotationMatrix);

            // Get raster coordinates
            Vector3D v0Raster = computePixelCoordinates(v0Space);
            Vector3D v1Raster = computePixelCoordinates(v1Space);
            Vector3D v2Raster = computePixelCoordinates(v2Space);
            ArrayList<Vector3D> points = new ArrayList<>(List.of(v0Raster, v1Raster, v2Raster));

            // Ensure that vertices are in a counterclockwise order
            if (Vector3D.isClockwise(points)) {
                ArrayList<Vector3D> counterClockwiseVertices = Vector3D.sortPointsInCounterClockwiseFashion(points);
                v0Raster = counterClockwiseVertices.get(0);
                v1Raster = counterClockwiseVertices.get(1);
                v2Raster = counterClockwiseVertices.get(2);
            }

            // Compute bounding box
            double boundingBoxXMin = Math.min(v0Raster.x(), Math.min(v1Raster.x(), v2Raster.x()));
            double boundingBoxXMax = Math.max(v0Raster.x(), Math.max(v1Raster.x(), v2Raster.x()));
            double boundingBoxYMin = Math.min(v0Raster.y(), Math.min(v1Raster.y(), v2Raster.y()));
            double boundingBoxYMax = Math.max(v0Raster.y(), Math.max(v1Raster.y(), v2Raster.y()));

            // Clamp bounding box inside the canvas
            int xMin = Math.max(0, Math.min((int) Math.floor(boundingBoxXMin), getWidth() - 1));
            int xMax = Math.max(0, Math.min((int) Math.floor(boundingBoxXMax), getWidth() - 1));
            int yMin = Math.max(0, Math.min((int) Math.floor(boundingBoxYMin), getHeight() - 1));
            int yMax = Math.max(0, Math.min((int) Math.floor(boundingBoxYMax), getHeight() - 1));

            // Draw wireframe
            edgePath.moveTo(v0Raster.x(), v0Raster.y());
            edgePath.lineTo(v1Raster.x(), v1Raster.y());
            edgePath.lineTo(v2Raster.x(), v2Raster.y());
            edgePath.closePath();

            graphics2D.setColor(triangle.color);
            graphics2D.draw(edgePath);
        }
//        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
//        for (int y = 0; y < image.getHeight(); y++) {
//            for (int x = 0; x < image.getWidth(); x++) {
//                image.setRGB(x, y, Color.BLACK.getRGB());
//            }
//        }
//        graphics2D.drawImage(image, 0, 0, null);
    }

    private Vector3D computePixelCoordinates(Vector3D point) {
        Vector3D pointInCameraSpace = point.applyTransformation(woldToCamera);
        Vector2D pointInScreenSpace = new Vector2D(
                nearClippingPlane * pointInCameraSpace.x() / -pointInCameraSpace.z(),
                nearClippingPlane * pointInCameraSpace.y() / -pointInCameraSpace.z()
        );

        // Normalize to [0, 1]
//        Vector2D pointNormalized = new Vector2D(
//                (pointInScreenSpace.x() + canvasWidth / 2) / canvasWidth,
//                (pointInScreenSpace.y() + canvasHeight / 2) / canvasHeight
//        );

        // Normalize to [-1, 1]
        double rightScreenCoordinate = getWidth() - 1;
        double leftScreenCoordinate = 0;
        double topScreenCoordinate = 0;
        double bottomScreenCoordinate = getHeight() - 1;

        Vector2D pointNormalized = new Vector2D(
                2 * pointInScreenSpace.x() / (rightScreenCoordinate - leftScreenCoordinate)
                        - (rightScreenCoordinate + leftScreenCoordinate) / (rightScreenCoordinate - leftScreenCoordinate),
                2 * pointInScreenSpace.y() / (topScreenCoordinate - bottomScreenCoordinate)
                        - (topScreenCoordinate + bottomScreenCoordinate) / (topScreenCoordinate - bottomScreenCoordinate)
        );

        // Get raster coordinates
        return new Vector3D(
                Math.floor((pointInScreenSpace.x() + 1) / 2 * getWidth()),
                Math.floor((1 - pointInScreenSpace.y()) / 2 * getHeight()),
                -pointInCameraSpace.z() // Store z value (depth) for z-buffering later
        );
    }

    private boolean isPointInsideTriangle(Triangle triangle, Vector2D point) {
        return Triangle.edgeFunction(triangle.v0, triangle.v1, point) && Triangle.edgeFunction(triangle.v1, triangle.v2, point) && Triangle.edgeFunction(triangle.v2, triangle.v0, point);
    }

    public void setObjectToPaint(ArrayList<Triangle> triangles) {
        currentObject = triangles;
        repaint();
    }

    private ArrayList<Triangle> getXMasTreeTriangles() {
        ArrayList<Vertex> vertices = new ArrayList<>();
        vertices.add(new Vertex(        0,    39.034,         0));vertices.add(new Vertex(  0.76212,    36.843,         0));
        vertices.add(new Vertex(        3,    36.604,         0));vertices.add(new Vertex(        1,    35.604,         0));
        vertices.add(new Vertex(   2.0162,    33.382,         0));vertices.add(new Vertex(        0,    34.541,         0));
        vertices.add(new Vertex(  -2.0162,    33.382,         0));vertices.add(new Vertex(       -1,    35.604,         0));
        vertices.add(new Vertex(       -3,    36.604,         0));vertices.add(new Vertex( -0.76212,    36.843,         0));
        vertices.add(new Vertex(-0.040181,     34.31,         0));vertices.add(new Vertex(   3.2778,    30.464,         0));
        vertices.add(new Vertex(-0.040181,    30.464,         0));vertices.add(new Vertex(-0.028749,    30.464,         0));
        vertices.add(new Vertex(   3.2778,    30.464,         0));vertices.add(new Vertex(   1.2722,    29.197,         0));
        vertices.add(new Vertex(   1.2722,    29.197,         0));vertices.add(new Vertex(-0.028703,    29.197,         0));
        vertices.add(new Vertex(   1.2722,    29.197,         0));vertices.add(new Vertex(   5.2778,    25.398,         0));
        vertices.add(new Vertex( -0.02865,    25.398,         0));vertices.add(new Vertex(   1.2722,    29.197,         0));
        vertices.add(new Vertex(   5.2778,    25.398,         0));vertices.add(new Vertex(   3.3322,    24.099,         0));
        vertices.add(new Vertex(-0.028683,    24.099,         0));vertices.add(new Vertex(   7.1957,    20.299,         0));
        vertices.add(new Vertex( -0.02861,    20.299,         0));vertices.add(new Vertex(   5.2778,    19.065,         0));
        vertices.add(new Vertex(-0.028663,    18.984,         0));vertices.add(new Vertex(   9.2778,    15.265,         0));
        vertices.add(new Vertex(-0.028571,    15.185,         0));vertices.add(new Vertex(   9.2778,    15.265,         0));
        vertices.add(new Vertex(   7.3772,    13.999,         0));vertices.add(new Vertex(-0.028625,    13.901,         0));
        vertices.add(new Vertex(   9.2778,    15.265,         0));vertices.add(new Vertex(   12.278,    8.9323,         0));
        vertices.add(new Vertex(-0.028771,    8.9742,         0));vertices.add(new Vertex(   12.278,    8.9323,         0));
        vertices.add(new Vertex(   10.278,    7.6657,         0));vertices.add(new Vertex(-0.028592,    7.6552,         0));
        vertices.add(new Vertex(   15.278,    2.5994,         0));vertices.add(new Vertex(-0.028775,    2.6077,         0));
        vertices.add(new Vertex(   15.278,    2.5994,         0));vertices.add(new Vertex(   13.278,    1.3329,         0));
        vertices.add(new Vertex(-0.028727,    1.2617,         0));vertices.add(new Vertex(   18.278,   -3.7334,         0));
        vertices.add(new Vertex(   18.278,   -3.7334,         0));vertices.add(new Vertex(   2.2722,   -1.2003,         0));
        vertices.add(new Vertex(-0.028727,   -1.3098,         0));vertices.add(new Vertex(   4.2722,        -5,         0));
        vertices.add(new Vertex(   4.2722,        -5,         0));vertices.add(new Vertex(-0.028727,        -5,         0));
        vertices.add(new Vertex(  -3.3582,    30.464,         0));vertices.add(new Vertex(  -3.3582,    30.464,         0));
        vertices.add(new Vertex(  -1.3526,    29.197,         0));vertices.add(new Vertex(  -1.3526,    29.197,         0));
        vertices.add(new Vertex(  -1.3526,    29.197,         0));vertices.add(new Vertex(  -5.3582,    25.398,         0));
        vertices.add(new Vertex(  -1.3526,    29.197,         0));vertices.add(new Vertex(  -5.3582,    25.398,         0));
        vertices.add(new Vertex(  -3.4126,    24.099,         0));vertices.add(new Vertex(   -7.276,    20.299,         0));
        vertices.add(new Vertex(  -5.3582,    19.065,         0));vertices.add(new Vertex(  -9.3582,    15.265,         0));
        vertices.add(new Vertex(  -9.3582,    15.265,         0));vertices.add(new Vertex(  -7.4575,    13.999,         0));
        vertices.add(new Vertex(  -9.3582,    15.265,         0));vertices.add(new Vertex(  -12.358,    8.9323,         0));
        vertices.add(new Vertex(  -12.358,    8.9323,         0));vertices.add(new Vertex(  -10.358,    7.6657,         0));
        vertices.add(new Vertex(  -15.358,    2.5994,         0));vertices.add(new Vertex(  -15.358,    2.5994,         0));
        vertices.add(new Vertex(  -13.358,    1.3329,         0));vertices.add(new Vertex(  -18.358,   -3.7334,         0));
        vertices.add(new Vertex(  -18.358,   -3.7334,         0));vertices.add(new Vertex(  -2.3526,   -1.2003,         0));
        vertices.add(new Vertex(  -4.3526,        -5,         0));vertices.add(new Vertex(  -4.3526,        -5,         0));
        vertices.add(new Vertex(        0,     34.31,  0.040181));vertices.add(new Vertex(        0,    30.464,   -3.2778));
        vertices.add(new Vertex(        0,    30.464,  0.040181));vertices.add(new Vertex(        0,    30.464,  0.028749));
        vertices.add(new Vertex(        0,    30.464,   -3.2778));vertices.add(new Vertex(        0,    29.197,   -1.2722));
        vertices.add(new Vertex(        0,    29.197,   -1.2722));vertices.add(new Vertex(        0,    29.197,  0.028703));
        vertices.add(new Vertex(        0,    29.197,   -1.2722));vertices.add(new Vertex(        0,    25.398,   -5.2778));
        vertices.add(new Vertex(        0,    25.398,   0.02865));vertices.add(new Vertex(        0,    29.197,   -1.2722));
        vertices.add(new Vertex(        0,    25.398,   -5.2778));vertices.add(new Vertex(        0,    24.099,   -3.3322));
        vertices.add(new Vertex(        0,    24.099,  0.028683));vertices.add(new Vertex(        0,    20.299,   -7.1957));
        vertices.add(new Vertex(        0,    20.299,   0.02861));vertices.add(new Vertex(        0,    19.065,   -5.2778));
        vertices.add(new Vertex(        0,    18.984,  0.028663));vertices.add(new Vertex(        0,    15.265,   -9.2778));
        vertices.add(new Vertex(        0,    15.185,  0.028571));vertices.add(new Vertex(        0,    15.265,   -9.2778));
        vertices.add(new Vertex(        0,    13.999,   -7.3772));vertices.add(new Vertex(        0,    13.901,  0.028625));
        vertices.add(new Vertex(        0,    15.265,   -9.2778));vertices.add(new Vertex(        0,    8.9323,   -12.278));
        vertices.add(new Vertex(        0,    8.9742,  0.028771));vertices.add(new Vertex(        0,    8.9323,   -12.278));
        vertices.add(new Vertex(        0,    7.6657,   -10.278));vertices.add(new Vertex(        0,    7.6552,  0.028592));
        vertices.add(new Vertex(        0,    2.5994,   -15.278));vertices.add(new Vertex(        0,    2.6077,  0.028775));
        vertices.add(new Vertex(        0,    2.5994,   -15.278));vertices.add(new Vertex(        0,    1.3329,   -13.278));
        vertices.add(new Vertex(        0,    1.2617,  0.028727));vertices.add(new Vertex(        0,   -3.7334,   -18.278));
        vertices.add(new Vertex(        0,   -3.7334,   -18.278));vertices.add(new Vertex(        0,   -1.2003,   -2.2722));
        vertices.add(new Vertex(        0,   -1.3098,  0.028727));vertices.add(new Vertex(        0,        -5,   -4.2722));
        vertices.add(new Vertex(        0,        -5,   -4.2722));vertices.add(new Vertex(        0,        -5,  0.028727));
        vertices.add(new Vertex(        0,    30.464,    3.3582));vertices.add(new Vertex(        0,    30.464,    3.3582));
        vertices.add(new Vertex(        0,    29.197,    1.3526));vertices.add(new Vertex(        0,    29.197,    1.3526));
        vertices.add(new Vertex(        0,    29.197,    1.3526));vertices.add(new Vertex(        0,    25.398,    5.3582));
        vertices.add(new Vertex(        0,    29.197,    1.3526));vertices.add(new Vertex(        0,    25.398,    5.3582));
        vertices.add(new Vertex(        0,    24.099,    3.4126));vertices.add(new Vertex(        0,    20.299,     7.276));
        vertices.add(new Vertex(        0,    19.065,    5.3582));vertices.add(new Vertex(        0,    15.265,    9.3582));
        vertices.add(new Vertex(        0,    15.265,    9.3582));vertices.add(new Vertex(        0,    13.999,    7.4575));
        vertices.add(new Vertex(        0,    15.265,    9.3582));vertices.add(new Vertex(        0,    8.9323,    12.358));
        vertices.add(new Vertex(        0,    8.9323,    12.358));vertices.add(new Vertex(        0,    7.6657,    10.358));
        vertices.add(new Vertex(        0,    2.5994,    15.358));vertices.add(new Vertex(        0,    2.5994,    15.358));
        vertices.add(new Vertex(        0,    1.3329,    13.358));vertices.add(new Vertex(        0,   -3.7334,    18.358));
        vertices.add(new Vertex(        0,   -3.7334,    18.358));vertices.add(new Vertex(        0,   -1.2003,    2.3526));
        vertices.add(new Vertex(        0,        -5,    4.3526));vertices.add(new Vertex(        0,        -5,    4.3526));

        int[] indices = new int[] {
                8,   7,   9,   6,   5,   7,   4,   3,   5,   2,   1,   3,   0,   9,   1,
                5,   3,   7,   7,   3,   9,   9,   3,   1,  10,  12,  11,  13,  15,  14,
                15,  13,  16,  13,  17,  16,  18,  20,  19,  17,  20,  21,  20,  23,  22,
                20,  24,  23,  23,  26,  25,  24,  26,  23,  26,  27,  25,  26,  28,  27,
                27,  30,  29,  28,  30,  27,  30,  32,  31,  30,  33,  32,  27,  30,  34,
                32,  36,  35,  33,  36,  32,  36,  38,  37,  36,  39,  38,  38,  41,  40,
                39,  41,  38,  41,  43,  42,  41,  44,  43,  44,  45,  43,  44,  47,  46,
                44,  48,  47,  48,  49,  47,  48,  51,  50,  10,  52,  12,  13,  53,  54,
                55,  17,  54,  13,  54,  17,  56,  57,  20,  17,  58,  20,  20,  59,  60,
                20,  60,  24,  60,  61,  26,  24,  60,  26,  26,  61,  62,  26,  62,  28,
                62,  63,  30,  28,  62,  30,  30,  64,  65,  30,  65,  33,  62,  66,  30,
                65,  67,  36,  33,  65,  36,  36,  68,  69,  36,  69,  39,  69,  70,  41,
                39,  69,  41,  41,  71,  72,  41,  72,  44,  44,  72,  73,  44,  74,  75,
                44,  75,  48,  48,  75,  76,  48,  77,  51,  78,  80,  79,  81,  83,  82,
                83,  81,  84,  81,  85,  84,  86,  88,  87,  85,  88,  89,  88,  91,  90,
                88,  92,  91,  91,  94,  93,  92,  94,  91,  94,  95,  93,  94,  96,  95,
                95,  98,  97,  96,  98,  95,  98, 100,  99,  98, 101, 100,  95,  98, 102,
                100, 104, 103, 101, 104, 100, 104, 106, 105, 104, 107, 106, 106, 109, 108,
                107, 109, 106, 109, 111, 110, 109, 112, 111, 112, 113, 111, 112, 115, 114,
                112, 116, 115, 116, 117, 115, 116, 119, 118,  78, 120,  80,  81, 121, 122,
                123,  85, 122,  81, 122,  85, 124, 125,  88,  85, 126,  88,  88, 127, 128,
                88, 128,  92, 128, 129,  94,  92, 128,  94,  94, 129, 130,  94, 130,  96,
                130, 131,  98,  96, 130,  98,  98, 132, 133,  98, 133, 101, 130, 134,  98,
                133, 135, 104, 101, 133, 104, 104, 136, 137, 104, 137, 107, 137, 138, 109,
                107, 137, 109, 109, 139, 140, 109, 140, 112, 112, 140, 141, 112, 142, 143,
                112, 143, 116, 116, 143, 144, 116, 145, 119
        };
        ArrayList<Triangle> triangles = new ArrayList<>();

        for (int i = 0; i < indices.length; i += 3) {
            triangles.add(new Triangle(vertices.get(indices[i]), vertices.get(indices[i + 1]), vertices.get(indices[i + 2])));
        }

        return triangles;
    }
}