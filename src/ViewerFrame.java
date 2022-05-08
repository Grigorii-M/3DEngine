import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.geom.Path2D;
import java.nio.file.FileSystem;
import java.util.ArrayList;

public class ViewerFrame extends JFrame {
    public ViewerFrame() {
        setSize(512, 512);
        setResizable(false);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openFileMenuItem = new JMenuItem();
        openFileMenuItem.setText("Open");
        openFileMenuItem.setIcon(UIManager.getIcon("FileView.fileIcon"));

        fileMenu.add(openFileMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        GridBagConstraints c = new GridBagConstraints();

        JSlider yawSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 180);
        yawSlider.setToolTipText("Rotation around Oy");

        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 1;
        c.gridwidth = 3;
        c.weightx = 0;
        c.weighty = 0;
        add(yawSlider, c);

        JSlider pitchSlider = new JSlider(JSlider.VERTICAL, 0, 360, 180);
        pitchSlider.setToolTipText("Rotation around Ox");

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        add(pitchSlider, c);

        JSlider rollSlider = new JSlider(JSlider.VERTICAL, 0, 360, 180);
        rollSlider.setToolTipText("Rotation around Oz");

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        add(rollSlider, c);

        RenderPanel renderPanel = new RenderPanel(yawSlider, pitchSlider, rollSlider);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(renderPanel, c);

        pack();

        openFileMenuItem.addActionListener(action -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select active directory");
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                ArrayList<Triangle> tris = new OBJParser().parse(fileChooser.getSelectedFile());
                renderPanel.setObjectToPaint(tris);
            }

        });

        yawSlider.addChangeListener(listener -> {
            System.out.println("Oy: " + yawSlider.getValue());
            renderPanel.repaint();
        });
        pitchSlider.addChangeListener(listener -> {
            System.out.println("Ox: " + pitchSlider.getValue());
            renderPanel.repaint();
        });
        rollSlider.addChangeListener(listener -> {
            System.out.println("Oz: " + rollSlider.getValue());
            renderPanel.repaint();
        });


        setVisible(true);
    }
}
