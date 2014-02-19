package net.xeger.ssh.launchers.unix;

import net.xeger.ssh.*;

public class GnomeTerminal extends SimpleUnixLauncher {
    public GnomeTerminal(Launchpad l) {
        super(l, "gnome-terminal --help", "gnome-terminal -x");
    }
}
