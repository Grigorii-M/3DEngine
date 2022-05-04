import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class ViewerFrame extends JFrame {
    public ViewerFrame() {
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSlider yawSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 180);
        add(yawSlider, BorderLayout.SOUTH);

        JSlider pitchSlider = new JSlider(JSlider.VERTICAL, -90, 90, 0);
        add(pitchSlider, BorderLayout.EAST);

        JSlider rollSlider = new JSlider(JSlider.VERTICAL, -90, 90, 0);
        add(rollSlider, BorderLayout.WEST);

        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D graphics2D = (Graphics2D) g;
                graphics2D.setColor(Color.BLACK);
                graphics2D.fillRect(0, 0, getWidth(), getHeight());
                graphics2D.setColor(Color.WHITE);
                // Put the origin to the center of the drawing area
                graphics2D.translate(getWidth() / 2, getHeight() / 2);

                Matrix xzRotation = Matrix.getXZRotationMatrix(yawSlider.getValue());
                Matrix xzRotationAlt = Matrix.getXZRotationMatrixAlt(yawSlider.getValue());

                Matrix yzRotation = Matrix.getYZRotationMatrix(pitchSlider.getValue());
                Matrix yzRotationAlt = Matrix.getYZRotationMatrixAlt(pitchSlider.getValue());

                Matrix xyRotation = Matrix.getXYRotationMatrix(rollSlider.getValue());

                Matrix rotationMatrix = xzRotation.multiply(yzRotation).multiply(xyRotation);
//                Matrix rotationMatrixAlt = xzRotationAlt.multiply(yzRotationAlt).multiply(xyRotation);

                ArrayList<Triangle> tris = getTetrahedronTriangles();
                for (Triangle triangle : tris) {
                    Path2D edgePath = new Path2D.Double();
                    Matrix triangleMatrix = new Matrix( new double[] {
                            triangle.v1().x(), triangle.v1().y(), triangle.v1().z(),
                            triangle.v2().x(), triangle.v2().y(), triangle.v2().z(),
                            triangle.v3().x(), triangle.v3().y(), triangle.v3().z()
                    }, 3, 3);
                    
                    Matrix transformedTriangle = triangleMatrix.multiply(rotationMatrix);
//                    Matrix transformedTriangleAlt = triangleMatrix.multiply(rotationMatrixAlt);
//
//                    System.out.println("Normal:");
//                    System.out.println(transformedTriangle);
//                    System.out.println("Alt:");
//                    System.out.println(transformedTriangleAlt);
                    
                    edgePath.moveTo(transformedTriangle.get(0, 0), transformedTriangle.get(0, 1));
                    edgePath.lineTo(transformedTriangle.get(1, 0), transformedTriangle.get(1, 1));
                    edgePath.lineTo(transformedTriangle.get(2, 0), transformedTriangle.get(2, 1));
                    edgePath.closePath();
                    graphics2D.draw(edgePath);
                }
            }

            private ArrayList<Triangle> getTetrahedronTriangles() {
                ArrayList<Triangle> tris = new ArrayList<>();
                tris.add(new Triangle(new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(-100, 100, -100),
                        Color.WHITE));
                tris.add(new Triangle(new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(100, -100, -100),
                        Color.RED));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(100, 100, 100),
                        Color.GREEN));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(-100, -100, 100),
                        Color.BLUE));
                return tris;
            }
        };

        yawSlider.addChangeListener(listener -> {
            //System.out.println("yaw: " + yawSlider.getValue());
            renderPanel.repaint();
        });
        pitchSlider.addChangeListener(listener -> {
            //System.out.println("pitch: " + pitchSlider.getValue());
            renderPanel.repaint();
        });
        rollSlider.addChangeListener(listener -> {
            //System.out.println("roll: " + rollSlider.getValue());
            renderPanel.repaint();
        });

        add(renderPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}
