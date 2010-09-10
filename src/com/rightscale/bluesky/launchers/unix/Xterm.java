package com.rightscale.bluesky.launchers.unix;

import com.rightscale.bluesky.*;
import com.rightscale.bluesky.launchers.*;
import java.io.*;

public class Xterm extends SimpleUnixLauncher {
    public Xterm(CommandRunner launchpad) {
        super(launchpad, "xterm -h", "xterm -e");

        if( isPlatform("Mac") ) {
            throw new RuntimeException("Wrong OS.");
        }
    }
}
