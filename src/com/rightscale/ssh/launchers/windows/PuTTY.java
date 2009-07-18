package com.rightscale.ssh.launchers.windows;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class PuTTY extends SimpleWindowsLauncher {
    private File _exe = null;
    
    public PuTTY(Launchpad l) {
        throw new RuntimeException("Disabled due to PuTTY's proprietary key format.");

//        _exe = findExecutable("putty");
//
//        if(_exe == null) {
//            _exe = findProgramFile("PuTTY", "putty");
//        }
//
//        if( !isPlatform("Windows") || null == _exe ) {
//            throw new RuntimeException("Wrong OS, or PuTTY not found in path.");
//        }
    }

    public void run(String user, String host, File id) throws IOException {
        String command = _exe.getCanonicalPath() + " " +
                defaults(user, host, id);
        
        getRuntime().exec(command);
    }
}
