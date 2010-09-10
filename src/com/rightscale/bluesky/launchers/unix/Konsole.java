package com.rightscale.bluesky.launchers.unix;

import com.rightscale.bluesky.*;
import com.rightscale.bluesky.launchers.*;
import java.io.*;

public class Konsole extends SimpleUnixLauncher {
    public Konsole(CommandRunner launchpad) {
        super(launchpad, "konsole -h", "konsole -e");
    }
}
