package com.rightscale.bluesky;

import com.rightscale.bluesky.launchers.Launcher;
import com.rightscale.bluesky.*;
import com.rightscale.util.*;
import com.rightscale.bluesky.launchers.*;

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

public class CommandRunnerApplet
        extends Applet
        implements AppletStub
{
    private SimpleCommandRunner      _launchpad   = new SimpleCommandRunner();
    
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

    protected String getUserdata() {
        String userdata = getParameter("userdata");
        userdata = userdata.replace('*', '\n');
        return userdata;
    }

    protected File getScriptFile()
        throws IOException
    {
        //TODO account for Windows
        return new File(_launchpad.getSafeDirectory(), "BlueSky_Installer.sh").getCanonicalFile();
    }

    ////
    //// "Internal" Applet implementation and related methods; all require the
    //// caller to have already elevated privilege.
    ////

    private void init_()
    {
        //Initialize the launchpad business logic
        _launchpad.setUserdata(getUserdata());
        _launchpad.init();

        //Initialize the UI (only if we haven't already done it)
        if(!_initialized) {
            initUI();
            _initialized = true;
        }
    }

    private void start_()
    {
    }

    ////
    //// UI fields and functions.
    ////

    boolean        _initialized = false;
    JPanel         _pnlMain     = null;

    Action _actRunNative = new AbstractAction("Manage Me!") {
        public void actionPerformed(ActionEvent evt) {
            try {
                System.err.println("Writing script file to " + getScriptFile().getAbsolutePath());
                //TODO account for Windows
                FileUtils.writeResource(this.getClass(), "/BlueSky_Installer.sh", getScriptFile());
                _launchpad.runHeadless("chmod 0700 " + getScriptFile().getAbsolutePath());
                System.err.println("Changed mode of script file to 0700");

                File userDataFile = new File(_launchpad.getSafeDirectory(), "user-data.txt");
                FileUtils.writeText(getUserdata(), userDataFile);
                
                _launchpad.run("sudo " + getScriptFile().getAbsolutePath() + " ; read -p 'Press a key...' dontcare");
            }
            catch(IOException e) {
                _launchpad.reportError("FALL DOWN GO BOOM. :(", e);
            }
        }
    };


    private void initUI() {
        JPanel pnl = createPanel();
        Box pnlCenter = Box.createVerticalBox();
        Box pnlButtons = Box.createHorizontalBox();
        JLabel lbl = new JLabel( "Click the button to connect this computer to the RightScale dashboard." );
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        lbl.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        pnlCenter.add(lbl);
        pnlButtons.add(new JButton(_actRunNative));
        pnlCenter.add(pnlButtons);
        pnl.setLayout(new BorderLayout());
        pnl.add(pnlCenter, BorderLayout.CENTER);
        
        this.add(pnl);
    }

    private JPanel createPanel() {
        JPanel pnl = new JPanel();
        pnl.setBackground(Color.white);
        return pnl;
    }
}
