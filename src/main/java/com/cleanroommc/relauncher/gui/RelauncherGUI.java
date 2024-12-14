package com.cleanroommc.relauncher.gui;

import com.cleanroommc.relauncher.download.CleanroomRelease;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RelauncherGUI extends JDialog {

    public static CleanroomRelease show(List<CleanroomRelease> eligibleReleases) {
        RelauncherGUI ui = new RelauncherGUI(eligibleReleases);
        return ui.selected;
    }

    private CleanroomRelease selected;

    private RelauncherGUI(List<CleanroomRelease> eligibleReleases) {
        super((Frame) null, "Which release do you want to relaunch with?", true);

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        ButtonGroup releaseGroup = new ButtonGroup();

        JButton confirmButton = new JButton("Confirm");
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(e -> confirm());

        for (CleanroomRelease release : eligibleReleases) {
            JRadioButton radioButton = new JRadioButton(release.name);
            radioButton.setActionCommand(release.name);
            radioButton.addActionListener(e -> {
                this.selected = release;
                confirmButton.setEnabled(true);
            });
            releaseGroup.add(radioButton);
            panel.add(radioButton);
        }

        this.add(panel);
        this.add(confirmButton, BorderLayout.SOUTH);

        this.pack();

        this.setSize(300, 400);
        this.setVisible(true);
        this.setAutoRequestFocus(true);
    }

    private void confirm() {
        this.dispose();
    }

}
