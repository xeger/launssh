package com.rightscale.ssh.launchers;

import java.io.*;

public interface Launcher {
    public static final int OPENSSH_KEY_FORMAT = 0;
    public static final int PUTTY_KEY_FORMAT   = 1;
    public static final int SSHCOM_KEY_FORMAT  = 2;

    public String  getFriendlyName();
    public boolean supportsKeyFormat(int format);
    public boolean canPasswordAuth();
    public boolean canPublicKeyAuth();

    public void   run(String username, String hostname, File identity)
            throws IOException, UnsupportedAuthMethod;

    public void   run(String username, String hostname, String password)
            throws IOException, UnsupportedAuthMethod;
}
