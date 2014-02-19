package net.xeger.ssh.launchers.unix;

import net.xeger.ssh.*;

public class Xterm extends SimpleUnixLauncher {
    public Xterm(Launchpad launchpad) {
        super(launchpad, "xterm -h", "xterm -e");

        if( isPlatform("Mac") ) {
            throw new RuntimeException("Wrong OS.");
        }
    }
}
