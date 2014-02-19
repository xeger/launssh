package net.xeger.ssh.launchers.unix;

import net.xeger.ssh.*;

public class Konsole extends SimpleUnixLauncher {
    public Konsole(Launchpad launchpad) {
        super(launchpad, "konsole -h", "konsole -e");
    }
}
