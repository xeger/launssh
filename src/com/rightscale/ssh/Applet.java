package com.rightscale.ssh;

import java.awt.Color;

public class Applet extends net.xeger.ssh.Applet {
	private static final long serialVersionUID = 8571832801694930444L;

    public void init() {
      super.init();
      getContentPane().setBackground(Color.WHITE);
    }
}
