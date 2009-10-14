package com.rightscale.ssh.launchers.windows;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;

public class SecureCRT extends SimpleWindowsLauncher {
    private File _exe = null;
    
    public SecureCRT(Launchpad l) {
        super(l);
        
        _exe = findProgramFile("VanDyke Software\\SecureCRT", "SecureCRT");

        if(null == _exe) {
            _exe = findExecutable("SecureCRT");
        }

        if( !isPlatform("Windows") || null == _exe ) {
            throw new RuntimeException("Wrong OS, or SecureCRT not found in path.");
        }
    }

    public String getFriendlyName() {
        return "SecureCRT";
    }

    public boolean canPublicKeyAuth() {
        return true;
    }

    public int getRequiredKeyFormat() {
        return PUTTY_KEY_FORMAT;
    }

    public void run(String user, String host, File id) throws IOException {
        String command = _exe.getCanonicalPath() + " " +
                defaults(user, host, id, "/I");
        
        getRuntime().exec(command);
    }
}
