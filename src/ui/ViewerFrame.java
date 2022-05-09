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
        openFileMenuItem.setIcon(UIManager.getIcon("FileView.fileIcon"));
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
        JCheckBoxMenuItem showWireframe = new JCheckBoxMenuItem("Wireframe");
        showWireframe.addActionListener(l -> {
            renderPanel.setShowWireframe(showWireframe.getState());
            renderPanel.repaint();
        });
        JCheckBoxMenuItem showFaces = new JCheckBoxMenuItem("Faces");
        showFaces.setState(true);
        showFaces.addActionListener(l -> {
            renderPanel.setShowFaces(showFaces.getState());
            renderPanel.repaint();
        });
        JCheckBoxMenuItem showBoundingBoxes = new JCheckBoxMenuItem("Bounding boxes");
        showBoundingBoxes.addActionListener(l -> {
            renderPanel.setShowBoundingBoxes(showBoundingBoxes.getState());
            renderPanel.repaint();
        });
        viewMenu.add(showWireframe);
        viewMenu.add(showFaces);
        viewMenu.add(showBoundingBoxes);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);

        GridBagConstraints c = new GridBagConstraints();

        JSlider yawSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 180);
        yawSlider.setToolTipText("Rotation around Oy");
        yawSlider.addChangeListener(l -> {
            //System.out.println("Oy: " + yawSlider.getValue());
            renderPanel.repaint();
        });

        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 1;
        c.gridwidth = 3;
        c.weightx = 0;
        c.weighty = 0;
        add(yawSlider, c);

        JSlider pitchSlider = new JSlider(JSlider.VERTICAL, 0, 360, 180);
        pitchSlider.setToolTipText("Rotation around Ox");
        pitchSlider.addChangeListener(l -> {
            //System.out.println("Ox: " + pitchSlider.getValue());
            renderPanel.repaint();
        });

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        add(pitchSlider, c);

        JSlider rollSlider = new JSlider(JSlider.VERTICAL, 0, 360, 180);
        rollSlider.setToolTipText("Rotation around Oz");
        rollSlider.addChangeListener(l -> {
            //System.out.println("Oz: " + rollSlider.getValue());
            renderPanel.repaint();
        });

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        add(rollSlider, c);

        renderPanel = new RenderPanel(yawSlider, pitchSlider, rollSlider);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(renderPanel, c);

        pack();

        setVisible(true);
    }
}
