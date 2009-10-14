/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.ssh.launchers.java;

import java.applet.*;
import java.io.*;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import com.rightscale.util.*;


/**
 *
 * @author tony
 */
public class Mindterm extends SimpleLauncher {
    Applet     _applet    = null;
    AppletStub _stub      = null;
    Thread     _thread    = null;
    
    public Mindterm(Launchpad launchpad, Applet applet, AppletStub stub)
    {
        super(launchpad);
        
        _applet    = applet;
        _launchpad = launchpad;
        _stub      = stub;
    }

    public String getFriendlyName() {
        return "MindTerm";
    }

    public boolean canPublicKeyAuth() {
        return true;
    }

    public boolean canPasswordAuth() {
        return true;
    }

    public int getRequiredKeyFormat() {
        return OPENSSH_KEY_FORMAT;
    }

    public void run(String user, String host, File id) throws IOException {
        writeMindtermKey(id);
        launchMindterm();
    }

    public void run(String user, String host, String password) throws IOException {
        launchMindterm();
    }

    public void stop() {
        if(_thread != null) {
            _thread.stop();
        }
    }

    protected void writeMindtermKey(File privateKey)
            throws IOException
    {
        File dir = getMindtermDirectory();
        File mtkey = new File(dir, privateKey.getName());
        String matl = FileUtils.readText(privateKey);
        writePrivateKey(matl, mtkey, 60*5);
    }

    protected void launchMindterm() {
        MindtermRunner runner =
                new MindtermRunner(_launchpad, _applet, _stub);

        _thread = new Thread(runner);
        _thread.start();
    }

    protected File getMindtermDirectory() {
        File home = new File( System.getProperty("user.home") );
        return new File(home, "mindterm");
    }
}
class MindtermRunner implements Runnable {
    Launchpad  _launchpad = null;
    Applet     _applet    = null;
    AppletStub _stub      = null;

    public MindtermRunner(Launchpad l, Applet applet, AppletStub stub) {
        _launchpad = l;
        _applet    = applet;
        _stub      = stub;
    }

    public void run() {
       try {
          Class appletClass = Class.forName("com.mindbright.application.MindTerm");
          Applet mindterm = (Applet)appletClass.newInstance();

          mindterm.setStub(_stub);
          _applet.setLayout( new java.awt.GridLayout(1,0) );
          _applet.add(mindterm);

          mindterm.init();
          mindterm.start();
          }
        catch (Exception e) {
          _launchpad.reportError("Could not initialize MindTerm via launchpad; please contact customer care.", e);
        }

        _applet.validate();
    }
}