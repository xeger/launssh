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

    public boolean canPublicKeyAuth() {
        return true;
    }

    public boolean canPasswordAuth() {
        return true;
    }

    public String getFriendlyName() {
        return "PuTTY";
    }

    public int getRequiredKeyFormat() {
        return PUTTY_KEY_FORMAT;
    }

    @Override
    public void run(String user, String host, File id)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();

        sb.append(_exe.getCanonicalPath());
        sb.append( String.format(" -i \"%s\"", id.getCanonicalPath()) );
        sb.append( String.format(" %s@%s", user, host) );

        getRuntime().exec(sb.toString());
    }

    public void run(String user, String host, String password)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();

        sb.append(_exe.getCanonicalPath());
        sb.append( String.format(" -pw \"%s\"", password) );
        sb.append( String.format(" %s@%s", user, host) );

        getRuntime().exec(sb.toString());
    }
}
