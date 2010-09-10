package com.rightscale.bluesky.launchers.unix;

import com.rightscale.bluesky.*;
import com.rightscale.bluesky.launchers.*;
import com.rightscale.util.*;
import java.io.*;

public class GnomeTerminal extends SimpleUnixLauncher {
    public GnomeTerminal(CommandRunner l) {
        super(l, "gnome-terminal --help", "gnome-terminal -x");
    }
}
