package ui;

import cameras.PinholeRasterCamera;
import maths.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Todo: transfer rasterization algorithm into another class
public class RenderPanel extends JPanel {
    private PinholeRasterCamera camera;

    private Matrix woldToCamera;
    private ArrayList<Triangle> currentObject;

    private final JSlider yawSlider;
    private final JSlider pitchSlider;
    private final JSlider rollSlider;

    private boolean showWireframe = false;
    private boolean showFaces = true;
    private boolean showBoundingBoxes = false;

    // FOV ???
    double canvasWidth = 2;
    double canvasHeight = 2;

    private final JFrame parentFrame;

    public RenderPanel(JSlider yawSlider, JSlider pitchSlider, JSlider rollSlider, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.yawSlider = yawSlider;
        this.pitchSlider = pitchSlider;
        this.rollSlider = rollSlider;
        setPreferredSize(new Dimension(512, 512));
        camera = new PinholeRasterCamera();
    }

    public void paintComponent(Graphics g) {
        setPreferredSize(new Dimension((int) camera.getImageWidth(), (int) camera.getImageHeight()));
        parentFrame.setSize(getPreferredSize());
        woldToCamera = camera.getCameraMatrix().getInverse();
        Matrix xzRotation = Matrix.getXZRightHandedRotationMatrix(yawSlider.getValue());
        Matrix yzRotation = Matrix.getYZRightHandedRotationMatrixAlt(pitchSlider.getValue());
        Matrix xyRotation = Matrix.getXYRightHandedRotationMatrix(rollSlider.getValue());
        Matrix rotationMatrix = xzRotation.multiply(yzRotation).multiply(xyRotation);

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.setColor(Color.WHITE);

        if (currentObject == null || currentObject.isEmpty()) {
            currentObject = getXMasTreeTriangles();
        }

        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        double[][] depthBuffer = new double[image.getHeight()][image.getWidth()];
        for (double[] doubles : depthBuffer) {
            Arrays.fill(doubles, camera.getFarClippingPlane());
        }

        for (Triangle triangle : currentObject) {
            // Get triangle vertices as vectors
            Vector3 v0Space = triangle.v0;
            Vector3 v1Space = triangle.v1;
            Vector3 v2Space = triangle.v2;

            // Apply transformation
            // Todo: add scaling and translation
            v0Space = v0Space.multiplyByMatrix(rotationMatrix);
            v1Space = v1Space.multiplyByMatrix(rotationMatrix);
            v2Space = v2Space.multiplyByMatrix(rotationMatrix);

            // Get raster coordinates
            Vector3 v0Raster = convertToRaster(v0Space);
            Vector3 v1Raster = convertToRaster(v1Space);
            Vector3 v2Raster = convertToRaster(v2Space);
            ArrayList<Vector3> triangleVerticesRaster = new ArrayList<>(List.of(v0Raster, v1Raster, v2Raster));

            // Ensure that vertices are in a counterclockwise order
            if (Vector3.isClockwise(triangleVerticesRaster)) {
                Vector3.sortPointsInClockwiseFashion(triangleVerticesRaster); // Using this because screen y coordinate is flipped
            }

            // Compute bounding box
            double boundingBoxXMin = Math.min(triangleVerticesRaster.get(0).x, Math.min(triangleVerticesRaster.get(1).x, triangleVerticesRaster.get(2).x));
            double boundingBoxXMax = Math.max(triangleVerticesRaster.get(0).x, Math.max(triangleVerticesRaster.get(1).x, triangleVerticesRaster.get(2).x));
            double boundingBoxYMin = Math.min(triangleVerticesRaster.get(0).y, Math.min(triangleVerticesRaster.get(1).y, triangleVerticesRaster.get(2).y));
            double boundingBoxYMax = Math.max(triangleVerticesRaster.get(0).y, Math.max(triangleVerticesRaster.get(1).y, triangleVerticesRaster.get(2).y));

            // If bounding box is out of the screen, the triangle is not visible, therefore no need to proceed
            if (boundingBoxXMin > getWidth() - 1 || boundingBoxXMax < 0 || boundingBoxYMin > getHeight() - 1 || boundingBoxYMax < 0) {
                continue;
            }

            // Clamp bounding box inside the canvas
            int xMin = Math.max(0, Math.min((int) Math.floor(boundingBoxXMin), getWidth() - 1));
            int xMax = Math.max(0, Math.min((int) Math.floor(boundingBoxXMax), getWidth() - 1));
            int yMin = Math.max(0, Math.min((int) Math.floor(boundingBoxYMin), getHeight() - 1));
            int yMax = Math.max(0, Math.min((int) Math.floor(boundingBoxYMax), getHeight() - 1));

            if (showBoundingBoxes) {
                graphics2D.setColor(Color.RED);
                graphics2D.drawRect(xMin, yMin, xMax - xMin, yMax - yMin);
            }

            // Get triangle area (multiplied by two) using edge function
            double triangleArea2 = Triangle.edgeFunction(triangleVerticesRaster.get(0), triangleVerticesRaster.get(1), triangleVerticesRaster.get(2));

            // Draw wireframe
            if (showWireframe) {
                Path2D edgePath = new Path2D.Double();
                edgePath.moveTo(v0Raster.x, v0Raster.y);
                edgePath.lineTo(v1Raster.x, v1Raster.y);
                edgePath.lineTo(v2Raster.x, v2Raster.y);
                edgePath.closePath();

                graphics2D.setColor(triangle.color);
                graphics2D.draw(edgePath);
            }

            if (showFaces && !Vector3.isClockwise(triangleVerticesRaster)) {
                // Process only inside the bounding box
                for (int y = yMin; y < yMax; y++) {
                    for (int x = xMin; x < xMax; x++) {
                        // Compute barycentric coordinates in order to check whether the point (x, y) lies within the triangle
                        Vector2 pixel = new Vector2(x, y);
                        double w0 = Triangle.edgeFunction(triangleVerticesRaster.get(1), triangleVerticesRaster.get(2), pixel) / triangleArea2;
                        double w1 = Triangle.edgeFunction(triangleVerticesRaster.get(2), triangleVerticesRaster.get(0), pixel) / triangleArea2;
                        double w2 = Triangle.edgeFunction(triangleVerticesRaster.get(0), triangleVerticesRaster.get(1), pixel) / triangleArea2;

                        if (isPixelOverlapping(triangleVerticesRaster, List.of(w0, w1, w2))) {
                            double z = 1 / (
                                    1 / triangleVerticesRaster.get(0).z * w0
                                    + 1 / triangleVerticesRaster.get(1).z * w1
                                    + 1 / triangleVerticesRaster.get(2).z * w2);

                            if (z < depthBuffer[y][x]) {
                                depthBuffer[y][x] = z;
                                image.setRGB(x, y, triangle.color.getRGB());
                            }
                        }
                    }
                }
                graphics2D.drawImage(image, 0, 0, null);
            }
        }
    }

