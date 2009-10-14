/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.ssh.launchers;

import com.rightscale.ssh.*;
import com.rightscale.util.*;
import java.io.*;

/**
 *
 * @author tony
 */
abstract public class OpenSshLauncher
    extends SimpleLauncher
{
    public OpenSshLauncher(Launchpad launchpad)
    {
        super(launchpad);
    }

    public String getFriendlyName()
    {
        return "OpenSSH";
    }

    static protected String defaults(String user, String host, File id)
        throws IOException
    {
        if(isPlatform("Windows")) {
            return defaults(user, host, id, "/i");
        }
        else {
            return defaults(user, host, id, "-i");
        }
    }

    static protected String defaults(String user, String host, File id, String sw)
        throws IOException
    {
        if(id != null) {
            /* Unices don't seem to like quotes around the file name */
            if( isPlatform("Linux") || isPlatform("BSD") || isPlatform("nix") ) {
                return sw + " " + id.getCanonicalPath() + " " + user + "@" + host;
            }
            else {
                return sw + " \"" + id.getCanonicalPath() + "\" " + user + "@" + host;
            }
        }
        else {
            return user + "@" + host;
        }
    }
}
