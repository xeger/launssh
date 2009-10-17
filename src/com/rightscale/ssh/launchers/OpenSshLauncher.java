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

    @Override
    public boolean canPublicKeyAuth() {
        return true;
    }

    protected String defaults(String user, String host, File id)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();

        File known_hosts = new File(_launchpad.getSafeDirectory(), "known_hosts");

        sb.append( " -o StrictHostKeyChecking=no" );

        /* Unices don't seem to like quotes around the file name */
        if( isPlatform("Linux") || isPlatform("BSD") || isPlatform("nix") ) {
            sb.append( String.format(" -o UserKnownHostsFile=%s", known_hosts.getCanonicalPath()) );
            if(id != null)
                sb.append( String.format(" -i %s", id.getCanonicalPath()) );
            if(_launchpad.getServerUUID() != null)
                sb.append( String.format(" -o HostKeyAlias=%s", _launchpad.getServerUUID()) );
        /* Windowses NEED quotes around the file name */
        }
        else {
            sb.append( String.format(" -o UserKnownHostsFile=\"%s\"", known_hosts.getCanonicalPath()) );
            if(id != null) 
                sb.append( String.format(" -i \"%s\"", id.getCanonicalPath()) );
            if(_launchpad.getServerUUID() != null)
                sb.append( String.format(" -o HostKeyAlias=\"%s\"", _launchpad.getServerUUID()) );
        }

        sb.append( String.format(" %s@%s", user, host) );

        return sb.toString();
    }
}
