/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.xeger.ssh.launchers;

import net.xeger.ssh.*;

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

    public boolean supportsKeyFormat(KeyFormat format) {
        return (format == KeyFormat.OPEN_SSH);
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
