package com.rightscale.ssh.launchers.windows;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class PuTTY extends SimpleWindowsLauncher {
    private File _exe = null;
    
    public PuTTY(Launchpad l) {
        super(l);
        
        _exe = findProgramFile("PuTTY", "putty");

        if(_exe == null) {
            _exe = findExecutable("putty");
        }

        if( !isPlatform("Windows") || null == _exe ) {
            throw new RuntimeException("Wrong OS, or PuTTY not found in path.");
        }
    }

    public String getFriendlyName() {
        return "PuTTY";
    }

    public int getRequiredKeyFormat() {
        return PUTTY_KEY_FORMAT;
    }

    public void run(String user, String host, File id) throws IOException {
        String command = _exe.getCanonicalPath() + " " +
                defaults(user, host, id, "-i");

        System.err.println(command);
        getRuntime().exec(command);
    }
}
