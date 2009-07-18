package com.rightscale.ssh.launchers.unix;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class Konsole extends SimpleLauncher {
    public Konsole(Launchpad l) {
        if( !canInvoke("konsole -h") ) {
            throw new RuntimeException("'konsole' command not found.");
        }
    }

    public void run(String user, String host, File id) throws IOException {
      String command = "konsole -e ssh " +
              defaults(user, host, id);

      getRuntime().exec(command);
    }
}
