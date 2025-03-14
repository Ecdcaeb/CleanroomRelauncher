package com.cleanroommc.relauncher.gui;

import com.cleanroommc.relauncher.CleanroomRelauncher;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import com.cleanroommc.relauncher.util.Platform;
import net.minecraftforge.fml.cleanroomrelauncher.ExitVMBypass;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

public class RelauncherGUI extends JDialog {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) { }
    }

    private static void scaleComponent(Component component, float scale) {
        // scaling rect
        if (component instanceof JTextField ||
                component instanceof JButton ||
                component instanceof JComboBox) {
            Dimension size = component.getPreferredSize();
            component.setPreferredSize(new Dimension((int) (size.width * scale), (int) (size.height * scale)));
            component.setMaximumSize(new Dimension((int) (size.width * scale), (int) (size.height * scale)));
        } else if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            Icon icon = label.getIcon();
            if (icon instanceof ImageIcon) {
                ImageIcon imageIcon = (ImageIcon) icon;
                Image image = imageIcon.getImage();
                if (image != null) {
                    Image scaledImage = image.getScaledInstance(
                            (int) (imageIcon.getIconWidth() * scale),
                            (int) (imageIcon.getIconHeight() * scale),
                            Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImage));
                }
            }
        }

        // scaling font
        if (component instanceof JLabel ||
                component instanceof JButton ||
                component instanceof JTextField ||
                component instanceof JComboBox) {
            Font font = component.getFont();
            if (font != null) {
                component.setFont(font.deriveFont(font.getSize() * scale));
            }
        }

        // scaling padding
        if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            Insets margin = button.getMargin();
            if (margin != null) {
                button.setMargin(new Insets(
                        (int) (margin.top * scale),
                        (int) (margin.left * scale),
                        (int) (margin.bottom * scale),
                        (int) (margin.right * scale)
                ));
            }
        } else if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            Insets margin = textField.getMargin();
            if (margin != null) {
                textField.setMargin(new Insets(
                        (int) (margin.top * scale),
                        (int) (margin.left * scale),
                        (int) (margin.bottom * scale),
                        (int) (margin.right * scale)
                ));
            }
        } else if (component instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) component;
            Insets margin = comboBox.getInsets();
            if (margin != null) {
                comboBox.setBorder(BorderFactory.createEmptyBorder(
                        (int) (margin.top * scale),
                        (int) (margin.left * scale),
                        (int) (margin.bottom * scale),
                        (int) (margin.right * scale)
                ));
            }
        } else if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            Insets margin = label.getInsets();
            if (margin != null) {
                label.setBorder(BorderFactory.createEmptyBorder(
                        (int) (margin.top * scale),
                        (int) (margin.left * scale),
                        (int) (margin.bottom * scale),
                        (int) (margin.right * scale)
                ));
            }
        } else if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            Border existingBorder = panel.getBorder();

            Insets margin = existingBorder instanceof EmptyBorder ?
                    ((EmptyBorder) existingBorder).getBorderInsets()
                    : new Insets(0, 0, 0, 0);

            panel.setBorder(BorderFactory.createEmptyBorder(
                    (int) (margin.top * scale),
                    (int) (margin.left * scale),
                    (int) (margin.bottom * scale),
                    (int) (margin.right * scale)
            ));
        }

        component.revalidate();
        component.repaint();

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                scaleComponent(child, scale);
            }
        }
    }

    public static RelauncherGUI show(List<CleanroomRelease> eligibleReleases) {
        ImageIcon imageIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(RelauncherGUI.class.getResource("/cleanroom-relauncher.png")));
        RelauncherGUI ui = new RelauncherGUI("Cleanroom Relaunch Configuration", imageIcon, eligibleReleases);
        return ui;
    }

    public CleanroomRelease selected;
    public String javaPath;

    private RelauncherGUI(String title, ImageIcon icon, List<CleanroomRelease> eligibleReleases) {
        super(new SupportingFrame(title, icon), title, true);

        this.setIconImage(icon.getImage());

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                RelauncherGUI.this.requestFocusInWindow();
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                selected = null;
                dispose();

                CleanroomRelauncher.LOGGER.info("No Cleanroom releases were selected, instance is dismissed.");
                ExitVMBypass.exit(0);
            }
        });
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice screen = env.getDefaultScreenDevice();
        Rectangle rect = screen.getDefaultConfiguration().getBounds();
        int width = rect.width / 3;
        int height = (int) (width / 1.5);
        int x = (rect.width - width) / 2;
        int y = (rect.height - height) / 2;
        this.setLocation(x, y);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel cleanroomLogo = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
        cleanroomLogo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel cleanroomPickerPanel = this.initializeCleanroomPicker(eligibleReleases);
        mainPanel.add(cleanroomPickerPanel);

        JPanel javaPickerPanel = this.initializeJavaPicker();
        mainPanel.add(javaPickerPanel);

        JButton relaunchButton = new JButton("Relaunch Cleanroom");
        relaunchButton.addActionListener(e -> {
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a Cleanroom version in order to relaunch.", "Cleanroom Release Not Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (javaPath == null) {
                JOptionPane.showMessageDialog(this, "Please provide a valid Java Executable in order to relaunch.", "Java Executable Not Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!this.testJava(null)) {
                JOptionPane.showMessageDialog(this, "Invalid Java Executable, please provide a valid java executable.", "Invalid Java Executable Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.dispose();
        });

        this.add(cleanroomLogo, BorderLayout.NORTH);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(relaunchButton, BorderLayout.SOUTH);
        float scale = rect.width / 1463f;
        scaleComponent(this, scale);

        this.pack();
        this.setSize(width, height);
        this.setVisible(true);
        this.setAutoRequestFocus(true);
    }

    private JPanel initializeCleanroomPicker(List<CleanroomRelease> eligibleReleases) {
        // Main Panel
        JPanel cleanroomPicker = new JPanel(new BorderLayout(5, 0));
        cleanroomPicker.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel select = new JPanel();
        select.setLayout(new BoxLayout(select, BoxLayout.Y_AXIS));
        cleanroomPicker.add(select);

        // Title label
        JLabel title = new JLabel("Select Cleanroom Version:");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        select.add(title);
        select.add(Box.createRigidArea(new Dimension(0, 5)));

        // Create dropdown panel
        JPanel dropdown = new JPanel(new BorderLayout(5, 5));
        dropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        select.add(dropdown);

        // Create the dropdown with release versions
        JComboBox<CleanroomRelease> releaseBox = new JComboBox<>();
        DefaultComboBoxModel<CleanroomRelease> releaseModel = new DefaultComboBoxModel<>();
        for (CleanroomRelease release : eligibleReleases) {
            releaseModel.addElement(release);
        }
        releaseBox.setModel(releaseModel);
        releaseBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CleanroomRelease) {
                    setText(((CleanroomRelease) value).name);
                }
                return this;
            }
        });
        if (CleanroomRelauncher.CONFIG != null && CleanroomRelauncher.CONFIG.getCleanroomVersion() != null) {
            Optional<CleanroomRelease> defaultRelease = eligibleReleases.stream().filter(release -> release.name.equals(CleanroomRelauncher.CONFIG.getCleanroomVersion())).findFirst();
            if (defaultRelease.isPresent()) {
                releaseBox.setSelectedItem(defaultRelease.get());
            } else {
                releaseBox.setSelectedItem(null);
                CleanroomRelauncher.CONFIG.setCleanroomVersion(null);
            }
        } else {
            releaseBox.setSelectedItem(null);
        }
        releaseBox.setMaximumRowCount(5);
        releaseBox.addActionListener(e -> selected = (CleanroomRelease) releaseBox.getSelectedItem());
        dropdown.add(releaseBox, BorderLayout.CENTER);

        return cleanroomPicker;
    }

    private JPanel initializeJavaPicker() {
        // Main Panel
        JPanel javaPicker = new JPanel(new BorderLayout(5, 0));
        javaPicker.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Select Panel
        JPanel select = new JPanel(new BorderLayout(5, 5));
        select.setLayout(new BoxLayout(select, BoxLayout.Y_AXIS));
        javaPicker.add(select);
        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        JLabel title = new JLabel("Select Java Executable:");
        textPanel.add(title, BorderLayout.NORTH);
        JTextField text = new JTextField(100);
        textPanel.add(text, BorderLayout.CENTER);
        // JButton browse = new JButton(UIManager.getIcon("FileView.directoryIcon"));
        JButton browse = new JButton("Browse");
        textPanel.add(browse, BorderLayout.EAST);
        select.add(textPanel);

        // Options Panel
        JPanel options = new JPanel(new BorderLayout(5, 5));
        options.setLayout(new BoxLayout(options, BoxLayout.X_AXIS));
        options.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        select.add(options);
        // JButton download = new JButton("Download");
        // JButton autoDetect = new JButton("Auto-Detect");
        JButton test = new JButton("Test");
        // options.add(autoDetect);
        options.add(test);

        text.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                javaPath = text.getText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                javaPath = text.getText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                javaPath = text.getText();
            }
        });

        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                text.setBorder(BorderFactory.createLineBorder(new Color(142, 177, 204)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                text.setBorder(null);
            }
        });

        browse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Find Java Executable");
            if (!text.getText().isEmpty()) {
                File currentFile = new File(text.getText());
                if (currentFile.getParentFile() != null && currentFile.getParentFile().exists()) {
                    fileChooser.setCurrentDirectory(currentFile.getParentFile());
                }
            }
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    if (file.isFile()) {
                        return !Platform.CURRENT.getOperatingSystem().isWindows() || file.getName().endsWith(".exe");
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return Platform.CURRENT.getOperatingSystem().isWindows() ? "Java Executable (*.exe)" : "Java Executable";
                }
            };
            fileChooser.setFileFilter(filter);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                text.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        test.addActionListener(e -> {
            String javaPath = text.getText();
            if (javaPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a Java executable first.", "No Java Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            File javaFile = new File(javaPath);
            if (!javaFile.exists()) {
                JOptionPane.showMessageDialog(this, "The selected Java executable does not exist.", "Invalid Java Executable Path", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JDialog testing = new JDialog(this, "Testing Java Executable", true);
            testing.setLocationRelativeTo(this);

            this.testJava(testing);
        });

        return javaPicker;
    }

    private boolean testJava(@Nullable JDialog testing) {
        try {
            ProcessBuilder pb = new ProcessBuilder(javaPath, "-version");
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (testing != null) {
                if (exitCode == 0) {
                    JOptionPane.showMessageDialog(this, "Java executable is working correctly!\n\nVersion Information:\n" + output, "Java Test Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Java executable test failed with exit code: " + exitCode, "Java Test Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
            return exitCode == 0;
        } catch (Exception e1) {
            CleanroomRelauncher.LOGGER.fatal("Failed to execute Java for testing", e1);
            if (testing != null) {
                JOptionPane.showMessageDialog(this, "Failed to execute Java (more information in console): " + e1.getMessage(), "Java Test Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

}
