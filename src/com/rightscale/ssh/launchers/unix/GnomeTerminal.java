package com.rightscale.ssh.launchers.unix;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class GnomeTerminal extends SimpleLauncher {
    public GnomeTerminal(Launchpad l) {
        if( !canInvoke("gnome-terminal --help") ) {
            throw new RuntimeException("'gnome-terminal' command not found.");
        }
    }

    public void run(String user, String host, File id) throws IOException {
      String command = "gnome-terminal -x ssh " +
              defaults(user, host, id);

      getRuntime().exec(command);
    }
}
