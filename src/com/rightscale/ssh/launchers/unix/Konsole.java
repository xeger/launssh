package com.rightscale.ssh.launchers.unix;

import com.rightscale.ssh.*;

public class Konsole extends SimpleUnixLauncher {
    public Konsole(Launchpad launchpad) {
        super(launchpad, "konsole -h", "konsole -e");
    }
}
