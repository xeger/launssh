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
import java.security.*;

public class LaunchpadApplet
        extends Applet
        implements AppletStub
{
    private boolean              _initialized = false;
    private RightScaleLaunchpad  _launchpad   = new RightScaleLaunchpad();

    public void appletResize( int width, int height ){
        resize( width, height );
    }
    
    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    public void init() {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    init_();
                    return null;
                }
            });
        }
        catch(PrivilegedActionException e) {
            _launchpad.reportError("Failed to acquire the privilege necessary to initialize the applet.", e);
        }
    }

    /**
     * Called every time the browser requests a new "instance" of the applet.
     */
    public void start() {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    start_();
                    return null;
                }
            });
        }
        catch(PrivilegedActionException e) {
            _launchpad.reportError("Failed to acquire the privilege necessary to initialize the applet.", e);
        }
    }

    public void stop() {
        // TODO delegate to RightScaleLauncher's _mindterm
    }

    protected boolean hasKeyFormat(int kf) {
        switch(kf) {
            case Launcher.OPENSSH_KEY_FORMAT:
                return getKeyMaterial() != null;
            case Launcher.PUTTY_KEY_FORMAT:
                return getPuttyKeyMaterial() != null;
            case Launcher.SSHCOM_KEY_FORMAT:
            default:
                return false; //TODO support SSH.com keys
        }
    }

    protected boolean isAutorun() {
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

    protected boolean isAttemptNative() {
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
        String newline = System.getProperty("line.separator");
        String km = getParameter("openssh-key-material");
        if(km == null) return null;
        return km.replaceAll("\\*", newline);
    }

    protected String getPuttyKeyMaterial() {
        String newline = System.getProperty("line.separator");
        String km = getParameter("putty-key-material");
        if(km == null) return null;
        return km.replaceAll("\\*", newline);
    }

    protected File getKeyFile() {
        return new File(_launchpad.getSafeDirectory(), getKeyName());
    }

    protected File getPuttyKeyFile() {
        return new File(_launchpad.getSafeDirectory(), getKeyName() + ".ppk");
    }

    ////
    //// Internal applet implementation; requires privilege
    ////

    private void init_()
    {
        Map keyMaterial = new HashMap();

        if( getKeyMaterial() != null ) {
            keyMaterial.put( new Integer(Launcher.OPENSSH_KEY_FORMAT), getKeyMaterial() );
        }

        if( getPuttyKeyMaterial() != null ) {
            keyMaterial.put( new Integer(Launcher.PUTTY_KEY_FORMAT), getPuttyKeyMaterial() );
        }

        //Initialize the launchpad business logic
        _launchpad.setUsername(getUsername());
        _launchpad.setServer(getServer());
        _launchpad.setKeyName(getKeyName());
        _launchpad.setKeyMaterial(keyMaterial);
        _launchpad.init();

        //Prepare to hold another applet
        setBackground(Color.white);

        //Remember we've been initialized
        _initialized = true;
    }

    private void start_()
    {
        try {
            if( isAutorun() ) {
                autorun_();
            }
        }
        catch(IOException e) {
            _launchpad.reportError("Could not automatically launch the SSH client.", e);
        }
    }

    private boolean autorun_()
            throws IOException
    {
        System.err.println("Attempting autorun...");

        boolean didLaunch = false;
        boolean didError  = false;

        if( isAttemptNative() ) {
            try {
                didLaunch = _launchpad.runNative();
            }
            catch(IOException e) {
                didLaunch = false;
                _launchpad.reportError("Could not invoke your system's SSH client.", e);
                didError = true;
            }

            if(!didLaunch && !didError) {
                String err =
                    "Could not find your system's SSH client; make sure it is in your PATH.\n" +
                    "In the meantime, we will fall back on MindTerm.";
                _launchpad.reportInfo(err);
            }
        }

        if(!didLaunch) {
            try {
                _launchpad.runMindterm(this, this);
                didLaunch = true;
            }
            catch(IOException e) {
                didLaunch = false;
                _launchpad.reportError("Could not invoke the MindTerm SSH applet.\nSorry, we can't connect you right now!", e);
            }
        }

        return didLaunch;
    }
}
