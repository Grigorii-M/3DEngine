import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
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
        Matrix xzRotation = Matrix.getXZRotationMatrix(yawSlider.getValue());
        Matrix xzRotationAlt = Matrix.getXZRotationMatrixAlt(yawSlider.getValue());

        Matrix yzRotation = Matrix.getYZRotationMatrix(pitchSlider.getValue());
        Matrix yzRotationAlt = Matrix.getYZRotationMatrixAlt(pitchSlider.getValue());

        Matrix xyRotation = Matrix.getXYRotationMatrix(rollSlider.getValue());

        //Matrix rotationMatrix = xzRotation.multiply(yzRotation).multiply(xyRotation);
        Matrix rotationMatrixAlt = xzRotationAlt.multiply(yzRotationAlt).multiply(xyRotation);

        Graphics2D graphics2D = (Graphics2D) g;
//        graphics2D.setColor(Color.BLACK);
//        graphics2D.fillRect(0, 0, getWidth(), getHeight());
//        graphics2D.setColor(Color.WHITE);
//
//        // Put the origin to the center of the drawing area
//        graphics2D.translate(getWidth() / 2, getHeight() / 2);
//
//
//        if (currentObject == null || currentObject.isEmpty()) {
//            currentObject = getTetrahedronTriangles();
//        }
//
//        for (Triangle triangle : currentObject) {
//            Path2D edgePath = new Path2D.Double();
//            Matrix triangleMatrix = new Matrix( new double[] {
//                triangle.v1.x(), triangle.v1.y(), triangle.v1.z(),
//                triangle.v2.x(), triangle.v2.y(), triangle.v2.z(),
//                triangle.v3.x(), triangle.v3.y(), triangle.v3.z()
//            }, 3, 3);
//
//            // Matrix transformedTriangleMatrix = triangleMatrix.multiply(rotationMatrix);
//            Matrix transformedTriangleMatrix = triangleMatrix.multiply(rotationMatrixAlt);
//
//            Vector3D triangleNormal = triangle.getNormal().normalize();
//
//            double collinearity = triangleNormal.dotProduct(Vector3D.one(triangleNormal));
//
//            //graphics2D.setColor(Color.getHSBColor( 1, 1, (float) collinearity));
//            graphics2D.setColor(triangle.color);
//
//            edgePath.moveTo(transformedTriangleMatrix.get(0, 0), transformedTriangleMatrix.get(0, 1));
//            edgePath.lineTo(transformedTriangleMatrix.get(1, 0), transformedTriangleMatrix.get(1, 1));
//            edgePath.lineTo(transformedTriangleMatrix.get(2, 0), transformedTriangleMatrix.get(2, 1));
//            edgePath.closePath();
//            graphics2D.draw(edgePath);
//        }

        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, Color.BLACK.getRGB());
            }
        }
        graphics2D.drawImage(image, 0, 0, null);
    }

    public void setObjectToPaint(ArrayList<Triangle> triangles) {
        currentObject = triangles;
        System.out.println(currentObject == null || currentObject.isEmpty());
        repaint();
    }

    private ArrayList<Triangle> getTetrahedronTriangles() {
        ArrayList<Triangle> tris = new ArrayList<>();
        tris.add(new Triangle(
        new Vertex(100, 200, 100),
        new Vertex(-100, -100, 100),
        new Vertex(-100, 100, -100),
        Color.WHITE));
        tris.add(new Triangle(
        new Vertex(100, 100, 100),
        new Vertex(-100, -100, 100),
        new Vertex(100, -100, -100),
        Color.RED));
        tris.add(new Triangle(
        new Vertex(-100, 100, -100),
        new Vertex(100, -100, -100),
        new Vertex(100, 100, 100),
        Color.GREEN));
        tris.add(new Triangle(
        new Vertex(-100, 100, -100),
        new Vertex(100, -100, -100),
        new Vertex(-100, -100, 100),
        Color.BLUE));

        return tris;
    }
}