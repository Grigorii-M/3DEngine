import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class RenderPanel extends JPanel {
    private ArrayList<Triangle> currentObject;

    private final JSlider yawSlider;
    private final JSlider pitchSlider;
    private final JSlider rollSlider;

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
        Matrix cameraToWorld = new Matrix(new double[] {
                0.871214, 0, -0.490904, 0,
                -0.192902, 0.919559, -0.342346, 0,
                0.451415, 0.392953, 0.801132, 0,
                14.777467, 29.361945, 27.993464, 1}, 4, 4);

//                Matrix cameraToWorld = new Matrix(new double[] {
//                1, 0, 0, 0,
//                0, 1, 0, 0,
//                0, 0, 1, 0,
//                0, 20, 40, 1}, 4, 4);
        Matrix woldToCamera = cameraToWorld.getInverse();


        if (currentObject == null || currentObject.isEmpty()) {
            currentObject = getXMasTreeTriangles();
        }

        for (Triangle triangle : currentObject) {
            Path2D edgePath = new Path2D.Double();

            Vector3D v1_3d = new Vector3D(triangle.v1.x, triangle.v1.y, triangle.v1.z);
            v1_3d = v1_3d.applyTransformation(rotationMatrix);
            Vector3D v2_3d = new Vector3D(triangle.v2.x, triangle.v2.y, triangle.v2.z);
            v2_3d = v2_3d.applyTransformation(rotationMatrix);
            Vector3D v3_3d = new Vector3D(triangle.v3.x, triangle.v3.y, triangle.v3.z);
            v3_3d = v3_3d.applyTransformation(rotationMatrix);

            Vector2D v1_2d = computePixelCoordinates(v1_3d, woldToCamera);
            Vector2D v2_2d = computePixelCoordinates(v2_3d, woldToCamera);
            Vector2D v3_2d = computePixelCoordinates(v3_3d, woldToCamera);

            edgePath.moveTo(v1_2d.x(), v1_2d.y());
            edgePath.lineTo(v2_2d.x(), v2_2d.y());
            edgePath.lineTo(v3_2d.x(), v3_2d.y());
            edgePath.closePath();

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

    private Vector2D computePixelCoordinates(Vector3D point, Matrix worldToCamera) {
        Vector3D pointInCameraSpace = point.applyTransformation(worldToCamera);
        Vector2D pointInScreenSpace = new Vector2D(
                pointInCameraSpace.x() / -pointInCameraSpace.z(),
                pointInCameraSpace.y() / -pointInCameraSpace.z()
        );
        double canvasWidth = 2;
        double canvasHeight = 2;
        Vector2D pointNormalized = new Vector2D(
                (pointInScreenSpace.x() + canvasWidth / 2) / canvasWidth,
                (pointInScreenSpace.y() + canvasHeight / 2) / canvasHeight
        );
        return new Vector2D(
                Math.floor(pointNormalized.x() * getWidth()),
                Math.floor((1 - pointNormalized.y()) * getHeight())
        );
    }

    public void setObjectToPaint(ArrayList<Triangle> triangles) {
        currentObject = triangles;
        System.out.println(currentObject == null || currentObject.isEmpty());
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
            triangles.add(new Triangle(vertices.get(indices[i]), vertices.get(indices[i + 1]), vertices.get(indices[i + 2]), Color.WHITE));
        }

        return triangles;
    }
}