package com.rightscale.bluesky.launchers.osx;

import com.rightscale.bluesky.*;
import com.rightscale.bluesky.launchers.*;
import com.rightscale.util.*;
import java.io.*;

public class Applescript extends SimpleLauncher {
    private File      _terminalScript = null;
    private File      _itermScript    = null;

    public Applescript(CommandRunner l) {
        super(l);
        
        if( !isPlatform("Mac") ) {
            throw new RuntimeException("Wrong OS");
        }
    }

    public void run(String command)
        throws IOException
    {
      createScripts();
      
      int     exitCode = -1;
      Process        p = null;
      
      try {
        String scr  = _itermScript.getCanonicalPath();
        p = getRuntime().exec(scr + " " + command);
        exitCode = p.waitFor();
      }
      catch(InterruptedException e) {
        exitCode = -1;
      }

      if(exitCode != 0) {
        try {
            String scr  = _terminalScript.getCanonicalPath();
            p = getRuntime().exec(scr + " " + command);
            exitCode = p.waitFor();
        }
        catch(InterruptedException e) {
          exitCode = -1;
        }
      }

      System.err.println();
    }

    private void createScripts() throws IOException {
        File dir = _launchpad.getSafeDirectory();

        File terminal = new File(dir, "BlueSky_Terminal_Launcher");
        File iterm    = new File(dir, "BlueSky_iTerm_Launcher");


        if(terminal.exists())
            terminal.delete();
        if(iterm.exists())
            iterm.delete();

        FileUtils.writeResource(getClass(), "/BlueSky_Terminal_Launcher", terminal);
        FileUtils.writeResource(getClass(), "/BlueSky_iTerm_Launcher", iterm);

        runHeadless("chmod 0700 " + terminal.getCanonicalPath());
        runHeadless("chmod 0700 " + iterm.getCanonicalPath());


        //HACK - sleep for a bit so the JVM picks up the files' change of perms
        try {
            Thread.sleep(250);
        } catch(InterruptedException e) {}

        _terminalScript = terminal;
        _itermScript = iterm;
    }
}
