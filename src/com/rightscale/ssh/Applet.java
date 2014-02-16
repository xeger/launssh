package com.rightscale.ssh;

import com.rightscale.ssh.launchers.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.security.*;

public class Applet
        extends java.applet.Applet
        implements java.applet.AppletStub, com.rightscale.ssh.UI
{
	private static final long serialVersionUID = -7047031265889225736L;
	
	public static final String AUTH_METHOD_PUBLIC_KEY = "publickey";
    public static final String AUTH_METHOD_PASSWORD   = "password";
    
    public static final String NO_LAUNCHER        = "choosing";
    public static final String LAUNCHING    = "launching";
    public static final String MISSING_KEY     = "missingKey";

    private Launchpad            _launchpad   = new Launchpad(this);
    private boolean              _launched    = false;
    private boolean              _hadFailure  = false;
    
    ////
    //// Methods that allow the page's JS to query our state
    ////

    // This method is misnamed because the applet has an interface contract with its host pages. We
    // no longer have non-native launchers, so this method should just be didLaunch or something.
    // @todo rename this method when the applet params are refactored
    public boolean ranNative() {
        return _launched;
    }

    public boolean hadFailure() {
        return _hadFailure;
    }

    ////
    //// AppletStub implementation
    ////

    public void appletResize( int width, int height ){
        resize( width, height );
    }
    
    ////
    //// Applet implementation
    ////

    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    public void init() {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws IOException {
                    init_();
                    return null;
                }
            });
        }
        catch(PrivilegedActionException e) {
            log("Failed to acquire the privilege necessary to initialize the applet.", e);
        }
    }

    /**
     * Called every time the browser requests a new "instance" of the applet.
     */
    public void start() {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws IOException {
                    start_();
                    return null;
                }
            });
        }
        catch(PrivilegedActionException e) {
            log("Failed to acquire the privilege necessary to initialize the applet.", e);
        }
    }

    public void stop() {
    }

    ////
    //// Properties and accessors.
    ////

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
        if("password".equals(getParameter("auth-method")))
            return AUTH_METHOD_PASSWORD;
        else
            return AUTH_METHOD_PUBLIC_KEY;
    }

    protected String getSpecialPrivateKey() {
        String newline = System.getProperty("line.separator");
        String km = getParameter("openssh-key-material");
        if(km == null) return null;
        return km.replaceAll("\\*", newline);
    }

    protected String getSpecialPuttyPrivateKey() {
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
        return (f != null) && f.exists();
    }

    protected boolean hasUserPuttyKeyFile() {
        File f = getUserPuttyKeyFile();
        return (f != null) && f.exists();
    }

    protected String getUserPrivateKey()
    {
    	BufferedReader br = null;
    	StringBuffer   sb = null;
    	
        try {
            if(hasUserKeyFile()) {
                File f = getUserKeyFile();
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                sb = new StringBuffer();
                while(br.ready()) {
                    sb.append(br.readLine());
                    sb.append("\n");
                }

                return sb.toString();
            }
            else {
                throw new Error("User private key file does not exist");
            }
        }
        catch(Exception e) {
            return null;
        }
        finally {
        	if(br != null) {        		
        		try { br.close(); } catch(Exception e) {}
        	}
        }
    }

    protected String getUserPuttyPrivateKey()
    {
    	BufferedReader br = null;
    	StringBuffer   sb = null;
    	
        try {
            if(hasUserPuttyKeyFile()) {
                File f = getUserPuttyKeyFile();
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                sb = new StringBuffer();
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
        finally {
        	if(br != null) {        		
        		try { br.close(); } catch(Exception e) {}
        	}
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
    //// "Internal" Applet implementation and related methods; all require the
    //// caller to have already elevated privilege.
    ////

    private void openSession_()
    {
    	try {
    		_launched = _launchpad.run();
    	}
    	catch(Exception e) {
    		alert("Could not launch your computer's SSH client", e);
    	}
    }    

    private void init_()
    {
        _launched = _hadFailure = false;
        
        Map<Integer, String> privateKeys = new HashMap<Integer, String>();

        if( getAuthMethod().equals(AUTH_METHOD_PUBLIC_KEY) ) {
            if( getUserKeyPath() != null && hasUserKeyFile() ) {
                privateKeys.put( Launcher.OPENSSH_KEY_FORMAT, getUserPrivateKey() );
                log("Added user's private OpenSSH key to launcher; source: " + getUserKeyPath());
            }
            else if( getSpecialPrivateKey() != null ) {
                privateKeys.put( Launcher.OPENSSH_KEY_FORMAT, getSpecialPrivateKey() );
                log("Added special private OpenSSH key to launcher");
            }
            else {
                log(String.format("User OpenSSH key not found (userKeyPath=%s, hasUserKeyFile=%s)", getUserKeyPath() != null, hasUserKeyFile()));
            }

            if( getUserKeyPath() != null && hasUserPuttyKeyFile() ) {
                privateKeys.put( Launcher.PUTTY_KEY_FORMAT, getUserPuttyPrivateKey() );
                log("Added user's private PuTTY key to launcher; source: " + getUserKeyPath());
            }
            if( getSpecialPuttyPrivateKey() != null ) {
                privateKeys.put( Launcher.PUTTY_KEY_FORMAT, getSpecialPuttyPrivateKey() );
                log("Added special private PuTTY private key to launcher");
            }
            else {
                log(String.format("User PuTTY key not found (userKeyPath=%s, hasUserKeyFile=%s)", getUserKeyPath() != null, hasUserKeyFile()));
            }

            if(privateKeys.isEmpty() && getUserKeyPath() == null) {
                alert("Unable to find a private key in the applet parameters.");
            }
        }

        if(getPassword() != null && getPassword().length() > 0) {
            _launchpad.setPassword(getPassword());
        }

        //Initialize the launchpad business logic
        _launchpad.setUsername(getUsername());
        _launchpad.setServer(getServer());
        _launchpad.setServerUUID(getServerUUID());
        _launchpad.setPrivateKeys(privateKeys);

        if(_launchpad.isLauncherAvailable()) {
            _actrun.putValue(Action.NAME, "Launch " + _launchpad.getLauncherName());
        }
        else {
        	//
        	_actrun.putValue(Action.NAME, "Launch SSH");
        }

        //Initialize the UI (only if we haven't already done it)
        if(!_initialized) {
            initUI();
        }

        _initialized = true;
    }

    private void start_()
    {
        if( getUserKeyPath() != null && !hasUserKeyFile() && !hasUserPuttyKeyFile() ) {
            //We can't find the user's local key file -- just give up!
            _hadFailure = true;
            setDisplayState(MISSING_KEY);
        }
        else if( !_launchpad.isLauncherAvailable() ) {
            setDisplayState(NO_LAUNCHER);
        }
        else {
        	openSession_();
        }
    }

    ////
    //// UI fields and functions.
    ////

    boolean        _initialized = false;
    JPanel         _pnlMain     = null;

    @SuppressWarnings("serial")
	Action _actTroubleshoot = new AbstractAction("Troubleshoot") {
        public void actionPerformed(ActionEvent evt) {
            URL url = getTroubleshootingLink();
            
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(url.toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressWarnings("serial")
	Action _actrun = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
        	openSession_();
        }
    };

    private void setDisplayState(String newState) {
        if(_initialized) {
            CardLayout layout = (CardLayout)_pnlMain.getLayout();
            layout.show(_pnlMain, newState);
        }
    }

    private void initUI() {
        //A header that is shared between all display states
        Container header            = createHeaderUI();

        //One panel for each display state the applet can be in
        Container pnlNoLauncher     = createNoLauncherUI(),
                  pnlMissingKey     = createMissingKeyUI(),
        		  pnlLaunching      = createLaunchingUI();

        //Add all of the initialized panels to the main (CardLayout) panel
        _pnlMain = createPanel();
        _pnlMain.setLayout(new CardLayout());
        _pnlMain.add(pnlLaunching, LAUNCHING);
        _pnlMain.add(pnlMissingKey, MISSING_KEY);
        _pnlMain.add(pnlNoLauncher, NO_LAUNCHER);

        //Add the main and header panels to ourself
        this.setLayout(new BorderLayout());
        this.add(header, BorderLayout.NORTH);
        this.add(_pnlMain, BorderLayout.CENTER);
    }

    private JPanel createPanel() {
        JPanel pnl = new JPanel();
        pnl.setBackground(Color.white);
        return pnl;
    }

    private Container createHeaderUI() {
        JPanel pnl = createPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel( "Connecting to" );
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnl.add(lbl);
        lbl = new JLabel( getServerName() );
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnl.add(lbl);
        pnl.add(Box.createRigidArea(new Dimension(1, 16)));
        return pnl;
    }


    private Container createNoLauncherUI() {
        JPanel pnl = createPanel();
        Box pnlCenter = Box.createVerticalBox();

        JLabel lbl = new JLabel("No supported SSH client is available.");
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        lbl.setForeground(Color.RED);
        pnlCenter.add(lbl);

        if(SimpleLauncher.isPlatform("Windows")) {
	        lbl = new JLabel("Install PuTTY or OpenSSH");
        }
        else if(SimpleLauncher.isPlatform("Mac")) {
	        lbl = new JLabel("Confirm that AppleScript, Terminal.app and /usr/bin/ssh are installed and functioning normally");
        }
        else {
	        lbl = new JLabel("Install OpenSSH and a suppored terminal (gnome-terminal, konsole, xterm)");
        }
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);

        pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
        
        if(getTroubleshootingLink() != null) {
            pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
            Box pnlButtons = Box.createHorizontalBox();
            pnlButtons.add(new JButton(_actTroubleshoot));
            pnlCenter.add(pnlButtons);
        }

        pnl.setLayout(new BorderLayout());
        pnl.add(pnlCenter, BorderLayout.CENTER);
        return pnl;
    }

    private Container createLaunchingUI() {
        JPanel pnl = createPanel();
        Box pnlCenter = Box.createVerticalBox();
        Box pnlButtons = Box.createHorizontalBox();
        JLabel lbl = new JLabel( _launchpad.getLauncherName() + " will launch in a separate window." );
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
        JButton btnrun = new JButton(_actrun);
        pnlButtons.add(btnrun);
        pnlCenter.add(pnlButtons);
        pnl.setLayout(new BorderLayout());
        pnl.add(pnlCenter, BorderLayout.CENTER);
        return pnl;
    }

    private Container createMissingKeyUI() {
        JPanel pnl = createPanel();
        Box pnlCenter = Box.createVerticalBox();

        JLabel lbl = new JLabel("Missing private key file/material");
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);

        String path = null;
        try {
            path = getUserKeyFile().getCanonicalPath();
        }
        catch(IOException e) {
            //If we can't even find the canonical path of the file...
            path = getUserKeyPath();
        }
        catch(NullPointerException e) {
            //If no user key file was specified
            path = "(unknown)";
        }

        lbl = new JLabel(path);
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);

        pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
        
        lbl = new JLabel("Please change your SSH settings or create the file mentioned above.");
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        lbl.setForeground(Color.RED);
        pnlCenter.add(lbl);

        if(getTroubleshootingLink() != null) {
            pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
            Box pnlButtons = Box.createHorizontalBox();
            pnlButtons.add(new JButton(_actTroubleshoot));
            pnlCenter.add(pnlButtons);
        }

        pnl.setLayout(new BorderLayout());
        pnl.add(pnlCenter, BorderLayout.CENTER);
        return pnl;
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
