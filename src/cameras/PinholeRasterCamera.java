package cameras;

import maths.Matrix;
import util.UnitsConverter;

/**
 * Model of a pinhole camera for rasterization algorithm
 */
public class PinholeRasterCamera {
    private final double focalLength = 20; // in mm

    private double filmApertureWidth = 0.825;
    private double filmApertureHeight = 0.446;

    private double fieldOfView;

    private double nearClippingPlane = 1;
    private double farClippingPlane = 1000;

    // Image resolution in pixels
    private int imageWidth = 512;
    private int imageHeight = 512;

    private FitResolutionGate fitResolutionGate = FitResolutionGate.OVERSCAN;
    public enum FitResolutionGate {
        FILL, OVERSCAN
    }
    private Matrix cameraToWorld;

    private double xScale = 1;
    private double yScale = 1;

    // Perspective projection matrix works is based on implicitly setting up the image plane at the near clipping plane
    private double distanceToCanvas = nearClippingPlane;

    private double filmAspectRatio;
    private double deviceAspectRatio;

    private double canvasTop;
    private double canvasBottom;
    private double canvasLeft;
    private double canvasRight;

    public PinholeRasterCamera() {
        canvasTop = ((filmApertureHeight * UnitsConverter.inchToMm / 2) / focalLength) * nearClippingPlane;
        canvasBottom = -canvasTop;

        canvasRight = ((filmApertureWidth * UnitsConverter.inchToMm / 2) / focalLength) * nearClippingPlane;
        canvasLeft = -canvasRight;

        filmAspectRatio = filmApertureWidth / filmApertureHeight;
        deviceAspectRatio = imageWidth / (double) imageHeight;


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

    public double getNearClippingPlane() {
        return nearClippingPlane;
    }

    public double getFarClippingPlane() {
        return farClippingPlane;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
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
}
