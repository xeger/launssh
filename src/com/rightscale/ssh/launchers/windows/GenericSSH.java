package com.rightscale.ssh.launchers.windows;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.unix.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class GenericSSH extends SimpleWindowsLauncher {
    public GenericSSH(Launchpad l) {
        if( !isPlatform("Windows") || findExecutable("ssh") == null ) {
            throw new RuntimeException("Wrong OS, or 'ssh' command not found.");
        }
    }

    public String getFriendlyName() {
        return "Generic SSH";
    }

    public void run(String user, String host, File id) throws IOException {
      String[] command = {
        "cmd.exe",
        "/c",
        "start",
        "ssh",
        "-i",
        id.getCanonicalPath(),
        user + "@" + host
      };

      debugExec(command);

//      String[] command = {
//            "cmd.exe",
//            "/c",
//            "start cmd"
//        };
//        debugExec(command);
    }
}
