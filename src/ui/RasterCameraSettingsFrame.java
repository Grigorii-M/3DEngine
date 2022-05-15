package ui;

import cameras.PinholeRasterCamera;
import maths.Matrix;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Objects;
public class RasterCameraSettingsFrame extends JFrame {

    private final PinholeRasterCamera camera;
    private final JPanel renderPanel;

    DecimalFormat decimalFormat = new DecimalFormat("####.##");

    public RasterCameraSettingsFrame(PinholeRasterCamera camera, JPanel renderPanel) {
        this.camera = camera;
        this.renderPanel = renderPanel;

        setLocationRelativeTo(renderPanel);
        setSize(270, 700);
        setResizable(false);
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));


        add(getLensPanel());
        add(getResolutionPanel());
        add(getTransformPanel());

        setVisible(true);
    }

    private JPanel getLensPanel() {
        JPanel lensPanel = new JPanel();
        lensPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel lensLabel = new JLabel("Lens");
        JLabel projectionTypeLabel = new JLabel("Projection");
        JComboBox<PinholeRasterCamera.ProjectionType> projectionTypeComboBox = new JComboBox<>(PinholeRasterCamera.ProjectionType.values());
        projectionTypeComboBox.addActionListener(l -> {
            camera.setProjectionType((PinholeRasterCamera.ProjectionType) projectionTypeComboBox.getSelectedItem());
            renderPanel.repaint();
        });

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(3, 3, 3, 3);
        lensPanel.add(lensLabel, c);
        c.gridwidth = 1;
        c.gridy = 1;
        lensPanel.add(projectionTypeLabel, c);
        c.gridx = 1;
        c.gridy = 1;
        lensPanel.add(projectionTypeComboBox, c);

        JComboBox<PinholeRasterCamera.CameraSetupMode> cameraSetupModeJComboBox = new JComboBox<>(PinholeRasterCamera.CameraSetupMode.values());
        cameraSetupModeJComboBox.setSelectedItem(PinholeRasterCamera.CameraSetupMode.FIELD_OF_VIEW);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        lensPanel.add(cameraSetupModeJComboBox, c);

        JPanel lensUnitsPanel = new JPanel(new CardLayout());

        JPanel focalLengthPanel = new JPanel(new GridBagLayout());
        JSlider focalLengthSlider = new JSlider(JSlider.HORIZONTAL, 1, 1000, (int) camera.getFocalLength());
        JTextField focalLengthTextField = new JTextField(decimalFormat.format(focalLengthSlider.getValue()));
        focalLengthTextField.setColumns(4);
        focalLengthSlider.addChangeListener(l -> {
            int value = focalLengthSlider.getValue();
            focalLengthTextField.setText(decimalFormat.format(value));
            camera.setFocalLength(value);
            renderPanel.repaint();
        });
        focalLengthTextField.addActionListener(l -> {
            double value = Integer.parseInt(focalLengthTextField.getText());
            value = Math.max(focalLengthSlider.getMinimum(), Math.min(value, focalLengthSlider.getMaximum()));
            focalLengthTextField.setText(decimalFormat.format(value));
            focalLengthSlider.setValue((int) value);
            camera.setFocalLength(value);
            renderPanel.repaint();
        });

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;

        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 1;
        focalLengthPanel.add(focalLengthSlider, g);
        g.gridx = 1;
        g.gridy = 1;
        g.weightx = 0.2;
        focalLengthPanel.add(focalLengthTextField, g);

        JPanel fieldOfViewPanel = new JPanel(new GridBagLayout());
        JSlider fieldOfViewSlider = new JSlider(JSlider.HORIZONTAL, 1, 173, (int) camera.getFieldOfView());
        JTextField fieldOfViewTextField = new JTextField(decimalFormat.format(fieldOfViewSlider.getValue()));
        fieldOfViewTextField.setColumns(4);
        fieldOfViewSlider.addChangeListener(l -> {
            int value = fieldOfViewSlider.getValue();
            fieldOfViewTextField.setText(decimalFormat.format(value));
            camera.setFieldOfView(value);
            renderPanel.repaint();
        });
        fieldOfViewTextField.addActionListener(l -> {
            double value = Integer.parseInt(fieldOfViewTextField.getText());
            value = Math.max(fieldOfViewSlider.getMinimum(), Math.min(value, fieldOfViewSlider.getMaximum()));
            fieldOfViewTextField.setText(decimalFormat.format(value));
            fieldOfViewSlider.setValue((int) value);
            camera.setFieldOfView(value);
            renderPanel.repaint();
        });

        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 1;
        fieldOfViewPanel.add(fieldOfViewSlider, g);
        g.gridx = 1;
        g.gridy = 1;
        g.weightx = 0.2;
        fieldOfViewPanel.add(fieldOfViewTextField, g);

        lensUnitsPanel.add(fieldOfViewPanel, PinholeRasterCamera.CameraSetupMode.FIELD_OF_VIEW.toString());
        lensUnitsPanel.add(focalLengthPanel, PinholeRasterCamera.CameraSetupMode.FOCAL_LENGTH.toString());

        cameraSetupModeJComboBox.addItemListener(e -> {
            CardLayout cl = (CardLayout)(lensUnitsPanel.getLayout());
            focalLengthSlider.setValue((int) camera.getFocalLength());
            focalLengthTextField.setText(decimalFormat.format(
                    Math.max(focalLengthSlider.getMinimum(), Math.min(camera.getFocalLength(), focalLengthSlider.getMaximum()))));
            fieldOfViewSlider.setValue((int) camera.getFieldOfView());
            fieldOfViewTextField.setText(decimalFormat.format(
                    Math.max(fieldOfViewSlider.getMinimum(), Math.min(camera.getFieldOfView(), fieldOfViewSlider.getMaximum()))));
            cl.show(lensUnitsPanel, Objects.requireNonNull(cameraSetupModeJComboBox.getSelectedItem()).toString());
            renderPanel.repaint();
        });

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 4;
        lensPanel.add(lensUnitsPanel, c);

        lensPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new BevelBorder(BevelBorder.RAISED)));

        return lensPanel;
    }

    private JPanel getResolutionPanel() {
        JPanel resolutionPanel = new JPanel();
        resolutionPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Film aperture, image dimensions, film resolution, image resolution, resolution gate
        JLabel resolutionLabel = new JLabel("Resolution");
        JLabel filmApertureLabel = new JLabel("Film aperture size");
        JLabel filmApertureWidthLabel = new JLabel("Width");
        JTextField filmApertureWidthTextField = new JTextField(decimalFormat.format(camera.getFilmApertureWidth()));
        JLabel filmApertureHeightLabel = new JLabel("Height");
        JTextField filmApertureHeightTextField = new JTextField(decimalFormat.format(camera.getFilmApertureHeight()));
        JLabel filmApertureAspectRatioLabel = new JLabel("Film aspect ratio");
        JComboBox<PinholeRasterCamera.AspectRatio> filmApertureAspectRatioComboBox = new JComboBox<>(PinholeRasterCamera.AspectRatio.values());
        filmApertureAspectRatioComboBox.setSelectedItem(PinholeRasterCamera.AspectRatio.FREE);

        filmApertureWidthTextField.addActionListener(l -> {
            synchronizeAspectRatioWidth(filmApertureWidthTextField, filmApertureHeightTextField, filmApertureAspectRatioComboBox);
            camera.setFilmApertureWidth(Double.parseDouble(filmApertureWidthTextField.getText()));
            camera.setFilmApertureHeight(Double.parseDouble(filmApertureHeightTextField.getText()));
            renderPanel.repaint();
        });
        filmApertureHeightTextField.addActionListener(l -> {
            synchronizeAspectRatioHeight(filmApertureHeightTextField, filmApertureWidthTextField, filmApertureAspectRatioComboBox);
            camera.setFilmApertureWidth(Double.parseDouble(filmApertureWidthTextField.getText()));
            camera.setFilmApertureHeight(Double.parseDouble(filmApertureHeightTextField.getText()));
            renderPanel.repaint();
        });

        c.insets = new Insets(3, 3, 3, 3);
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        resolutionPanel.add(resolutionLabel, c);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridy = 1;
        resolutionPanel.add(filmApertureLabel, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        resolutionPanel.add(filmApertureWidthLabel, c);
        c.gridx = 1;
        resolutionPanel.add(filmApertureWidthTextField, c);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        resolutionPanel.add(filmApertureHeightLabel, c);
        c.gridx = 1;
        resolutionPanel.add(filmApertureHeightTextField, c);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        resolutionPanel.add(filmApertureAspectRatioLabel, c);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        resolutionPanel.add(filmApertureAspectRatioComboBox, c);

        JLabel imageLabel = new JLabel("Image size");
        JLabel imageWidthLabel = new JLabel("Width");
        JTextField imageWidthTextField = new JTextField(decimalFormat.format(camera.getImageWidth()));
        JLabel imageHeightLabel = new JLabel("Height");
        JTextField imageHeightTextField = new JTextField(decimalFormat.format(camera.getImageHeight()));
        JLabel imageAspectRatioLabel = new JLabel("Image aspect ratio");
        JComboBox<PinholeRasterCamera.AspectRatio> imageAspectRatioComboBox = new JComboBox<>(PinholeRasterCamera.AspectRatio.values());
        imageAspectRatioComboBox.setSelectedItem(PinholeRasterCamera.AspectRatio.FREE);

        imageWidthTextField.addActionListener(l -> {
            synchronizeAspectRatioWidth(imageWidthTextField, imageHeightTextField, imageAspectRatioComboBox);
            camera.setImageWidth((int) Double.parseDouble(imageWidthTextField.getText()));
            camera.setImageHeight((int) Double.parseDouble(imageHeightTextField.getText()));
            renderPanel.repaint();
        });
        imageHeightTextField.addActionListener(l -> {
            synchronizeAspectRatioHeight(imageHeightTextField, imageWidthTextField, imageAspectRatioComboBox);
            camera.setImageWidth((int) Double.parseDouble(imageWidthTextField.getText()));
            camera.setImageHeight((int) Double.parseDouble(imageHeightTextField.getText()));
            renderPanel.repaint();
        });

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        resolutionPanel.add(imageLabel, c);
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        resolutionPanel.add(imageWidthLabel, c);
        c.gridx = 1;
        resolutionPanel.add(imageWidthTextField, c);
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 1;
        resolutionPanel.add(imageHeightLabel, c);
        c.gridx = 1;
        resolutionPanel.add(imageHeightTextField, c);
        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 2;
        resolutionPanel.add(imageAspectRatioLabel, c);
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 2;
        resolutionPanel.add(imageAspectRatioComboBox, c);

        JLabel resolutionGateLabel = new JLabel("Resolution gate");
        JComboBox<PinholeRasterCamera.FitResolutionGate> resolutionGateComboBox = new JComboBox<>(PinholeRasterCamera.FitResolutionGate.values());
        resolutionGateComboBox.setSelectedItem(PinholeRasterCamera.FitResolutionGate.OVERSCAN);
        resolutionGateComboBox.addActionListener(l -> {
            camera.setFitResolutionGate((PinholeRasterCamera.FitResolutionGate) resolutionGateComboBox.getSelectedItem());
            renderPanel.repaint();
        });

        c.gridx = 0;
        c.gridy = 11;
        resolutionPanel.add(resolutionGateLabel, c);
        c.gridy = 12;
        resolutionPanel.add(resolutionGateComboBox, c);

        resolutionPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new BevelBorder(BevelBorder.RAISED)));

        return resolutionPanel;
    }

    private void synchronizeAspectRatioWidth(JTextField textFieldFrom, JTextField textFieldTo, JComboBox<PinholeRasterCamera.AspectRatio> aspectRatioJComboBox) {
        double from = Double.parseDouble(textFieldFrom.getText());
        double to = Double.parseDouble(textFieldTo.getText());
        PinholeRasterCamera.AspectRatio aspectRatio = (PinholeRasterCamera.AspectRatio) aspectRatioJComboBox.getSelectedItem();

        assert aspectRatio != null;
        textFieldTo.setText(decimalFormat.format(getHeightFromWidthAspectRatio(from, to, aspectRatio)));
    }

    private void synchronizeAspectRatioHeight(JTextField textFieldFrom, JTextField textFieldTo, JComboBox<PinholeRasterCamera.AspectRatio> aspectRatioJComboBox) {
        double from = Double.parseDouble(textFieldFrom.getText());
        double to = Double.parseDouble(textFieldTo.getText());
        PinholeRasterCamera.AspectRatio aspectRatio = (PinholeRasterCamera.AspectRatio) aspectRatioJComboBox.getSelectedItem();

        assert aspectRatio != null;
        textFieldTo.setText(decimalFormat.format(getWidthFromHeightAspectRatio(to, from, aspectRatio)));
    }

    private double getHeightFromWidthAspectRatio(double width, double height, PinholeRasterCamera.AspectRatio aspectRatio) {
        switch (aspectRatio) {
            case RATIO_4X3 -> width *= 3.0 / 4;
            case RATIO_5X3 -> width *= 3.0 / 5;
            case RATIO_5X4 -> width *= 4.0 / 5;
            case RATIO_16X9 -> width *= 9.0 / 16;
            case RATIO_1X1 -> {}
            case FREE -> width = height;
        }

        return width;
    }

    private double getWidthFromHeightAspectRatio(double width, double height, PinholeRasterCamera.AspectRatio aspectRatio) {
        switch (aspectRatio) {
            case RATIO_4X3 -> height *= 4.0 / 3;
            case RATIO_5X3 -> height *= 5.0 / 3;
            case RATIO_5X4 -> height *= 5.0 / 4;
            case RATIO_16X9 -> height *= 16.0 / 9;
            case RATIO_1X1 -> {}
            case FREE -> height = width;
        }

        return height;
    }

    private JPanel getTransformPanel() {
        JPanel transformPanel = new JPanel();
        transformPanel.setLayout(new GridBagLayout());

        JLabel transformLabel = new  JLabel("Transform");
        JLabel xAxisLabel = new JLabel("X axis");
        JLabel yAxisLabel = new JLabel("Y axis");
        JLabel zAxisLabel = new JLabel("Z axis");
        JLabel translationLabel = new JLabel("Translation");

        JTextField m00TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(0, 0)));
        JTextField m01TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(0, 1)));
        JTextField m02TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(0, 2)));
        JTextField m03TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(0, 3)));

        JTextField m10TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(1, 0)));
        JTextField m11TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(1, 1)));
        JTextField m12TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(1, 2)));
        JTextField m13TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(1, 3)));

        JTextField m20TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(2, 0)));
        JTextField m21TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(2, 1)));
        JTextField m22TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(2, 2)));
        JTextField m23TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(2, 3)));

        JTextField m30TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(3, 0)));
        JTextField m31TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(3, 1)));
        JTextField m32TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(3, 2)));
        JTextField m33TextField = new JTextField(decimalFormat.format(camera.getCameraMatrix().get(3, 3)));

        JButton applyTransformButton = new JButton("Apply transform");
        applyTransformButton.addActionListener(l -> {
            double m00 = Double.parseDouble(m00TextField.getText());
            double m01 = Double.parseDouble(m01TextField.getText());
            double m02 = Double.parseDouble(m02TextField.getText());
            double m03 = Double.parseDouble(m03TextField.getText());

            double m10 = Double.parseDouble(m10TextField.getText());
            double m11 = Double.parseDouble(m11TextField.getText());
            double m12 = Double.parseDouble(m12TextField.getText());
            double m13 = Double.parseDouble(m13TextField.getText());

            double m20 = Double.parseDouble(m20TextField.getText());
            double m21 = Double.parseDouble(m21TextField.getText());
            double m22 = Double.parseDouble(m22TextField.getText());
            double m23 = Double.parseDouble(m23TextField.getText());

            double m30 = Double.parseDouble(m30TextField.getText());
            double m31 = Double.parseDouble(m31TextField.getText());
            double m32 = Double.parseDouble(m32TextField.getText());
            double m33 = Double.parseDouble(m33TextField.getText());

            camera.setCameraMatrix(new Matrix(new double[] {
                    m00, m01, m02, m03,
                    m10, m11, m12, m13,
                    m20, m21, m22, m23,
                    m30, m31, m32, m33,
            }, 4, 4));
            renderPanel.repaint();
        });

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        transformPanel.add(transformLabel, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        transformPanel.add(xAxisLabel, c);
        c.gridx = 1;
        transformPanel.add(m00TextField, c);
        c.gridx = 2;
        transformPanel.add(m01TextField, c);
        c.gridx = 3;
        transformPanel.add(m02TextField, c);
        c.gridx = 4;
        transformPanel.add(m03TextField, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        transformPanel.add(yAxisLabel, c);
        c.gridx = 1;
        transformPanel.add(m10TextField, c);
        c.gridx = 2;
        transformPanel.add(m11TextField, c);
        c.gridx = 3;
        transformPanel.add(m12TextField, c);
        c.gridx = 4;
        transformPanel.add(m13TextField, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        transformPanel.add(zAxisLabel, c);
        c.gridx = 1;
        transformPanel.add(m20TextField, c);
        c.gridx = 2;
        transformPanel.add(m21TextField, c);
        c.gridx = 3;
        transformPanel.add(m22TextField, c);
        c.gridx = 4;
        transformPanel.add(m23TextField, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        transformPanel.add(translationLabel, c);
        c.gridx = 1;
        transformPanel.add(m30TextField, c);
        c.gridx = 2;
        transformPanel.add(m31TextField, c);
        c.gridx = 3;
        transformPanel.add(m32TextField, c);
        c.gridx = 4;
        transformPanel.add(m33TextField, c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 4;
        transformPanel.add(applyTransformButton, c);

        transformPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new BevelBorder(BevelBorder.RAISED)));

        return transformPanel;
    }
}
