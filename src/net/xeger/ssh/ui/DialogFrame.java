package net.xeger.ssh.ui;

import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class DialogFrame extends JFrame {
	public DialogFrame(String title) {
		super(title);
	}
	
	public DialogFrame() {
		super();
	}
	
	public void setContentPane(Container pane) {
		super.setContentPane(pane);
    	pack();
	}
	
	public void pack() {
		super.pack();
    	setMinimumSize(getPreferredSize());
    	setMaximumSize(getPreferredSize());
    	setResizable(false);
    	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int left = (d.width - getWidth()) / 2;
        int top = (d.height - getHeight()) / 2;
        setLocation(left, top);
	}
}
