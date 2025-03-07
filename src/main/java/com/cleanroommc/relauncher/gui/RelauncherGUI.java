package com.cleanroommc.relauncher.gui;

import com.cleanroommc.relauncher.CleanroomRelauncher;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import net.minecraftforge.fml.cleanroomrelauncher.ExitVMBypass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class RelauncherGUI extends JDialog {

    private static final int width = 300, height = 400;

    public static RelauncherGUI show(List<CleanroomRelease> eligibleReleases) {
        RelauncherGUI ui = new RelauncherGUI(eligibleReleases);
        return ui;
    }

    public CleanroomRelease selected;
    public String javaPath;

    private RelauncherGUI(List<CleanroomRelease> eligibleReleases) {
        super((Frame) null, "Which release do you want to relaunch with?", true);

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
        int x = (rect.width - width) / 2;
        int y = (rect.height - height) / 2;
        this.setLocation(x, y);

        JPanel javaPathPanel = new JPanel();
        JTextField textField = new JTextField(20);
        if (CleanroomRelauncher.CONFIG != null && CleanroomRelauncher.CONFIG.getJavaExecutablePath() != null) {
            textField.setText(CleanroomRelauncher.CONFIG.getJavaExecutablePath());
        }
        JLabel label = new JLabel("Enter Java Path:");
        javaPathPanel.add(label);
        javaPathPanel.add(textField);

        JButton confirmButton = new JButton("Confirm");
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(e -> {
            javaPath = textField.getText();
            this.dispose();
        });

        JPanel selectionPanel = new JPanel();
        ButtonGroup releaseGroup = new ButtonGroup();
        CleanroomRelease defaulted = null;
        if (CleanroomRelauncher.CONFIG != null && CleanroomRelauncher.CONFIG.getCleanroomVersion() != null) {
            defaulted = eligibleReleases.stream().filter(cr -> cr.name.equals(CleanroomRelauncher.CONFIG.getCleanroomVersion())).findFirst().get();
        }
        for (CleanroomRelease release : eligibleReleases) {
            JRadioButton radioButton = new JRadioButton(release.name);
            if (defaulted == release) {
                radioButton.setEnabled(true);
            }
            radioButton.setActionCommand(release.name);
            radioButton.addActionListener(e -> {
                selected = release;
                confirmButton.setEnabled(true);
            });
            releaseGroup.add(radioButton);
            selectionPanel.add(radioButton);
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(selectionPanel);
        mainPanel.add(javaPathPanel);

        this.add(mainPanel);
        this.add(confirmButton, BorderLayout.SOUTH);
        this.pack();
        this.setSize(width, height);
        this.setVisible(true);
        this.setAutoRequestFocus(true);
    }
}
