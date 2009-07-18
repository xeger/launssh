package com.rightscale.ssh;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import com.rightscale.ssh.launchers.java.*;

import java.lang.reflect.*;
import java.applet.Applet;
import java.applet.AppletStub;
import java.security.Permission;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class LaunchpadApplet
        extends Applet
        implements AppletStub, Launchpad
{
    private ArrayList _launchers = new ArrayList();

    private String[] LAUNCHERS = {
        "com.rightscale.ssh.launchers.osx.MacTerminal",
        "com.rightscale.ssh.launchers.unix.GnomeTerminal",
        "com.rightscale.ssh.launchers.unix.Konsole",
        "com.rightscale.ssh.launchers.unix.Xterm",
        "com.rightscale.ssh.launchers.windows.GenericSSH"
    };

    private Mindterm _mindterm = null;
    
    public void appletResize( int width, int height ){
        resize( width, height );
    }
    
    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    public void init() {
        Class[]  paramTypes = {Launchpad.class};
        Object[] params     = {this};

        //Initialize platform-native launchers
        for(int i = 0; i < LAUNCHERS.length; i++) {
            String cn = LAUNCHERS[i];
            
            try {
                Constructor ctor = Class.forName(cn).getConstructor(paramTypes);
                Launcher l = (Launcher) ctor.newInstance(params);
                _launchers.add(l);
                System.err.println(cn + " is compatible.");
            }
            catch(Exception e) {
                Throwable t = e;
                while(t.getCause() != null)
                    t = t.getCause();
                
                System.err.println(cn + " is NOT compatible: " + t);
                if(getDebug()) {
                    e.printStackTrace(System.err);
                }
            }
        }

        //Prepare to hold another applet
        setBackground(Color.white);
    }

    /**
     * Called every time the browser requests a new instance of the applet.
     */
    public void start() {
        try {
            //Acquire privs we need in order to run
            elevatePrivilege();
        }
        catch(IOException e) {
            reportError("Failed to acquire the necessary privilege for launching SSH.",e);
        }

        boolean didLaunch = false;

        if( getAttemptNative() ) {
            try {
                didLaunch = runNative();
            }
            catch(IOException e) {
                didLaunch = false;
                reportError("Error invoking your system's SSH client.", e);
            }

            if(!didLaunch) {
                String err =
                    "Could not find your system's SSH client; make sure it is in your PATH.\n" +
                    "In the meantime, we will fall back on MindTerm.";
                reportError(err, null);
            }
        }

        if(!didLaunch) {
            try {
                runMindterm();
                didLaunch = true;
            }
            catch(IOException e) {
                didLaunch = false;
                reportError("Error invoking the MindTerm SSH applet.", e);
            }
        }
    }

    public void stop() {
        if(_mindterm != null) {
            _mindterm.stop();
            _mindterm = null;

        }
    }

    protected void elevatePrivilege() throws IOException {
        Permission p1 = new FilePermission("<<ALL FILES>>", "execute");
        System.getSecurityManager().checkPermission(p1);

        File home = new File(System.getProperty("user.home"));
        File hss = new File(home, "-");
        String shss = hss.getCanonicalPath();
        Permission p2 = new FilePermission(shss, "write");
    }

    protected boolean runMindterm()
            throws IOException
    {
        String matl = getKeyMaterial();
        File   key  = getKeyFile();
        SimpleLauncher.writePrivateKey(matl, key, 60);
        _mindterm = new Mindterm(this, this, this);
        _mindterm.run( getUsername(), getServer(), getKeyFile() );
        return true;
    }
    
    protected boolean runNative()
            throws IOException
    {
        String matl = getKeyMaterial();
        File   key  = getKeyFile();
        SimpleLauncher.writePrivateKey(matl, key, 60);

        //Try all the launchers in sequence
        Iterator it = _launchers.iterator();
        while( it.hasNext() ) {
            Launcher l = (Launcher)it.next();

            try {
                l.run( getUsername(), getServer(), key );
                return true;
            }
            catch(IOException e) {
                System.err.println(e.toString());
            }
        }

        return false;
    }

    protected boolean getDebug() {
        String v = getParameter("debug");
        if(v == null) {
            return false;
        }

        v = v.toLowerCase();
        if(v.startsWith("y") || v.startsWith("t") || v.startsWith("1")) {
            return true;
        }
        else {
            return false;
        }
    }

    protected boolean getAttemptNative() {
        String v = getParameter("native");
        if(v == null) {
            return false;
        }

        v = v.toLowerCase();
        if(v.startsWith("y") || v.startsWith("t") || v.startsWith("1")) {
            return true;
        }
        else {
            return false;
        }
    }

    protected String getUsername() {
        String v = getParameter("username");

        if(v != null)
            return v;
        else
            return "root";
    }

    protected String getServer() {
        String v = getParameter("server");

        if(v != null)
            return v;
        else
            return "localhost";
    }

    protected String getKeyName() {
        return getParameter("private-key");
    }

    protected String getKeyMaterial() {
        String km = getParameter("openssh-key-material");
        return km.replace('*', '\n');
    }

    protected File getKeyFile() {
        return new File(getSafeDirectory(), getKeyName());
    }

    public File getSafeDirectory() {
        String dir = System.getProperty("user.home");
        dir = dir + "/.rightscale";
        return new File(dir);
    }

    public void reportError(String reason, Exception e) {
        if(e != null) {
            reason = reason + "\n" + e.getMessage();
        }

        JOptionPane.showMessageDialog(null, reason, "SSH Error",
                JOptionPane.ERROR_MESSAGE);

        if(e != null) {
            System.err.println(e.getMessage());
            if( getDebug() ) {
                e.printStackTrace(System.err);
            }
        }
    }
}
