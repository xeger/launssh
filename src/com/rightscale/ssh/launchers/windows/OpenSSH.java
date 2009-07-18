package com.rightscale.ssh.launchers.windows;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class OpenSSH extends SimpleWindowsLauncher {
    private File _exe = null;
    
    public OpenSSH(Launchpad l) {
        throw new RuntimeException("Disabled due to Win32's inability to exec console apps properly.");

//        _exe = findProgramFile("OpenSSH", "bin\\ssh");
//
//        if( !isPlatform("Windows") || null == _exe ) {
//            throw new RuntimeException("Wrong OS, or OpenSSH not found in default location.");
//        }
    }

    public void run(String user, String host, File id) throws IOException {
        String command = "start \"" + _exe.getCanonicalPath() + "\" " +
                defaults(user, host, id);
        
        getRuntime().exec(command);
    }
}
