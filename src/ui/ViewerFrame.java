package ui;

import maths.Triangle;
import parsers.OBJParser;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ViewerFrame extends JFrame {
    private RenderPanel renderPanel;
    public ViewerFrame() {
        setSize(512, 512);
        setResizable(false);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openFileMenuItem = new JMenuItem("Open");
        openFileMenuItem.addActionListener(action -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select active directory");
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                ArrayList<Triangle> tris = new OBJParser().parse(fileChooser.getSelectedFile());
                renderPanel.setObjectToPaint(tris);
            }

        });
        fileMenu.add(openFileMenuItem);

        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem showWireframeMenuItem = new JCheckBoxMenuItem("Wireframe");
        showWireframeMenuItem.addActionListener(l -> {
            renderPanel.setShowWireframe(showWireframeMenuItem.getState());
            renderPanel.repaint();
        });
        JCheckBoxMenuItem showFacesMenuItem = new JCheckBoxMenuItem("Faces");
        showFacesMenuItem.setState(true);
        showFacesMenuItem.addActionListener(l -> {
            renderPanel.setShowFaces(showFacesMenuItem.getState());
            renderPanel.repaint();
        });
        JCheckBoxMenuItem showBoundingBoxesMenuItem = new JCheckBoxMenuItem("Bounding boxes");
        showBoundingBoxesMenuItem.addActionListener(l -> {
            renderPanel.setShowBoundingBoxes(showBoundingBoxesMenuItem.getState());
            renderPanel.repaint();
        });
        viewMenu.add(showWireframeMenuItem);
        viewMenu.add(showFacesMenuItem);
        viewMenu.add(showBoundingBoxesMenuItem);

        JMenu cameraMenu = new JMenu("Camera");

        JMenuItem editRasterCameraMenuItem = new JMenuItem("Raster camera");
        editRasterCameraMenuItem.addActionListener(l -> SwingUtilities.invokeLater(() -> new RasterCameraSettingsFrame(renderPanel.getCamera(), renderPanel)));
        cameraMenu.add(editRasterCameraMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(cameraMenu);
        setJMenuBar(menuBar);

        GridBagConstraints c = new GridBagConstraints();

        JSlider yawSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 180);
        yawSlider.setToolTipText("Rotation around Oy");
        yawSlider.addChangeListener(l -> renderPanel.repaint());

        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 1;
        c.gridwidth = 3;
        c.weightx = 0;
        c.weighty = 0;
        add(yawSlider, c);

        JSlider pitchSlider = new JSlider(JSlider.VERTICAL, 0, 360, 180);
        pitchSlider.setToolTipText("Rotation around Ox");
        pitchSlider.addChangeListener(l -> renderPanel.repaint());

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        add(pitchSlider, c);

        JSlider rollSlider = new JSlider(JSlider.VERTICAL, 0, 360, 180);
        rollSlider.setToolTipText("Rotation around Oz");
        rollSlider.addChangeListener(l -> renderPanel.repaint());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        add(rollSlider, c);

        renderPanel = new RenderPanel(yawSlider, pitchSlider, rollSlider, this);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(renderPanel, c);

        pack();

        setVisible(true);
    }
}
