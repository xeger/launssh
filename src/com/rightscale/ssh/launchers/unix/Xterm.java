package com.rightscale.ssh.launchers.unix;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class Xterm extends SimpleLauncher {
    public Xterm(Launchpad l) {
        if( !canInvoke("xterm -h") ) {
            throw new RuntimeException("'xterm' command not found.");
        }
    }

    public void run(String user, String host, File id) throws IOException {
      String command = "xterm -e ssh " +
              defaults(user, host, id);

      getRuntime().exec(command);
    }
}
