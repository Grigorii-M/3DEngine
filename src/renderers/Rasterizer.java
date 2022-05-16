package renderers;

import cameras.PinholeRasterCamera;
import maths.*;
import util.Models3D;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rasterizer {

    private final PinholeRasterCamera camera;

    private Matrix woldToCamera;
    private ArrayList<Triangle> currentObject;


    private boolean showWireframe = false;
    private boolean showFaces = true;
    private boolean showBoundingBoxes = false;
    
    private Graphics g;

    private int yaw;
    private int pitch;
    private int roll;
    
    public Rasterizer(PinholeRasterCamera camera) {
        this.camera = camera;
    }

    public void setGraphics(Graphics g) {
        this.g = g;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    public void render() {
        woldToCamera = camera.getCameraMatrix().getInverse();
        Matrix xzRotation = Matrix.getXZRightHandedRotationMatrix(yaw);
        Matrix yzRotation = Matrix.getYZRightHandedRotationMatrixAlt(pitch);
        Matrix xyRotation = Matrix.getXYRightHandedRotationMatrix(roll);
        Matrix rotationMatrix = xzRotation.multiply(yzRotation).multiply(xyRotation);

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(0, 0, (int) camera.getImageWidth(), (int) camera.getImageHeight());
        graphics2D.setColor(Color.WHITE);

        if (currentObject == null || currentObject.isEmpty()) {
            currentObject = Models3D.getXMasTreeTriangles();
        }

        BufferedImage image = new BufferedImage((int) camera.getImageWidth(), (int) camera.getImageHeight(), BufferedImage.TYPE_INT_ARGB);
        double[][] depthBuffer = new double[image.getHeight()][image.getWidth()];
        for (double[] doubles : depthBuffer) {
            Arrays.fill(doubles, 1); // Z value of raster coordinates is normalized between 0 and 1, where 1 is the distance to far clipping plane
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
            ArrayList<Vector3> triangleVerticesRaster = new ArrayList<>(java.util.List.of(v0Raster, v1Raster, v2Raster));

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
            if (boundingBoxXMin > camera.getImageWidth() - 1 || boundingBoxXMax < 0 || boundingBoxYMin > camera.getImageHeight() - 1 || boundingBoxYMax < 0) {
                continue;
            }

            // Clamp bounding box inside the canvas
            int xMin = (int) Math.max(0, Math.min((int) Math.floor(boundingBoxXMin), camera.getImageWidth() - 1));
            int xMax = (int) Math.max(0, Math.min((int) Math.floor(boundingBoxXMax), camera.getImageWidth() - 1));
            int yMin = (int) Math.max(0, Math.min((int) Math.floor(boundingBoxYMin), camera.getImageHeight() - 1));
            int yMax = (int) Math.max(0, Math.min((int) Math.floor(boundingBoxYMax), camera.getImageHeight() - 1));

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

                        if (isPixelOverlapping(triangleVerticesRaster, java.util.List.of(w0, w1, w2))) {
                            // Interpolate z perspective correctly
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
    private boolean isPixelOverlapping(java.util.List<Vector3> points, List<Double> barycentricCoordinates) {
        Vector3 edge0 = points.get(2).minus(points.get(1));
        Vector3 edge1 = points.get(0).minus(points.get(1));
        Vector3 edge2 = points.get(1).minus(points.get(0));

        return ((barycentricCoordinates.get(0) == 0) ? ((edge0.y == 0 && edge0.x > 0) || edge0.y > 0) : barycentricCoordinates.get(0) > 0)
                && ((barycentricCoordinates.get(1) == 0) ? ((edge1.y == 0 && edge1.x > 0) || edge1.y > 0) : barycentricCoordinates.get(1) > 0)
                && ((barycentricCoordinates.get(2) == 0) ? ((edge2.y == 0 && edge2.x > 0) || edge2.y > 0) : barycentricCoordinates.get(2) > 0);
    }

    private Vector3 convertToRaster(Vector3 vertex) {

        Vector4 vertexInCameraSpace = vertex.getAsVector4().multiplyByMatrix(woldToCamera);

        Matrix projectionMatrix = camera.getProjectionType() == PinholeRasterCamera.ProjectionType.PERSPECTIVE
                ? computePerspectiveProjectionMatrix() : computeOrthographicProjectionMatrix();

        Vector3 vertexNormalized = vertexInCameraSpace.multiplyByMatrix(projectionMatrix).getAsVector3();

        return new Vector3(
                (vertexNormalized.x + 1) / 2 * camera.getImageWidth(),
                (1 - vertexNormalized.y) / 2 * camera.getImageHeight(),
                vertexNormalized.z
        );
    }

    private Matrix computePerspectiveProjectionMatrix() {
        double n = camera.getNearClippingPlane();
        double f = camera.getFarClippingPlane();
        double r = camera.getCanvasRight();
        double l = camera.getCanvasLeft();
        double t = camera.getCanvasTop();
        double b = camera.getCanvasBottom();

        return new Matrix(new double[] {
                2 * n / (r - l)  , 0                , 0                    , 0 ,
                0                , 2 * n / (t - b)  , 0                    , 0 ,
                (r + l) / (r - l), (t + b) / (t - b), - (f + n) / (f - n)  , -1,
                0                , 0                , - 2 * f * n / (f - n), 0
        }, 4, 4);
    }

    private Matrix computeOrthographicProjectionMatrix() {
        double n = camera.getNearClippingPlane();
        double f = camera.getFarClippingPlane();
        double r = camera.getCanvasRight();
        double l = camera.getCanvasLeft();
        double t = camera.getCanvasTop();
        double b = camera.getCanvasBottom();

        return new Matrix(new double[] {
                2 / (r - l), 0          , 0            , - (r + l) / (r - l),
                0          , 2 / (t - b), 0            , - (t + b) / (t - b),
                0          , 0          , - 2 / (f - n), - (f + n) / (f - n),
                0          , 0          , 0            , 1
        }, 4, 4);
    }

    public void setObjectToPaint(ArrayList<Triangle> triangles) {
        currentObject = triangles;
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
}
