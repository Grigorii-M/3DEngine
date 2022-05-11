package cameras;

import maths.Matrix;
import util.UnitsConverter;

/**
 * Model of a pinhole camera for rasterization algorithm
 */
public class PinholeRasterCamera {
    private double focalLength = 20; // in mm
    private double fieldOfView; // horizontal

    public enum CameraSetupMode {
        FOCAL_LENGTH, FIELD_OF_VIEW
    }

    private double filmApertureWidth = 0.825;
    private double filmApertureHeight = 0.446;

    private double nearClippingPlane = 1;
    private double farClippingPlane = 1000;

    // Image resolution in pixels
    // Todo: set render panel dependent on these things
    private int imageWidth = 512;
    private int imageHeight = 512;

    private FitResolutionGate fitResolutionGate = FitResolutionGate.OVERSCAN;
    public enum FitResolutionGate {
        FILL, OVERSCAN
    }

    private ProjectionType projectionType = ProjectionType.PERSPECTIVE;
    public enum ProjectionType {
        PERSPECTIVE, ORTHOGRAPHIC
    }

    private Matrix cameraToWorld = new Matrix(new double[] {
        0.871214, 0, -0.490904, 0,
                -0.192902, 0.919559, -0.342346, 0,
                0.451415, 0.392953, 0.801132, 0,
                14.777467, 29.361945, 27.993464, 1}, 4, 4);

    private double xScale = 1;
    private double yScale = 1;

    // Perspective projection matrix algorithm is based on implicitly setting up the image plane at the near clipping plane
    private double distanceToCanvas = nearClippingPlane;

    private double filmAspectRatio;
    private double deviceAspectRatio;

    public enum AspectRatio {
        RATIO_4X3,
        RATIO_5X3,
        RATIO_5X4,
        RATIO_1X1,
        RATIO_16X9,
        FREE
    }

    private double canvasTop;
    private double canvasBottom;
    private double canvasLeft;
    private double canvasRight;

    public PinholeRasterCamera() {
        calculateCanvas();
    }

    private void calculateCanvas() {
        canvasTop = ((filmApertureHeight * UnitsConverter.inchToMm / 2) / focalLength) * nearClippingPlane;
        canvasBottom = -canvasTop;

        canvasRight = ((filmApertureWidth * UnitsConverter.inchToMm / 2) / focalLength) * nearClippingPlane;
        canvasLeft = -canvasRight;

        filmAspectRatio = filmApertureWidth / filmApertureHeight;
        deviceAspectRatio = imageWidth / (double) imageHeight;

        fieldOfView = 2 * Math.toDegrees(Math.atan((filmApertureWidth * UnitsConverter.inchToMm / 2) / focalLength));

        // Todo: fix this motherfucker
        switch (fitResolutionGate) {
            case FILL -> {
                if (filmAspectRatio > deviceAspectRatio) {
                    xScale = deviceAspectRatio / filmAspectRatio;
                } else {
                    yScale = filmAspectRatio / deviceAspectRatio;
                }
            }
            case OVERSCAN -> {
                if (filmAspectRatio > deviceAspectRatio) {
                    yScale = filmAspectRatio / deviceAspectRatio;
                } else {
                    xScale = deviceAspectRatio / filmAspectRatio;
                }
            }
        }

        canvasRight *= xScale;
        canvasTop *= yScale;

        canvasLeft = -canvasRight;
        canvasBottom = -canvasTop;
    }

    public void setProjectionType(ProjectionType projectionType) {
        this.projectionType = projectionType;
    }

    public void setNearClippingPlane(double nearClippingPlane) {
        this.nearClippingPlane = nearClippingPlane;
    }

    public double getNearClippingPlane() {
        return nearClippingPlane;
    }

    public void setFarClippingPlane(double farClippingPlane) {
        this.farClippingPlane = farClippingPlane;
    }

    public double getFarClippingPlane() {
        return farClippingPlane;
    }

    public double getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(double focalLength) {
        this.focalLength = focalLength;
        this.fieldOfView = 2 * Math.toDegrees(Math.atan((filmApertureWidth * UnitsConverter.inchToMm / 2) / this.focalLength));
        calculateCanvas();
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(double fieldOfView) {
        this.fieldOfView = fieldOfView;
        this.focalLength = filmApertureWidth * UnitsConverter.inchToMm / (2 * Math.tan(Math.toRadians(this.fieldOfView / 2)));
        calculateCanvas();
    }

    public double getFilmApertureWidth() {
        return filmApertureWidth;
    }

    public void setFilmApertureWidth(double filmApertureWidth) {
        this.filmApertureWidth = filmApertureWidth;
        calculateCanvas();
    }

    public double getFilmApertureHeight() {
        return filmApertureHeight;
    }

    public void setFilmApertureHeight(double filmApertureHeight) {
        this.filmApertureHeight = filmApertureHeight;
        calculateCanvas();
    }

    public double getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
        calculateCanvas();
    }

    public double getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
        calculateCanvas();
    }

    public void setFitResolutionGate(FitResolutionGate value) {
        this.fitResolutionGate = value;
        calculateCanvas();
    }

    public double getCanvasTop() {
        return canvasTop;
    }

    public double getCanvasBottom() {
        return canvasBottom;
    }

    public double getCanvasRight() {
        return canvasRight;
    }

    public double getCanvasLeft() {
        return canvasLeft;
    }

    public Matrix getCameraMatrix() {
        return cameraToWorld;
    }

    public void setCameraMatrix(Matrix matrix) {
        if (!(matrix.getColumns() == matrix.getRows() && matrix.getRows() == 4)) {
            throw new IllegalArgumentException("Camera matrix should be 4x4");
        }

        this.cameraToWorld = matrix;
        calculateCanvas();
    }
}
