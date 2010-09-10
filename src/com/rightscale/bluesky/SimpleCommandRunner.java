/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.bluesky;

import com.rightscale.bluesky.launchers.Launcher;
import com.rightscale.bluesky.*;
import com.rightscale.bluesky.launchers.*;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.applet.Applet;
import java.applet.AppletStub;
import java.security.*;
import java.util.regex.*;


/**
 *
 * @author tony
 */
public class SimpleCommandRunner
    implements CommandRunner
{
    static final private int KEY_DELETION_TIMEOUT = 300;

    static final private String[] LAUNCHERS = {
        "com.rightscale.bluesky.launchers.osx.Applescript",
        "com.rightscale.bluesky.launchers.unix.GnomeTerminal",
        "com.rightscale.bluesky.launchers.unix.Konsole",
        "com.rightscale.bluesky.launchers.unix.Xterm",
    };

    private String               _userdata    = null;
    private ArrayList            _launchers   = new ArrayList();

    ////
    //// Launchpad implementation
    ////

    public void init() {
        Class[]  paramTypes = {CommandRunner.class};
        Object[] params     = {this};

        //Initialize platform-native launchers
        for(int i = 0; i < LAUNCHERS.length; i++) {
            String cn = LAUNCHERS[i];

            try {
                Constructor ctor = Class.forName(cn).getConstructor(paramTypes);
                Launcher l = (Launcher) ctor.newInstance(params);
                System.err.println(cn + " is COMPATIBLE.");
                _launchers.add(l);
            }
            catch(Exception e) {
                Throwable t = e;
                while(t.getCause() != null)
                    t = t.getCause();

                System.err.println(cn + " is NOT compatible: " + t);
            }
        }
    }

    public void runHeadless(String command) {
        for(int i = 0; i < _launchers.size(); i++) {
            try {
                Launcher l = (Launcher)_launchers.get(i);
                l.runHeadless(command);
                return;
            }
            catch(Throwable t) {
                //go to next launcher...
            }
        }
    }

    public void run(String command) {
        for(int i = 0; i < _launchers.size(); i++) {
            try {
                Launcher l = (Launcher)_launchers.get(i);
                l.run(command);
                return;
            }
            catch(Throwable t) {
                //go to next launcher...
            }
        }
    }

    public File getSafeDirectory() {
        String dir = System.getProperty("user.home");
        dir = dir + "/.rightscale";
        return new File(dir);
    }

    public void reportError(String reason, Exception e) {
        if(e != null) {
            reason = reason + "\n" + e.toString();
        }


        if(e != null) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }

        report(JOptionPane.ERROR_MESSAGE, "Error", reason);
    }

    ////
    //// Additional UI convenience methods
    ////

    public void reportInfo(String message) {
        report(JOptionPane.INFORMATION_MESSAGE, "SSH Launcher", message);
    }

    public void report(int icon, String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, icon);
    }

    ////
    //// Read/write properties
    ////

    public String getUserdata() {
        return _userdata;
    }

    public void setUserdata(String userdata) {
        _userdata = userdata;
    }

    public boolean isNativeClientAvailable() {
        return (_launchers.size() > 0);
    }

    public String getNativeClientName() {
        if(_launchers.size() > 0) {
            return ((Launcher)_launchers.get(0)).getClass().getName();
        }
        else {
            return null;
        }
    }

    private void validate(String str) {
        Pattern p = Pattern.compile("[^A-Za-z0-9\\_\\-\\.]");
        Matcher m = p.matcher(str);
        if(m.matches()) {
            throw new SecurityException("Input contains unsafe characters: " + str);
        }
    }
}
