package com.rightscale.ssh;

import com.rightscale.util.*;
import com.rightscale.ssh.launchers.*;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.jnlp.*;
import javax.swing.*;
import java.util.*;
import java.security.*;

public class Application
    implements com.rightscale.ssh.UI
{
    public static final String AUTH_METHOD_PUBLIC_KEY = "publickey";
    public static final String AUTH_METHOD_PASSWORD   = "password";

	public static void main(String args[]) {
        Application app = new Application(args);

        try {
            boolean result = app.run();
            System.exit(result ? 0 : -1);
        }
        catch(IllegalArgumentException e) {
            app.alert("Cannot launch SSH: " + e.getMessage());
            System.exit(-2);            
        }
        catch(Exception e) {
            app.alert("Cannot launch SSH", e);
            System.exit(-3);
        }
	}

    private Map<String, String>  _parameters  = new HashMap<String, String>();
    private Launchpad            _launchpad   = new Launchpad(this);

    Application(String[] args) {
        for(String s : args) {
            String[] pair = s.split("=");

            if(pair.length == 2) {
               _parameters.put(pair[0], pair[1]);
            }
            else {
               throw new IllegalArgumentException("Malformed command-line argument; expecting 'k=v'");
            }
        }
    }

    /// Run the application.
    public boolean run()
        throws IOException
    {
        Map keyMaterial = new HashMap<Integer, String>();

        if( getAuthMethod().equals(AUTH_METHOD_PUBLIC_KEY) ) {
            if( getUserKeyPath() != null && hasUserKeyFile() ) {
                keyMaterial.put( Launcher.OPENSSH_KEY_FORMAT, getUserKeyMaterial() );
                log("User OpenSSH private key loaded from local disk");
            }
            else if( getServerKeyMaterial() != null ) {
                keyMaterial.put( Launcher.OPENSSH_KEY_FORMAT, getServerKeyMaterial() );
                log("Server-specific OpenSSH private key loaded from parameters");
            }
            else {
                log("No OpenSSH private key loaded");
            }

            if( getUserKeyPath() != null && hasUserPuttyKeyFile() ) {
                keyMaterial.put( Launcher.PUTTY_KEY_FORMAT, getUserPuttyKeyMaterial() );
                log("User PuTTY private key loaded from local disk");
            }
            if( getServerPuttyKeyMaterial() != null ) {
                keyMaterial.put( Launcher.PUTTY_KEY_FORMAT, getServerPuttyKeyMaterial() );
                log("Server-specific PuTTY private key loaded from local disk");
            }
            else {
                boolean why = getUserKeyPath() != null && hasUserPuttyKeyFile();
                log("No PuTTY private key loaded");
            }

            if(keyMaterial.isEmpty() && getUserKeyPath() == null) {
                throw new IllegalArgumentException("Unable to identify a private key; add openssh-key-material=, putty-key-material= or user-key-path=");
            }
        }
        else if( getAuthMethod().equals(AUTH_METHOD_PASSWORD)) {
            if(getPassword() != null && getPassword().length() > 0) {
                _launchpad.setPassword(getPassword());
            }
            else {
                    throw new IllegalArgumentException("Unable to determine password; add password=");
            }            
        }

        String uuid = getServerUUID();
        if(uuid == null) {
            uuid = getServer();
        }

        //Initialize the launchpad business logic
        _launchpad.setUsername(getUsername());
        _launchpad.setServer(getServer());
        _launchpad.setServerUUID(uuid);
        _launchpad.setKeyMaterial(keyMaterial);

        //Fix up the "use native client" button's text for friendlier UI
        if(_launchpad.isNativeClientAvailable()) {
            return _launchpad.run();
        }
        else {
            throw new IllegalArgumentException("No supported SSH client is available.");
        }
    }

    ////
    //// An Applet workalike method for accessing command line parameters, plus getters for the various params we expect
    ////

    protected String getParameter(String name) {
        return _parameters.get(name);
    }

    protected String getUsername() {
        String v = getParameter("username");

        if(v != null)
            return v;
        else
            return System.getProperty("user.name");
    }

    protected String getServer() {
        String v = getParameter("server");

        if(v != null)
            return v;
        else
            return "localhost";
    }

    protected String getServerUUID() {
        return getParameter("server-uuid");
    }

    protected String getServerName() {
        String v = getParameter("server-name");

        if(v != null)
            return v;
        else
            return getServer();
    }

    protected String getAuthMethod() {
        if("publickey".equals(getParameter("auth-method")))
            return AUTH_METHOD_PUBLIC_KEY;
        else if("password".equals(getParameter("auth-method")))
            return AUTH_METHOD_PASSWORD;
        else
            return AUTH_METHOD_PUBLIC_KEY;
    }

    protected String getServerKeyMaterial() {
        String newline = System.getProperty("line.separator");
        String km = getParameter("openssh-key-material");
        if(km == null) return null;
        return km.replaceAll("\\*", newline);
    }

    protected String getServerPuttyKeyMaterial() {
        String newline = System.getProperty("line.separator");
        String km = getParameter("putty-key-material");
        if(km == null) return null;
        return km.replaceAll("\\*", newline);
    }

    protected String getPassword() {
        return getParameter("password");
    }

    protected String getUserKeyPath() {
        return getParameter("user-key-path");
    }

    protected File getServerKeyFile() {
        return new File(_launchpad.getSafeDirectory(), getServerUUID());
    }

    protected File getServerPuttyKeyFile() {
        return new File(_launchpad.getSafeDirectory(), getServerUUID() + ".ppk");
    }

    protected File getUserKeyFile() {
        String path = getUserKeyPath();
        if(path == null)
            return null;


        //Split the path into elements, accepting either \ or / as a separator
        String[] elements = path.split("/|\\\\");

        String home = System.getProperty("user.home");

        StringBuffer canonPath = new StringBuffer();
        canonPath.append(home);

        for(String elem : elements) {
            canonPath.append(File.separator);
            canonPath.append(elem);
        }

        return new File(canonPath.toString());
    }

    protected File getUserPuttyKeyFile() {
        File f = getUserKeyFile();
        String s = f.getPath();

        if(s.endsWith(".ppk")) {
            return f;
        }
        else {
            return new File(s + ".ppk");
        }
    }

    protected boolean hasUserKeyFile() {
        File f = getUserKeyFile();
        return f.exists();
    }

    protected boolean hasUserPuttyKeyFile() {
        File f = getUserPuttyKeyFile();
        return f.exists();
    }

    protected String getUserKeyMaterial()
    {
        try {
            if(hasUserKeyFile()) {
                File f = getUserKeyFile();
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                StringBuffer sb = new StringBuffer();
                while(br.ready()) {
                    sb.append(br.readLine());
                    sb.append("\n");
                }

                return sb.toString();
            }
            else {
                throw new Error("Key file does not exist");
            }
        }
        catch(Exception e) {
            return null;
        }
    }

    protected String getUserPuttyKeyMaterial()
    {
        try {
            if(hasUserPuttyKeyFile()) {
                File f = getUserPuttyKeyFile();
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                StringBuffer sb = new StringBuffer();
                while(br.ready()) {
                    sb.append(br.readLine());
                    sb.append("\n");
                }

                return sb.toString();
            }
            else {
                throw new Error("Key file does not exist");
            }
        }
        catch(Exception e) {
            return null;
        }
    }

    protected URL getTroubleshootingLink() {
        try {
            return new URL( getParameter("troubleshooting-url") );
        }
        catch(MalformedURLException e) {
            return null;
        }
    }

    ////
    //// Implementation of com.rightscale.ssh.UI
    ////

    public void log(String message) {
        System.out.println(message);
    }

    public void log(String message, Throwable problem) {
        System.err.println(String.format("%s - %s: %s", message, problem.getClass().getName(), problem.getMessage()));
    }	

    public void alert(String message) {
        log(message);
        JOptionPane.showMessageDialog(null, message, "SSH Launcher", JOptionPane.INFORMATION_MESSAGE);
    }

    public void alert(String message, Throwable problem) {
        log(message, problem);
        JOptionPane.showMessageDialog(null, String.format("%s\n(%s: %s)", message, problem.getClass().getName(), problem.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);        
    }
}
