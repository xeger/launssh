/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.ssh.launchers;

import com.rightscale.ssh.*;
import com.rightscale.util.*;
import java.io.*;
import java.util.*;

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

    public int getRequiredKeyFormat() {
        return OPENSSH_KEY_FORMAT;
    }

    public boolean canPublicKeyAuth() {
        return true;
    }

    static protected String defaults(String user, String host, File id)
        throws IOException
    {
        if(id != null) {
            /* Unices don't seem to like quotes around the file name */
            if( isPlatform("Linux") || isPlatform("BSD") || isPlatform("nix") ) {
                return "-i" + " " + id.getCanonicalPath() + " " + user + "@" + host;
            }
            else {
                return "-i" + " \"" + id.getCanonicalPath() + "\" " + user + "@" + host;
            }
        }
        else {
            return user + "@" + host;
        }
    }
}
