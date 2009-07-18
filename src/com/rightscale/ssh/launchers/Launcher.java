package com.rightscale.ssh.launchers;

import java.io.*;

public interface Launcher {
    public static final int OPENSSH_KEY_FORMAT = 0;
    public static final int PUTTY_KEY_FORMAT   = 1;
    public static final int SSHCOM_KEY_FORMAT  = 2;

    public String getFriendlyName();
    public int    getRequiredKeyFormat();
    public void   run(String username, String hostname, File identity) throws IOException;
}

