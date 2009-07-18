/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.ssh.launchers;

import com.rightscale.ssh.*;
import com.rightscale.util.*;
import java.io.*;

/**
 *
 * @author tony
 */
public abstract class SimpleLauncher implements Launcher {
    static private Runtime _runtime;

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

    static public void writePrivateKey(String key, File location, int timeout)
            throws IOException
    {
        File dir = location.getParentFile();

        if( !dir.isDirectory() ) {
            dir.mkdir();
        }

        if( !isPlatform("Windows") ) {
            getRuntime().exec("chmod 0700 " + dir.getCanonicalPath());
        }

        FileUtils.writeText(key, location);
        FileUtils.scheduleDelete(location, timeout);

        if( !isPlatform("Windows") ) {
            getRuntime().exec("chmod 0600 " + location.getCanonicalPath());
        }
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

    static protected String defaults(String user, String host, File identity)
        throws IOException
    {
        if(isPlatform("Windows")) {
            return defaults(user, host, identity, "/i");
        }
        else {
            return defaults(user, host, identity, "-i");
        }
    }
    
    static protected String defaults(String user, String host, File id, String sw)
        throws IOException
    {
        if(id != null) {
            /* Unices don't seem to like quotes around the file name */
            if( isPlatform("Linux") || isPlatform("BSD") || isPlatform("nix") ) {
                return sw + " " + id.getCanonicalPath() + " " + user + "@" + host;
            }
            else {
                return sw + " \"" + id.getCanonicalPath() + "\" " + user + "@" + host;
            }
        }
        else {
            return user + "@" + host;
        }
    }
}
