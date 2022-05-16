package ui;

import cameras.PinholeRasterCamera;
import maths.*;
import renderers.Rasterizer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class RenderPanel extends JPanel {
    private final PinholeRasterCamera camera;
    private final JSlider yawSlider;
    private final JSlider pitchSlider;
    private final JSlider rollSlider;

    private final Rasterizer renderer;

    @Override
    public void paintComponent(Graphics g) {
        setPreferredSize(new Dimension((int) camera.getImageWidth(), (int) camera.getImageHeight()));
        parentFrame.setSize(getPreferredSize());
        renderer.setGraphics(g);
        renderer.setYaw(yawSlider.getValue());
        renderer.setPitch(pitchSlider.getValue());
        renderer.setRoll(rollSlider.getValue());
        renderer.render();
    }

    private final JFrame parentFrame;

    public RenderPanel(JSlider yawSlider, JSlider pitchSlider, JSlider rollSlider, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.yawSlider = yawSlider;
        this.pitchSlider = pitchSlider;
        this.rollSlider = rollSlider;
        setPreferredSize(new Dimension(512, 512));
        camera = new PinholeRasterCamera();
        renderer = new Rasterizer(camera);
    }


    public void setShowWireframe(boolean showWireframe) {
        renderer.setShowWireframe(showWireframe);
    }

    public void setShowFaces(boolean showFaces) {
        renderer.setShowFaces(showFaces);
    }

    public void setShowBoundingBoxes(boolean showBoundingBoxes) {
        renderer.setShowBoundingBoxes(showBoundingBoxes);
    }

    public PinholeRasterCamera getCamera() {
        return camera;
    }

    public void setObjectToPaint(ArrayList<Triangle> tris) {
        renderer.setObjectToPaint(tris);
    }
}