    /**
     * Checks whether a pixel on screen overlaps the triangle using barycentric coordinates.
     * If a pixel lies on the edge (at least one barycentric coordinate is zero) uses top-left rule
     * @param points vertices of the triangle
     * @param barycentricCoordinates barycentric coordinates of a point
     * @return true - if a pixel lies inside a triangle, false - otherwise
     */
    private boolean isPixelOverlapping(List<Vector3> points, List<Double> barycentricCoordinates) {
        Vector3 edge0 = points.get(2).minus(points.get(1));
        Vector3 edge1 = points.get(0).minus(points.get(1));
        Vector3 edge2 = points.get(1).minus(points.get(0));

        return ((barycentricCoordinates.get(0) == 0) ? ((edge0.y == 0 && edge0.x > 0) || edge0.y > 0) : barycentricCoordinates.get(0) > 0)
                && ((barycentricCoordinates.get(1) == 0) ? ((edge1.y == 0 && edge1.x > 0) || edge1.y > 0) : barycentricCoordinates.get(1) > 0)
                && ((barycentricCoordinates.get(2) == 0) ? ((edge2.y == 0 && edge2.x > 0) || edge2.y > 0) : barycentricCoordinates.get(2) > 0);
    }

    private Vector3 convertToRaster(Vector3 vertex) {
        Vector3 vertexInCameraSpace = vertex.multiplyByMatrix(woldToCamera);
        Vector2 vertexInScreenSpace = new Vector2(
                camera.getNearClippingPlane() * vertexInCameraSpace.x / -vertexInCameraSpace.z,
                camera.getNearClippingPlane() * vertexInCameraSpace.y / -vertexInCameraSpace.z
        );

        double r = camera.getCanvasRight();
        double l = camera.getCanvasLeft();
        double t = camera.getCanvasTop();
        double b = camera.getCanvasBottom();

        // Normalize to [-1, 1]
        Vector2 vertexNormalized = new Vector2(
                2 * vertexInScreenSpace.x / (r - l) - (r + l) / (r - l),
                2 * vertexInScreenSpace.y / (t - b) - (t + b) / (t - b)
        );

        // Get raster coordinates
        Vector3 vertexRaster = new Vector3(
                (vertexNormalized.x + 1) / 2 * camera.getImageWidth(),
                (1 - vertexNormalized.y) / 2 * camera.getImageHeight(),
                -vertexInCameraSpace.z // Store z value (depth) for z-buffering later
        );
        return vertexRaster;
    }

