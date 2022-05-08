import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.geom.Path2D;
import java.nio.file.FileSystem;
import java.util.ArrayList;

public class ViewerFrame extends JFrame {
    public ViewerFrame() {
        setSize(400, 400);
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

        JSlider yawSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, 180);
        add(yawSlider, BorderLayout.SOUTH);

        JSlider pitchSlider = new JSlider(JSlider.VERTICAL, 0, 360, 180);
        add(pitchSlider, BorderLayout.EAST);

        JSlider rollSlider = new JSlider(JSlider.VERTICAL, 0, 360, 180);
        add(rollSlider, BorderLayout.WEST);

        RenderPanel renderPanel = new RenderPanel(yawSlider, pitchSlider, rollSlider);

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
