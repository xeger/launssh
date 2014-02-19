/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.xeger.ssh.launchers;

import net.xeger.ssh.*;
import net.xeger.util.*;
import java.io.*;

/**
 *
 * @author tony
 */
public abstract class SimpleLauncher implements Launcher {
    static private Runtime _runtime;

    protected Launchpad _launchpad = null;
    
    public SimpleLauncher(Launchpad launchpad) {
        _launchpad = launchpad;
    }

    public boolean canPasswordAuth() {
        return false;
    }

    public boolean canPublicKeyAuth() {
        return false;
    }

    public void run(String username, String hostname, File identity)
        throws IOException, UnsupportedAuthMethod
    {
        throw new UnsupportedAuthMethod("Public-key authentication is not supported by this SSH client.");
    }

    public void run(String username, String hostname, String password)
        throws IOException, UnsupportedAuthMethod
    {
        throw new UnsupportedAuthMethod("Password authentication is not supported by this SSH client.");
    }

    static public Runtime getRuntime()
    {
        if(_runtime == null) {
            _runtime = Runtime.getRuntime();
        }
        
        return _runtime;
    }

    /// Known platforms include: "Windows", "Mac"
    /// Not yet observed: Linux, AIX, Solaris, etc. (Capitalization?)
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
}
