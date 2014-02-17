package com.rightscale.ssh.ui;

import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class CenteringPanel extends JPanel {
    public CenteringPanel(JComponent child) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        GridBagConstraints c = new GridBagConstraints();
        child.setMaximumSize(child.getPreferredSize());
        child.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalGlue());
        add(child, c);
        add(Box.createVerticalGlue());
    }
}
