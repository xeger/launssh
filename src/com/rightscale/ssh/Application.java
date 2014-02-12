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
            app.run();
        }
        catch(IOException e) {
            app.log("Cannot launch SSH", e);
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
    public void run()
        throws IOException
    {
        Map keyMaterial = new HashMap();

        if( getAuthMethod().equals(AUTH_METHOD_PUBLIC_KEY) ) {
            if( getUserKeyPath() != null && hasUserKeyFile() ) {
                keyMaterial.put( new Integer(Launcher.OPENSSH_KEY_FORMAT), getUserKeyMaterial() );
            }
            else if( getServerKeyMaterial() != null ) {
                keyMaterial.put( new Integer(Launcher.OPENSSH_KEY_FORMAT), getServerKeyMaterial() );
            }
            else {
                boolean why = getUserKeyPath() != null && hasUserKeyFile();
                System.out.println("OpenSSH key material not found (path&file=" + why + ")");
            }

            if( getUserKeyPath() != null && hasUserPuttyKeyFile() ) {
                keyMaterial.put( new Integer(Launcher.PUTTY_KEY_FORMAT), getUserPuttyKeyMaterial() );
            }
            if( getServerPuttyKeyMaterial() != null ) {
                keyMaterial.put( new Integer(Launcher.PUTTY_KEY_FORMAT), getServerPuttyKeyMaterial() );
            }
            else {
                boolean why = getUserKeyPath() != null && hasUserPuttyKeyFile();
                System.out.println("PuTTY key material not found (path&file=" + why + ")");
            }

            if(keyMaterial.isEmpty() && getUserKeyPath() == null) {
                log("Unable to find a private key in the applet parameters.", null);
            }
        }

        if(getPassword() != null && getPassword().length() > 0) {
            _launchpad.setPassword(getPassword());
        }

        //Initialize the launchpad business logic
        _launchpad.setUsername(getUsername());
        _launchpad.setServer(getServer());
        _launchpad.setServerUUID(getServerUUID());
        _launchpad.setKeyMaterial(keyMaterial);

        //Fix up the "use native client" button's text for friendlier UI
        if(!_launchpad.isNativeClientAvailable()) {
            throw new UnsupportedOperationException("No native SSH clients are available");
        }

        _launchpad.run();
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
            return "root";
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
            return null;
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
        System.out.println(message);
    }	
}
