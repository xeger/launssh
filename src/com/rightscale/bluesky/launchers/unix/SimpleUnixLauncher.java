package com.rightscale.bluesky.launchers.unix;

import com.rightscale.bluesky.*;
import com.rightscale.bluesky.launchers.*;
import com.rightscale.util.*;
import java.io.*;

public class SimpleUnixLauncher extends SimpleLauncher {
    String    _command;

    public SimpleUnixLauncher(CommandRunner l, String testCommand, String realCommand)
    {
        super(l);

        _command   = realCommand;

        if( !canInvoke(testCommand) ) {
            throw new RuntimeException("'" + testCommand + "' command not found.");
        }
    }

    @Override
    public void run(String command) throws IOException {
      getRuntime().exec(_command + " " + command);
    }
}
