package com.rightscale.ssh.launchers.unix;

import com.rightscale.ssh.*;

public class GnomeTerminal extends SimpleUnixLauncher {
    public GnomeTerminal(Launchpad l) {
        super(l, "gnome-terminal --help", "gnome-terminal -x");
    }
}
