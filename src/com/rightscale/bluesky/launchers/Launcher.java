package com.rightscale.bluesky.launchers;

import java.io.*;

public interface Launcher {
    public void   runHeadless(String command)
            throws IOException;

    public void   run(String command)
            throws IOException;
}

