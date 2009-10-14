package com.rightscale.ssh.launchers.osx;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import com.rightscale.util.*;
import java.io.*;

public class Applescript extends OpenSshLauncher {
    private File      _terminalScript = null;
    private File      _itermScript    = null;

    public Applescript(Launchpad l) {
        super(l);
        
        if( !isPlatform("Mac") ) {
            throw new RuntimeException("Wrong OS");
        }
    }

    public void run(String username, String hostname, File identity)
        throws IOException
    {
      createScripts();
      
      String cmdline = defaults(username, hostname, identity);

      int     exitCode = -1;
      Process        p = null;
      
      try {
        String scr  = _itermScript.getCanonicalPath();
        p = getRuntime().exec( scr + " " + cmdline );
        exitCode = p.waitFor();
      }
      catch(InterruptedException e) {
        exitCode = -1;
      }

      if(exitCode != 0) {
        String scr  = _terminalScript.getCanonicalPath();
        p = getRuntime().exec( scr + " " + cmdline );

      }
    }

    private void createScripts() throws IOException {
        File dir = _launchpad.getSafeDirectory();

        File terminal = new File(dir, "RightScale_Terminal_Launcher");
        File iterm    = new File(dir, "RightScale_iTerm_Launcher");
        File wrapper  = new File(dir, "RightScale_SSH_Wrapper");


        if(terminal.exists())
            terminal.delete();
        if(iterm.exists())
            iterm.delete();
        if(wrapper.exists())
            wrapper.delete();


        FileUtils.writeResource(getClass(), "/RightScale_Terminal_Launcher", terminal);
        FileUtils.writeResource(getClass(), "/RightScale_iTerm_Launcher", iterm);
        FileUtils.writeResource(getClass(), "/RightScale_SSH_Wrapper", wrapper);

        getRuntime().exec("chmod 0700 " + terminal.getCanonicalPath());
        getRuntime().exec("chmod 0700 " + iterm.getCanonicalPath());
        getRuntime().exec("chmod 0700 " + wrapper.getCanonicalPath());


        //HACK - sleep for a bit so the JVM picks up the files' change of perms
        try {
            Thread.sleep(250);
        } catch(InterruptedException e) {}

        _terminalScript = terminal;
        _itermScript = iterm;
    }
}