    public void setObjectToPaint(ArrayList<Triangle> triangles) {
        currentObject = triangles;
        repaint();
    }

    private ArrayList<Triangle> getXMasTreeTriangles() {
        ArrayList<Vector3> vertices = new ArrayList<>();
        vertices.add(new Vector3(        0,    39.034,         0));vertices.add(new Vector3(  0.76212,    36.843,         0));
        vertices.add(new Vector3(        3,    36.604,         0));vertices.add(new Vector3(        1,    35.604,         0));
        vertices.add(new Vector3(   2.0162,    33.382,         0));vertices.add(new Vector3(        0,    34.541,         0));
        vertices.add(new Vector3(  -2.0162,    33.382,         0));vertices.add(new Vector3(       -1,    35.604,         0));
        vertices.add(new Vector3(       -3,    36.604,         0));vertices.add(new Vector3( -0.76212,    36.843,         0));
        vertices.add(new Vector3(-0.040181,     34.31,         0));vertices.add(new Vector3(   3.2778,    30.464,         0));
        vertices.add(new Vector3(-0.040181,    30.464,         0));vertices.add(new Vector3(-0.028749,    30.464,         0));
        vertices.add(new Vector3(   3.2778,    30.464,         0));vertices.add(new Vector3(   1.2722,    29.197,         0));
        vertices.add(new Vector3(   1.2722,    29.197,         0));vertices.add(new Vector3(-0.028703,    29.197,         0));
        vertices.add(new Vector3(   1.2722,    29.197,         0));vertices.add(new Vector3(   5.2778,    25.398,         0));
        vertices.add(new Vector3( -0.02865,    25.398,         0));vertices.add(new Vector3(   1.2722,    29.197,         0));
        vertices.add(new Vector3(   5.2778,    25.398,         0));vertices.add(new Vector3(   3.3322,    24.099,         0));
        vertices.add(new Vector3(-0.028683,    24.099,         0));vertices.add(new Vector3(   7.1957,    20.299,         0));
        vertices.add(new Vector3( -0.02861,    20.299,         0));vertices.add(new Vector3(   5.2778,    19.065,         0));
        vertices.add(new Vector3(-0.028663,    18.984,         0));vertices.add(new Vector3(   9.2778,    15.265,         0));
        vertices.add(new Vector3(-0.028571,    15.185,         0));vertices.add(new Vector3(   9.2778,    15.265,         0));
        vertices.add(new Vector3(   7.3772,    13.999,         0));vertices.add(new Vector3(-0.028625,    13.901,         0));
        vertices.add(new Vector3(   9.2778,    15.265,         0));vertices.add(new Vector3(   12.278,    8.9323,         0));
        vertices.add(new Vector3(-0.028771,    8.9742,         0));vertices.add(new Vector3(   12.278,    8.9323,         0));
        vertices.add(new Vector3(   10.278,    7.6657,         0));vertices.add(new Vector3(-0.028592,    7.6552,         0));
        vertices.add(new Vector3(   15.278,    2.5994,         0));vertices.add(new Vector3(-0.028775,    2.6077,         0));
        vertices.add(new Vector3(   15.278,    2.5994,         0));vertices.add(new Vector3(   13.278,    1.3329,         0));
        vertices.add(new Vector3(-0.028727,    1.2617,         0));vertices.add(new Vector3(   18.278,   -3.7334,         0));
        vertices.add(new Vector3(   18.278,   -3.7334,         0));vertices.add(new Vector3(   2.2722,   -1.2003,         0));
        vertices.add(new Vector3(-0.028727,   -1.3098,         0));vertices.add(new Vector3(   4.2722,        -5,         0));
        vertices.add(new Vector3(   4.2722,        -5,         0));vertices.add(new Vector3(-0.028727,        -5,         0));
        vertices.add(new Vector3(  -3.3582,    30.464,         0));vertices.add(new Vector3(  -3.3582,    30.464,         0));
        vertices.add(new Vector3(  -1.3526,    29.197,         0));vertices.add(new Vector3(  -1.3526,    29.197,         0));
        vertices.add(new Vector3(  -1.3526,    29.197,         0));vertices.add(new Vector3(  -5.3582,    25.398,         0));
        vertices.add(new Vector3(  -1.3526,    29.197,         0));vertices.add(new Vector3(  -5.3582,    25.398,         0));
        vertices.add(new Vector3(  -3.4126,    24.099,         0));vertices.add(new Vector3(   -7.276,    20.299,         0));
        vertices.add(new Vector3(  -5.3582,    19.065,         0));vertices.add(new Vector3(  -9.3582,    15.265,         0));
        vertices.add(new Vector3(  -9.3582,    15.265,         0));vertices.add(new Vector3(  -7.4575,    13.999,         0));
        vertices.add(new Vector3(  -9.3582,    15.265,         0));vertices.add(new Vector3(  -12.358,    8.9323,         0));
        vertices.add(new Vector3(  -12.358,    8.9323,         0));vertices.add(new Vector3(  -10.358,    7.6657,         0));
        vertices.add(new Vector3(  -15.358,    2.5994,         0));vertices.add(new Vector3(  -15.358,    2.5994,         0));
        vertices.add(new Vector3(  -13.358,    1.3329,         0));vertices.add(new Vector3(  -18.358,   -3.7334,         0));
        vertices.add(new Vector3(  -18.358,   -3.7334,         0));vertices.add(new Vector3(  -2.3526,   -1.2003,         0));
        vertices.add(new Vector3(  -4.3526,        -5,         0));vertices.add(new Vector3(  -4.3526,        -5,         0));
        vertices.add(new Vector3(        0,     34.31,  0.040181));vertices.add(new Vector3(        0,    30.464,   -3.2778));
        vertices.add(new Vector3(        0,    30.464,  0.040181));vertices.add(new Vector3(        0,    30.464,  0.028749));
        vertices.add(new Vector3(        0,    30.464,   -3.2778));vertices.add(new Vector3(        0,    29.197,   -1.2722));
        vertices.add(new Vector3(        0,    29.197,   -1.2722));vertices.add(new Vector3(        0,    29.197,  0.028703));
        vertices.add(new Vector3(        0,    29.197,   -1.2722));vertices.add(new Vector3(        0,    25.398,   -5.2778));
        vertices.add(new Vector3(        0,    25.398,   0.02865));vertices.add(new Vector3(        0,    29.197,   -1.2722));
        vertices.add(new Vector3(        0,    25.398,   -5.2778));vertices.add(new Vector3(        0,    24.099,   -3.3322));
        vertices.add(new Vector3(        0,    24.099,  0.028683));vertices.add(new Vector3(        0,    20.299,   -7.1957));
        vertices.add(new Vector3(        0,    20.299,   0.02861));vertices.add(new Vector3(        0,    19.065,   -5.2778));
        vertices.add(new Vector3(        0,    18.984,  0.028663));vertices.add(new Vector3(        0,    15.265,   -9.2778));
        vertices.add(new Vector3(        0,    15.185,  0.028571));vertices.add(new Vector3(        0,    15.265,   -9.2778));
        vertices.add(new Vector3(        0,    13.999,   -7.3772));vertices.add(new Vector3(        0,    13.901,  0.028625));
        vertices.add(new Vector3(        0,    15.265,   -9.2778));vertices.add(new Vector3(        0,    8.9323,   -12.278));
        vertices.add(new Vector3(        0,    8.9742,  0.028771));vertices.add(new Vector3(        0,    8.9323,   -12.278));
        vertices.add(new Vector3(        0,    7.6657,   -10.278));vertices.add(new Vector3(        0,    7.6552,  0.028592));
        vertices.add(new Vector3(        0,    2.5994,   -15.278));vertices.add(new Vector3(        0,    2.6077,  0.028775));
        vertices.add(new Vector3(        0,    2.5994,   -15.278));vertices.add(new Vector3(        0,    1.3329,   -13.278));
        vertices.add(new Vector3(        0,    1.2617,  0.028727));vertices.add(new Vector3(        0,   -3.7334,   -18.278));
        vertices.add(new Vector3(        0,   -3.7334,   -18.278));vertices.add(new Vector3(        0,   -1.2003,   -2.2722));
        vertices.add(new Vector3(        0,   -1.3098,  0.028727));vertices.add(new Vector3(        0,        -5,   -4.2722));
        vertices.add(new Vector3(        0,        -5,   -4.2722));vertices.add(new Vector3(        0,        -5,  0.028727));
        vertices.add(new Vector3(        0,    30.464,    3.3582));vertices.add(new Vector3(        0,    30.464,    3.3582));
        vertices.add(new Vector3(        0,    29.197,    1.3526));vertices.add(new Vector3(        0,    29.197,    1.3526));
        vertices.add(new Vector3(        0,    29.197,    1.3526));vertices.add(new Vector3(        0,    25.398,    5.3582));
        vertices.add(new Vector3(        0,    29.197,    1.3526));vertices.add(new Vector3(        0,    25.398,    5.3582));
        vertices.add(new Vector3(        0,    24.099,    3.4126));vertices.add(new Vector3(        0,    20.299,     7.276));
        vertices.add(new Vector3(        0,    19.065,    5.3582));vertices.add(new Vector3(        0,    15.265,    9.3582));
        vertices.add(new Vector3(        0,    15.265,    9.3582));vertices.add(new Vector3(        0,    13.999,    7.4575));
        vertices.add(new Vector3(        0,    15.265,    9.3582));vertices.add(new Vector3(        0,    8.9323,    12.358));
        vertices.add(new Vector3(        0,    8.9323,    12.358));vertices.add(new Vector3(        0,    7.6657,    10.358));
        vertices.add(new Vector3(        0,    2.5994,    15.358));vertices.add(new Vector3(        0,    2.5994,    15.358));
        vertices.add(new Vector3(        0,    1.3329,    13.358));vertices.add(new Vector3(        0,   -3.7334,    18.358));
        vertices.add(new Vector3(        0,   -3.7334,    18.358));vertices.add(new Vector3(        0,   -1.2003,    2.3526));
        vertices.add(new Vector3(        0,        -5,    4.3526));vertices.add(new Vector3(        0,        -5,    4.3526));

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

    public void setShowWireframe(boolean showWireframe) {
        this.showWireframe = showWireframe;
    }

    public void setShowFaces(boolean showFaces) {
        this.showFaces = showFaces;
    }

    public void setShowBoundingBoxes(boolean showBoundingBoxes) {
        this.showBoundingBoxes = showBoundingBoxes;
    }

    public PinholeRasterCamera getCamera() {
        return camera;
    }
}