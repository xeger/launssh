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
import java.util.regex.*;


/**
 *
 * @author Tony Spataro
 */
public class Launchpad
{
    static final private int KEY_DELETION_TIMEOUT = 300;

    static final private String[] LAUNCHERS = {
        "com.rightscale.ssh.launchers.osx.Applescript",
        "com.rightscale.ssh.launchers.unix.GnomeTerminal",
        "com.rightscale.ssh.launchers.unix.Konsole",
        "com.rightscale.ssh.launchers.unix.Xterm",
        "com.rightscale.ssh.launchers.windows.PuTTY",
        "com.rightscale.ssh.launchers.windows.OpenSSH",
        "com.rightscale.ssh.launchers.windows.GenericSSH"
    };

    private UI                   _ui = null;
    private String               _username    = null;
    private String               _server      = null;
    private String               _serverUUID     = null;
    private Map                  _keyMaterial = null;
    private String               _password    = null;
    private ArrayList            _launchers   = new ArrayList();
    private String               _nativeClientStatus = null;
    private Mindterm             _mindterm    = null;
    private Set                  _requiredKeys= new HashSet();
    private Map                  _writtenKeys = new HashMap();

    ////
    //// Launchpad implementation
    ////

    public Launchpad(UI ui) {
        _ui = ui;

        initializeLaunchers();
    }

    public File getSafeDirectory() {
        String dir = System.getProperty("user.home");
        dir = dir + "/.rightscale";
        return new File(dir);
    }

    ////
    //// Read/write properties
    ////

    public String getKeyMaterial(int format) {
        Object o = _keyMaterial.get(new Integer(format));
        if(o != null) {
            return (String)o;
        }
        else {
            return null;
        }
    }

    public void setKeyMaterial(Map keyMaterial) {
        _keyMaterial = keyMaterial;
    }

    public boolean hasKeyMaterial() {
        return !( (_keyMaterial == null) || _keyMaterial.isEmpty() );
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        validate(password);
        _password = password;
    }

    public boolean hasPassword() {
        return (_password != null);
    }
    
    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        validate(username);
        _username = username;
    }

    public String getServer() {
        return _server;
    }

    public void setServer(String server) {
        validate(server);
        _server = server;
    }
    
    public String getServerUUID() {
        return _serverUUID;
    }

    public void setServerUUID(String serverUUID) {
        validate(serverUUID);
        _serverUUID = serverUUID;
    }


    ////
    //// Read-only and derived properties
    ////

    public boolean hasKeyFormat(int keyFormat) {
        return ( _keyMaterial.get(new Integer(keyFormat)) != null );
    }

    public boolean isNativeClientAvailable() {
        return (_launchers.size() > 0);
    }

    public String getNativeClientName() {
        if(_launchers.size() > 0) {
            return ((Launcher)_launchers.get(0)).getFriendlyName();
        }
        else {
            return "none supported";
        }
    }

    public String getNativeClientStatus() {
        return _nativeClientStatus;
    }

    public File getKeyFile(int keyFormat) {
        if(getServerUUID() == null) {
            return null;
        }

        switch(keyFormat) {
            case Launcher.OPENSSH_KEY_FORMAT:
                return new File(getSafeDirectory(), getServerUUID());
            case Launcher.PUTTY_KEY_FORMAT:
                return new File(getSafeDirectory(), getServerUUID() + ".ppk");
            default:
                throw new Error("Unsupported key format");
        }
    }

    ////
    //// Methods that do useful stuff
    ////

    public void writePrivateKeys()
            throws IOException
    {
        if(_requiredKeys.contains(new Integer(Launcher.OPENSSH_KEY_FORMAT))) {
            String matl = getKeyMaterial(Launcher.OPENSSH_KEY_FORMAT);
            File   key  = getKeyFile(Launcher.OPENSSH_KEY_FORMAT);

            if(matl != null && key != null) {
                SimpleLauncher.writePrivateKey(matl, key, KEY_DELETION_TIMEOUT);
            }
            else {
                _ui.log("Not writing OpenSSH key material (key material or name is null)");
            }

        }
        if(_requiredKeys.contains(new Integer(Launcher.PUTTY_KEY_FORMAT))) {
            String matl = getKeyMaterial(Launcher.PUTTY_KEY_FORMAT);
            File   key  = getKeyFile(Launcher.PUTTY_KEY_FORMAT);

            if(matl != null && key != null) {
                SimpleLauncher.writePrivateKey(matl, key, KEY_DELETION_TIMEOUT);
            }
            else {
                _ui.log("Not writing PuTTY key material (key material or name is null)");
            }
        }
    }

    public boolean run()
            throws IOException
    {
        try {
            writePrivateKeys();
        }
        catch(IOException e) {
            _ui.log("Could not write your private key file.", e);
            return false;
        }

        //Try all the launchers in sequence
        Iterator it = _launchers.iterator();
        while( it.hasNext() ) {
            Launcher l = (Launcher)it.next();

            _ui.log("  Running " + l.getClass().getName());

            try {
                if(hasKeyMaterial()) {
                    File keyFile = getKeyFile(l.getRequiredKeyFormat());
                    l.run( getUsername(), getServer(), keyFile );
                    return true;
                }
                else if(hasPassword()) {
                    l.run( getUsername(), getServer(), getPassword() );
                    return true;
                }
            }
            catch(Exception e) {
                _ui.log("Failed to launch using " + l.getFriendlyName(), e);
            }
        }

        return false;
    }

    private void initializeLaunchers() {
        Class[]  paramTypes = {Launchpad.class};
        Object[] params     = {this};

        //Initialize platform-native launchers
        for(int i = 0; i < LAUNCHERS.length; i++) {
            String cn = LAUNCHERS[i];

            try {
                Constructor ctor = Class.forName(cn).getConstructor(paramTypes);
                Launcher l = (Launcher) ctor.newInstance(params);

                if(!hasPassword() && !hasKeyFormat(l.getRequiredKeyFormat())) {
                    _ui.log(cn + " is UNAVAILABLE (missing required key format).");
                    _nativeClientStatus = l.getFriendlyName() + " requires a key format that is unavailable.";
                    continue;
                }

                if(!hasKeyMaterial() && !l.canPasswordAuth()) {
                    _ui.log(cn + " is UNAVAILABLE (password-based auth unsupported).");
                    _nativeClientStatus = l.getFriendlyName() + " does not support noninteractive password authentication.";
                    continue;
                }

                if(!hasPassword() && !l.canPublicKeyAuth())
                {
                    _ui.log(cn + " is UNAVAILABLE (public-key auth unsupported).");
                    _nativeClientStatus = l.getFriendlyName() + " does not support public-key authentication.";
                    continue;
                }

                _ui.log(cn + " is COMPATIBLE.");
                _launchers.add(l);
                if(l.canPublicKeyAuth()) {
                    _requiredKeys.add( new Integer(l.getRequiredKeyFormat()) );
                }
            }
            catch(Exception e) {
                _ui.log(cn + " is INCOMPATIBLE (threw exception during initialization)", e);
            }
        }

        //OpenSSH format is always required (for PuTTY)
        _requiredKeys.add(new Integer(Launcher.OPENSSH_KEY_FORMAT));
    }
    
    private void validate(String str) {
        Pattern p = Pattern.compile("[^A-Za-z0-9\\_\\-\\.]");
        Matcher m = p.matcher(str);
        if(m.matches()) {
            throw new SecurityException("Input contains unsafe characters: " + str);
        }
    }
}
