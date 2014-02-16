package com.rightscale.ssh.launchers.windows;

import com.rightscale.ssh.*;

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

    public boolean canPublicKeyAuth() {
        return true;
    }

    public boolean canPasswordAuth() {
        return true;
    }

    public String getFriendlyName() {
        return "PuTTY";
    }

    public boolean supportsKeyFormat(KeyFormat format) {
        return (format == KeyFormat.PUTTY);
    }

    @Override
    public void run(String user, String host, File id)
        throws IOException
    {
        String[] cmd = {
            _exe.getCanonicalPath(),
            "-i",
            id.getCanonicalPath(),
            String.format("%s@%s", user, host)
        };

        getRuntime().exec(cmd);
    }

    public void run(String user, String host, String password)
        throws IOException
    {
        String[] cmd = {
            _exe.getCanonicalPath(),
            "-pw",
            password,
            String.format("%s@%s", user, host)
        };

        getRuntime().exec(cmd);
    }
}
