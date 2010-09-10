/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.bluesky.launchers;

import com.rightscale.bluesky.*;
import com.rightscale.util.*;
import java.io.*;

/**
 *
 * @author tony
 */
public abstract class SimpleLauncher implements Launcher {
    static private Runtime _runtime;

    protected CommandRunner _launchpad = null;
    
    public SimpleLauncher(CommandRunner launchpad) {
        _launchpad = launchpad;
    }

    abstract public void run(String command)
        throws IOException;

    static public Runtime getRuntime()
    {
        if(_runtime == null) {
            _runtime = Runtime.getRuntime();
        }
        
        return _runtime;
    }

    static public boolean isPlatform(String platform) {
        String osName = System.getProperty("os.name");
        return (osName.toLowerCase().indexOf(platform.toLowerCase()) != -1);
    }

    static protected boolean canInvoke(String command) {
        try {
            getRuntime().exec(command);
            return true;
        }
        catch(IOException e) {
            return false;
        }
    }

    public void runHeadless(String command)
        throws IOException
    {
        getRuntime().exec(command);
    }
}
