package net.xeger.ssh.launchers.windows;

import net.xeger.ssh.*;

import java.io.*;

public class OpenSSH extends SimpleWindowsLauncher {
    private File _exe = null;
    
    public OpenSSH(Launchpad l) {
        super(l);
        
        _exe = findProgramFile("OpenSSH", "bin\\ssh");

        if( !isPlatform("Windows") || null == _exe ) {
            throw new RuntimeException("Wrong OS, or OpenSSH not found in default location.");
        }
    }

    public String getFriendlyName()
    {
        return "OpenSSH";
    }

    public boolean canPublicKeyAuth() {
        return true;
    }

    public boolean supportsKeyFormat(KeyFormat format) {
        return (format == KeyFormat.OPEN_SSH);
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

        getRuntime().exec(command, null, _exe.getParentFile());
    }
}
