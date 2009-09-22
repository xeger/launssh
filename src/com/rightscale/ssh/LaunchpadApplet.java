package com.rightscale.ssh;

import com.rightscale.ssh.launchers.Launcher;
import com.rightscale.ssh.*;
import com.rightscale.util.*;
import com.rightscale.ssh.launchers.*;
import com.rightscale.ssh.launchers.java.*;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.applet.Applet;
import java.applet.AppletStub;
import java.security.*;

public class LaunchpadApplet
        extends Applet
        implements AppletStub
{
    public static final String CHOOSING        = "choosing";
    public static final String USING_MINDTERM  = "usingMindterm";
    public static final String FORCED_MINDTERM = "forcedMindterm";
    public static final String USING_NATIVE    = "usingNative";

    private RightScaleLaunchpad  _launchpad   = new RightScaleLaunchpad();
    private boolean              _ranNative   = false;
    private boolean              _ranMindterm = false;
    
    ////
    //// Methods that allow the page's JS to query our state
    ////

    public boolean ranNative() {
        return _ranNative;
    }

    public boolean ranMindterm() {
        return _ranMindterm;
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
    }

    ////
    //// Properties and accessors.
    ////

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

        //Fix up the "use native client" button's text for friendlier UI
        if(_launchpad.isNativeClientAvailable()) {
            _actRunNative.putValue(_actRunNative.NAME, "Use " + _launchpad.getNativeClientName());
        }

        //Initialize the UI (only if we haven't already done it)
        if(!_initialized) {
            initUI();
        }

        _initialized = true;
    }

    private void start_()
    {
        try {
            if( isAutorun() ) {
                autorun_();
            }
            else {
                choose_();
            }
        }
        catch(IOException e) {
            _launchpad.reportError("Encountered an error while invoking the SSH client.", e);
        }
    }

    private boolean autorun_()
            throws IOException
    {
        System.err.println("Attempting autorun...");

        boolean didLaunch = false;

        if( isAttemptNative() ) {
            try {
                setDisplayState(USING_NATIVE);
                _ranNative = didLaunch = _launchpad.runNative();
            }
            catch(IOException e) {
                didLaunch = false;
                _launchpad.reportError("Could not invoke your system's SSH client.", e);
            }
        }

        if(didLaunch) {
            return true;
        }

        try {
            if( isAttemptNative() ) {
                setDisplayState(FORCED_MINDTERM);
            }
            else {
                setDisplayState(USING_MINDTERM);
            }

            _ranMindterm = didLaunch = _launchpad.runMindterm(this, this);
        }
        catch(IOException e) {
            didLaunch = false;
            _launchpad.reportError("Could not invoke the MindTerm SSH applet.\nSorry, we can't connect you right now!", e);
        }

        return didLaunch;
    }

    private void choose_()
            throws IOException
    {
        if( _launchpad.isNativeClientAvailable() ) {
            setDisplayState(CHOOSING);
        }
        else {
            setDisplayState(FORCED_MINDTERM);
            _ranMindterm = _launchpad.runMindterm(this, this);
        }
    }

    ////
    //// UI fields and functions.
    ////

    boolean        _initialized = false;
    JPanel         _pnlMain     = null;

    Action _actTroubleshoot = new AbstractAction("Troubleshoot") {
        public void actionPerformed(ActionEvent evt) {
            URL url = getTroubleshootingLink();
            if(url != null) {
                LaunchpadApplet.this.getAppletContext().showDocument(url, "_blank");
            }
        }
    };

    Action _actRunNative = new AbstractAction("Use Native") {
        public void actionPerformed(ActionEvent evt) {
            try {
                _ranNative = _launchpad.runNative();
                setDisplayState(USING_NATIVE);
            }
            catch(IOException e) {
                _launchpad.reportError("Could not invoke your computer's SSH application.", e);
            }
        }
    };

    Action _actRunMindterm = new AbstractAction("Use Mindterm") {
        public void actionPerformed(ActionEvent evt) {
            try {
                _ranMindterm = _launchpad.runMindterm(LaunchpadApplet.this, LaunchpadApplet.this);
                setDisplayState(USING_MINDTERM);
            }
            catch(IOException e) {
                _launchpad.reportError("Could not invoke MindTerm.", e);
            }
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
        Container header           = createHeaderUI();

        //One panel for each display state the applet can be in
        Container pnlChoosing       = createChoosingUI(),
                  pnlUsingNative    = createUsingNativeUI(),
                  pnlUsingMindterm  = createUsingMindtermUI(),
                  pnlForcedMindterm = createForcedMindtermUI();

        //Add all of the initialized panels to the main (CardLayout) panel
        _pnlMain = new JPanel();
        _pnlMain.setLayout(new CardLayout());
        _pnlMain.add(pnlChoosing, CHOOSING);
        _pnlMain.add(pnlUsingNative, USING_NATIVE);
        _pnlMain.add(pnlUsingMindterm, USING_MINDTERM);
        _pnlMain.add(pnlForcedMindterm, FORCED_MINDTERM);

        //Add the main and header panels to ourself
        this.setBackground(Color.white);
        this.setLayout(new BorderLayout());
        this.add(header, BorderLayout.NORTH);
        this.add(_pnlMain, BorderLayout.CENTER);
    }

    private Container createHeaderUI() {
        Box pnl = Box.createVerticalBox();
        JLabel lbl = new JLabel( "Connecting to" );
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnl.add(lbl);
        lbl = new JLabel( getServer() );
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnl.add(lbl);
        pnl.add(Box.createRigidArea(new Dimension(1, 16)));
        return pnl;
    }

    private Container createChoosingUI() {
        JPanel pnl = new JPanel();
        Box pnlCenter = Box.createVerticalBox();
        Box pnlButtons = Box.createHorizontalBox();
        JLabel lbl = new JLabel( "How do you want to connect?" );
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        pnlButtons.add(new JButton(_actRunNative));
        pnlButtons.add(new JButton(_actRunMindterm));
        pnlCenter.add(pnlButtons);
        pnl.setLayout(new BorderLayout());
        pnl.add(pnlCenter, BorderLayout.CENTER);

        return pnl;
    }

    private Container createUsingNativeUI() {
        JPanel pnl = new JPanel();
        Box pnlCenter = Box.createVerticalBox();
        Box pnlButtons = Box.createHorizontalBox();
        JLabel lbl = new JLabel( _launchpad.getNativeClientName() + " will launch in a separate window." );
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        lbl = new JLabel( "If you encounter problems, use MindTerm instead." );
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
        pnlButtons.add(new JButton(_actRunMindterm));
        pnlCenter.add(pnlButtons);
        pnl.setLayout(new BorderLayout());
        pnl.add(pnlCenter, BorderLayout.CENTER);
        return pnl;
    }

    private Container createUsingMindtermUI() {
        JPanel pnl = new JPanel();
        Box pnlCenter = Box.createVerticalBox();
        JLabel lbl = new JLabel( "Using MindTerm pure-Java SSH client.");
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        lbl = new JLabel( "Session will display in a separate window.");
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
        lbl = new JLabel( "This window must stay open while using SSH.");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        lbl.setForeground(Color.RED);
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        pnl.setLayout(new BorderLayout());
        pnl.add(pnlCenter, BorderLayout.CENTER);
        return pnl;
    }

    private Container createForcedMindtermUI() {
        JPanel pnl = new JPanel();
        Box pnlCenter = Box.createVerticalBox();
        JLabel lbl = new JLabel( "Your system does not appear to contain a compatible SSH client.");
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        lbl = new JLabel( "Using MindTerm instead.");
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        lbl = new JLabel( "Session will display in a separate window.");
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        pnlCenter.add(Box.createRigidArea(new Dimension(1, 16)));
        lbl = new JLabel( "This window must stay open while using SSH.");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        lbl.setForeground(Color.RED);
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
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
    //// Overridden superclass methods used to keep MindTerm from trashing
    //// our components and layout.
    ////

    @Override
    public void setLayout(LayoutManager layout) {
        if(!_initialized) {
            super.setLayout(layout);
        }
    }

    @Override
    public Component add(Component comp) {
        if(!_initialized) {
            return super.add(comp);
        }
        else {
            return comp;
        }
    }

    @Override
    public void add(Component comp, Object constraints) {
        if(!_initialized) {
            super.add(comp, constraints);
        }
    }
}
