package com.cleanroommc.relauncher.gui;

import com.cleanroommc.relauncher.CleanroomRelauncher;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import com.cleanroommc.relauncher.util.Platform;
import net.minecraftforge.fml.cleanroomrelauncher.ExitVMBypass;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

public class RelauncherGUI extends JDialog {

    private static final int WIDTH = 500, HEIGHT = 500;

    public static RelauncherGUI show(List<CleanroomRelease> eligibleReleases) {
        RelauncherGUI ui = new RelauncherGUI(eligibleReleases);
        return ui;
    }

    public CleanroomRelease selected;
    public String javaPath;

    private RelauncherGUI(List<CleanroomRelease> eligibleReleases) {
        super((Frame) null, "Cleanroom Relaunch Configuration", true);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                selected = null;
                dispose();

                ExitVMBypass.exit(0);
            }
        });
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice screen = env.getDefaultScreenDevice();
        Rectangle rect = screen.getDefaultConfiguration().getBounds();
        int x = (rect.width - WIDTH) / 2;
        int y = (rect.height - HEIGHT) / 2;
        this.setLocation(x, y);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel cleanroomPickerPanel = this.initializeCleanroomPicker(eligibleReleases);
        mainPanel.add(cleanroomPickerPanel);

        JPanel javaPickerPanel = this.initializeJavaPicker();
        mainPanel.add(javaPickerPanel);

        JButton relaunchButton = new JButton("Relaunch Cleanroom");
        relaunchButton.addActionListener(e -> {
            if (selected == null) {
                JOptionPane.showMessageDialog(mainPanel, "Please select a Cleanroom version in order to relaunch.", "Cleanroom Release Not Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (javaPath == null) {
                JOptionPane.showMessageDialog(mainPanel, "Please provide a valid Java Executable in order to relaunch.", "Java Executable Not Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!this.testJava(null)) {
                JOptionPane.showMessageDialog(mainPanel, "Invalid Java Executable, please provide a valid java executable.", "Invalid Java Executable Selected", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.dispose();
        });

        this.add(mainPanel, BorderLayout.NORTH);
        this.add(relaunchButton, BorderLayout.SOUTH);
        this.pack();
        this.setSize(WIDTH, HEIGHT);
        this.setVisible(true);
        this.setAutoRequestFocus(true);
    }

    private JPanel initializeCleanroomPicker(List<CleanroomRelease> eligibleReleases) {
        // Main Panel
        JPanel cleanroomPicker = new JPanel(new BorderLayout(5, 0));
        cleanroomPicker.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

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
        dropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
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
        textPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel title = new JLabel("Select Java Executable:");
        textPanel.add(title, BorderLayout.NORTH);
        JTextField text = new JTextField(70);
        textPanel.add(text, BorderLayout.CENTER);
        JButton browse = new JButton("Browse");
        textPanel.add(browse, BorderLayout.EAST);
        select.add(textPanel);

        // Options Panel
        JPanel options = new JPanel(new BorderLayout(5, 5));
        options.setLayout(new BoxLayout(options, BoxLayout.X_AXIS));
        options.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        select.add(options);
        // JButton download = new JButton("Download");
        JButton autoDetect = new JButton("Auto-Detect");
        JButton test = new JButton("Test");
        options.add(autoDetect);
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
                    JOptionPane.showMessageDialog(testing, "Java executable is working correctly!\n\nVersion Information:\n" + output, "Java Test Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(testing, "Java executable test failed with exit code: " + exitCode, "Java Test Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
            return exitCode == 0;
        } catch (Exception e1) {
            CleanroomRelauncher.LOGGER.fatal("Failed to execute Java for testing", e1);
            if (testing != null) {
                JOptionPane.showMessageDialog(testing, "Failed to execute Java (more information in console): " + e1.getMessage(), "Java Test Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

}
