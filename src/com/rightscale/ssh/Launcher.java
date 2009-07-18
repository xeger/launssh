package com.rightscale.ssh;

import java.io.*;

public interface Launcher {
    public void run(String username, String hostname, File identity) throws IOException;
}

