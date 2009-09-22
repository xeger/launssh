package com.rightscale.ssh.launchers.unix;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class Konsole extends SimpleUnixLauncher {
    public Konsole(Launchpad launchpad) {
        super(launchpad, "konsole -h", "konsole -e");
    }
}
