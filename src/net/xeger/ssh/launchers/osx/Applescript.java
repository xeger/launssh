package net.xeger.ssh.launchers.osx;

import net.xeger.ssh.*;
import net.xeger.ssh.launchers.*;
import net.xeger.util.*;
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

        if(terminal.exists())
            terminal.delete();
        if(iterm.exists())
            iterm.delete();

        FileUtils.writeResource(getClass(), "/RightScale_Terminal_Launcher", terminal);
        FileUtils.writeResource(getClass(), "/RightScale_iTerm_Launcher", iterm);

        getRuntime().exec("chmod 0700 " + terminal.getCanonicalPath());
        getRuntime().exec("chmod 0700 " + iterm.getCanonicalPath());


        //HACK - sleep for a bit so the JVM picks up the files' change of perms
        try {
            Thread.sleep(250);
        } catch(InterruptedException e) {}

        _terminalScript = terminal;
        _itermScript = iterm;
    }
}
