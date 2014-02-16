package com.rightscale.ssh.launchers;

import java.io.*;
import com.rightscale.ssh.KeyFormat;

public interface Launcher {
    public String  getFriendlyName();
    public boolean supportsKeyFormat(KeyFormat format);
    public boolean canPasswordAuth();
    public boolean canPublicKeyAuth();

    public void   run(String username, String hostname, File identity)
            throws IOException, UnsupportedAuthMethod;

    public void   run(String username, String hostname, String password)
            throws IOException, UnsupportedAuthMethod;
}
