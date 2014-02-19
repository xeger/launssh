package net.xeger.ssh;

import java.io.*;
import net.xeger.ssh.KeyFormat;

public interface Launcher {
    public String  getFriendlyName();
    public boolean supportsKeyFormat(KeyFormat format);
    public boolean canPasswordAuth();
    public boolean canPublicKeyAuth();

    /**
     * Launch an SSH session to a remote server using public-key authentication.
     * 
     * @param username name to use when authenticating to the remote server.
     * @param hostname DNS hostname of the remote server
     * @param identity location of the private-key file to use during authentication
     * @throws IOException
     * @throws UnsupportedAuthMethod
     */
    public void   run(String username, String hostname, File identity)
            throws IOException, UnsupportedAuthMethod;

    /**
     * Launch an SSH session to a remote server using password-based authentication.
     * 
     * @param username name to use when authenticating to the remote server.
     * @param hostname DNS hostname of the remote server
     * @param password user's password 
     * @throws IOException
     * @throws UnsupportedAuthMethod
     */
    public void   run(String username, String hostname, String password)
            throws IOException, UnsupportedAuthMethod;
}
