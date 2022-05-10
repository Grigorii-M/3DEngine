package ui;

import cameras.PinholeRasterCamera;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class RasterCameraSettingsFrame extends JFrame {

    private PinholeRasterCamera camera;

    public RasterCameraSettingsFrame(PinholeRasterCamera camera, Component parentComponent) {
        this.camera = camera;

        setLocationRelativeTo(parentComponent);
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
        projectionTypeComboBox.addActionListener(l -> camera.setProjectionType((PinholeRasterCamera.ProjectionType) projectionTypeComboBox.getSelectedItem()));

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
        cameraSetupModeJComboBox.setEditable(false);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        lensPanel.add(cameraSetupModeJComboBox, c);

        JPanel lensUnitsPanel = new JPanel(new CardLayout());

        cameraSetupModeJComboBox.addItemListener(e -> {
            CardLayout cl = (CardLayout)(lensUnitsPanel.getLayout());
            cl.show(lensUnitsPanel, Objects.requireNonNull(cameraSetupModeJComboBox.getSelectedItem()).toString());
        });

        JPanel focalLengthPanel = new JPanel(new GridBagLayout());
        JSlider focalLengthSlider = new JSlider(JSlider.HORIZONTAL, 1, 1000, (int) camera.getFocalLength());
        JTextField focalLengthTextField = new JFormattedTextField(focalLengthSlider.getValue());
        focalLengthTextField.setColumns(3);
        focalLengthSlider.addChangeListener(l -> {
            int value = focalLengthSlider.getValue();
            focalLengthTextField.setText(String.valueOf(value));
            camera.setFocalLength(value);
        });
        focalLengthTextField.addActionListener(l -> {
            int value = Integer.parseInt(focalLengthTextField.getText());
            focalLengthSlider.setValue(value);
            camera.setFocalLength(value);
        });

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;

        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 1;
        focalLengthPanel.add(focalLengthSlider, g);
        g.gridx = 1;
        g.gridy = 1;
        g.weightx = 0.15;
        focalLengthPanel.add(focalLengthTextField, g);


        JPanel fieldOfViewPanel = new JPanel(new GridBagLayout());
        JSlider fieldOfViewSlider = new JSlider(JSlider.HORIZONTAL, 1, 173, (int) camera.getFieldOfView());
        JTextField fieldOfViewTextField = new JFormattedTextField(fieldOfViewSlider.getValue());
        fieldOfViewTextField.setColumns(3);
        fieldOfViewSlider.addChangeListener(l -> {
            int value = fieldOfViewSlider.getValue();
            fieldOfViewTextField.setText(String.valueOf(value));
            camera.setFieldOfView(value);
        });
        fieldOfViewTextField.addActionListener(l -> {
            int value = Integer.parseInt(fieldOfViewTextField.getText());
            fieldOfViewSlider.setValue(value);
            camera.setFieldOfView(value);
        });

        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 1;
        fieldOfViewPanel.add(fieldOfViewSlider, g);
        g.gridx = 1;
        g.gridy = 1;
        g.weightx = 0.15;
        fieldOfViewPanel.add(fieldOfViewTextField, g);

        lensUnitsPanel.add(focalLengthPanel, PinholeRasterCamera.CameraSetupMode.FOCAL_LENGTH.toString());
        lensUnitsPanel.add(fieldOfViewPanel, PinholeRasterCamera.CameraSetupMode.FIELD_OF_VIEW.toString());

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
        JTextField filmApertureWidthTextField = new JTextField(String.valueOf(camera.getFilmApertureWidth()));
        JLabel filmApertureHeightLabel = new JLabel("Height");
        JTextField filmApertureHeightTextField = new JTextField(String.valueOf(camera.getFilmApertureHeight()));
        JLabel filmApertureAspectRatioLabel = new JLabel("Film aspect ratio");
        JComboBox<PinholeRasterCamera.AspectRatio> filmApertureAspectRatioComboBox = new JComboBox<>(PinholeRasterCamera.AspectRatio.values());
        filmApertureAspectRatioComboBox.setSelectedItem(PinholeRasterCamera.AspectRatio.FREE);
        synchronizeAspectRatio(filmApertureWidthTextField, filmApertureHeightTextField, filmApertureAspectRatioComboBox);

        // Todo: center these things
        c.insets = new Insets(3, 3, 3, 3);
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        resolutionPanel.add(resolutionLabel, c);
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
        JTextField imageWidthTextField = new JTextField(String.valueOf(camera.getImageWidth()));
        JLabel imageHeightLabel = new JLabel("Height");
        JTextField imageHeightTextField = new JTextField(String.valueOf(camera.getImageHeight()));
        JLabel imageAspectRatioLabel = new JLabel("Image aspect ratio");
        JComboBox<PinholeRasterCamera.AspectRatio> imageAspectRatioComboBox = new JComboBox<>(PinholeRasterCamera.AspectRatio.values());
        imageAspectRatioComboBox.setSelectedItem(PinholeRasterCamera.AspectRatio.FREE);
        synchronizeAspectRatio(imageWidthTextField, imageHeightTextField, imageAspectRatioComboBox);

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
        resolutionGateComboBox.addActionListener(l -> camera.setFitResolutionGate((PinholeRasterCamera.FitResolutionGate) resolutionGateComboBox.getSelectedItem()));

        c.gridx = 0;
        c.gridy = 11;
        resolutionPanel.add(resolutionGateLabel, c);
        c.gridy = 12;
        resolutionPanel.add(resolutionGateComboBox, c);

        resolutionPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new BevelBorder(BevelBorder.RAISED)));

        return resolutionPanel;
    }

    private void synchronizeAspectRatio(JTextField widthTextField, JTextField heightTextField, JComboBox<PinholeRasterCamera.AspectRatio> aspectRatioJComboBox) {
        widthTextField.addActionListener(l -> {
            double width = Double.parseDouble(widthTextField.getText());
            double height = Double.parseDouble(heightTextField.getText());
            PinholeRasterCamera.AspectRatio aspectRatio = (PinholeRasterCamera.AspectRatio) aspectRatioJComboBox.getSelectedItem();

            assert aspectRatio != null;
            heightTextField.setText(String.valueOf(getHeightFromWidthAspectRatio(width, height, aspectRatio)));
        });
        heightTextField.addActionListener(l -> {
            double width = Double.parseDouble(widthTextField.getText());
            double height = Double.parseDouble(heightTextField.getText());
            PinholeRasterCamera.AspectRatio aspectRatio = (PinholeRasterCamera.AspectRatio) aspectRatioJComboBox.getSelectedItem();

            assert aspectRatio != null;
            widthTextField.setText(String.valueOf(getWidthFromHeightAspectRatio(width, height, aspectRatio)));
        });
    }

    private double getHeightFromWidthAspectRatio(double width, double height, PinholeRasterCamera.AspectRatio aspectRatio) {
        switch (aspectRatio) {
            case RATIO_4X3 -> width *= 3.0 / 4;
            case RATIO_5X3 -> width *= 3.0 / 5;
            case RATIO_5X4 -> width *= 4.0 / 5;
            case RATIO_16X9 -> width *= 9.0 / 16;
            case RATIO_1X1 -> {}
            case FREE, default -> width = height;
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
            case FREE, default -> height = width;
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

        JTextField m00TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(0, 0)));
        JTextField m01TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(0, 1)));
        JTextField m02TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(0, 2)));
        JTextField m03TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(0, 3)));

        JTextField m10TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(1, 0)));
        JTextField m11TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(1, 1)));
        JTextField m12TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(1, 2)));
        JTextField m13TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(1, 3)));

        JTextField m20TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(2, 0)));
        JTextField m21TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(2, 1)));
        JTextField m22TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(2, 2)));
        JTextField m23TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(2, 3)));

        JTextField m30TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(3, 0)));
        JTextField m31TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(3, 1)));
        JTextField m32TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(3, 2)));
        JTextField m33TextField = new JTextField(String.valueOf(camera.getCameraMatrix().get(3, 3)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 4;

        transformPanel.add(transformLabel, c);
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

        transformPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new BevelBorder(BevelBorder.RAISED)));


        return transformPanel;
    }
}
