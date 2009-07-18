package com.rightscale.ssh;

import com.rightscale.ssh.launchers.Launcher;
import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import com.rightscale.ssh.launchers.java.*;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.applet.Applet;
import java.applet.AppletStub;
import java.security.Permission;

public class LaunchpadApplet
        extends Applet
        implements AppletStub, Launchpad, ActionListener
{
    private ArrayList _launchers   = new ArrayList();
    private Set       _requiredKeys= new HashSet();
    private Map       _writtenKeys = new HashMap();

    private int KEY_DELETION_TIMEOUT = 180;
    
    private String[] LAUNCHERS = {
        "com.rightscale.ssh.launchers.osx.MacTerminal",
        "com.rightscale.ssh.launchers.unix.GnomeTerminal",
        "com.rightscale.ssh.launchers.unix.Konsole",
        "com.rightscale.ssh.launchers.unix.Xterm",
        "com.rightscale.ssh.launchers.windows.PuTTY",
        "com.rightscale.ssh.launchers.windows.OpenSSH",
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

                if(haveKeyFormat(l.getRequiredKeyFormat())) {
                    _launchers.add(l);
                    _requiredKeys.add( new Integer(l.getRequiredKeyFormat()) );
                    System.err.println(cn + " is COMPATIBLE.");
                }
                else {
                    System.err.println(cn + " is UNAVAILABLE (missing required key format).");
                }
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
        boolean didError = false;

        try {
            writePrivateKeys();
        }
        catch(IOException e) {
            reportError("Could not write your private key file.", e);
            didError = true;
        }

        if(getAutorun() == false) {
            return;
        }

        System.err.println("Attempting autorun...");

        if( !didError && getAttemptNative() ) {
            try {
                didLaunch = runNative();
            }
            catch(IOException e) {
                didLaunch = false;
                reportError("Could not invoke your system's SSH client.", e);
                didError = true;
            }

            if(!didLaunch && !didError) {
                String err =
                    "Could not find your system's SSH client; make sure it is in your PATH.\n" +
                    "In the meantime, we will fall back on MindTerm.";
                reportInfo(err);
            }
        }

        if(!didLaunch) {
            try {
                runMindterm();
                didLaunch = true;
            }
            catch(IOException e) {
                didLaunch = false;
                reportError("Could not invoke the MindTerm SSH applet.\nSorry, we can't connect you right now!", e);
            }
        }
    }

    public void stop() {
        if(_mindterm != null) {
            _mindterm.stop();
            _mindterm = null;

        }
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if(e.getActionCommand() == "launchMindterm") {
                runMindterm();
            }
            else if(e.getActionCommand() == "launchNativeClient") {
                runNative();
            }
        }
        catch(IOException ex) {
            //TODO: report error
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

    protected void writePrivateKeys()
            throws IOException
    {
        if(_requiredKeys.contains(new Integer(Launcher.OPENSSH_KEY_FORMAT))) {
            String matl = getKeyMaterial();
            File   key  = getKeyFile();
            SimpleLauncher.writePrivateKey(matl, key, KEY_DELETION_TIMEOUT);

        }
        if(_requiredKeys.contains(new Integer(Launcher.PUTTY_KEY_FORMAT))) {
            String matl = getPuttyKeyMaterial();
            File   key  = getPuttyKeyFile();
            SimpleLauncher.writePrivateKey(matl, key, KEY_DELETION_TIMEOUT);
        }
    }
    protected boolean runMindterm()
            throws IOException
    {
        _mindterm = new Mindterm(this, this, this);
        _mindterm.run( getUsername(), getServer(), getKeyFile() );
        return true;
    }
    
    protected boolean runNative()
            throws IOException
    {
        //Try all the launchers in sequence
        Iterator it = _launchers.iterator();
        while( it.hasNext() ) {
            Launcher l = (Launcher)it.next();

            try {
                File keyFile = null;
                switch(l.getRequiredKeyFormat() ) {
                    case Launcher.OPENSSH_KEY_FORMAT:
                        keyFile = getKeyFile();
                        break;
                    case Launcher.PUTTY_KEY_FORMAT:
                        keyFile = getPuttyKeyFile();
                        break;
                    default:
                        throw new Error("Unsupported key format; should not get here!!");
                }

                l.run( getUsername(), getServer(), keyFile );
                return true;
            }
            catch(IOException e) {
                System.err.println(e.toString());
            }
        }

        return false;
    }

    protected boolean haveKeyFormat(int kf) {
        switch(kf) {
            case Launcher.OPENSSH_KEY_FORMAT:
                return getKeyMaterial() != null;
            case Launcher.PUTTY_KEY_FORMAT:
                return getPuttyKeyMaterial() != null;
            case Launcher.SSHCOM_KEY_FORMAT:
            default:
                return false; //TODO support OpenSSH keys
        }
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

    protected boolean getAutorun() {
        String v = getParameter("autorun");
        if(v == null) {
            return true;
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
        if(km == null) return null;
        return km.replace('*', '\n');
    }

    protected String getPuttyKeyMaterial() {
        String km = getParameter("putty-key-material");
        if(km == null) return null;
        return km.replace('*', '\n');
    }

    protected File getKeyFile() {
        return new File(getSafeDirectory(), getKeyName());
    }

    protected File getPuttyKeyFile() {
        return new File(getSafeDirectory(), getKeyName() + ".ppk");
    }

    public File getSafeDirectory() {
        String dir = System.getProperty("user.home");
        dir = dir + "/.rightscale";
        return new File(dir);
    }

    public void reportInfo(String message) {
        report(JOptionPane.INFORMATION_MESSAGE, "SSH Launcher", message);
    }

    public void reportError(String reason, Exception e) {
        if(e != null) {
            reason = reason + "\n" + e.getMessage();
        }


        if(e != null) {
            System.err.println(e.getMessage());
            if( getDebug() ) {
                e.printStackTrace(System.err);
            }
        }

        report(JOptionPane.ERROR_MESSAGE, "Error", reason);
    }

    public void report(int icon, String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, icon);
    }
}
