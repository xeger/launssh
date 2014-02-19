package net.xeger.ssh.launchers.unix;

import net.xeger.ssh.*;
import net.xeger.ssh.launchers.*;
import net.xeger.util.*;
import java.io.*;

public class SimpleUnixLauncher extends OpenSshLauncher {
    String    _command;
    File      _script;

    public SimpleUnixLauncher(Launchpad l, String testCommand, String realCommand)
    {
        super(l);

        _command   = realCommand;

        if( !canInvoke(testCommand) ) {
            throw new RuntimeException("'" + testCommand + "' command not found.");
        }
    }

    @Override
    public void run(String user, String host, File id) throws IOException {
      createScripts();
      
      String scr     = _script.getCanonicalPath();
      String command = _command + " " + scr + " " + defaults(user, host, id);

      getRuntime().exec(command);
    }

    private void createScripts()
        throws IOException
    {
        File dir = _launchpad.getSafeDirectory();
        File wrapper  = new File(dir, "RightScale_SSH_Wrapper");

        if(wrapper.exists())
            wrapper.delete();

        FileUtils.writeResource(getClass(), "/RightScale_SSH_Wrapper", wrapper);
        getRuntime().exec("chmod 0700 " + wrapper.getCanonicalPath());

        //HACK - sleep for a bit so the JVM picks up the files' change of perms
        try {
            Thread.sleep(250);
        } catch(InterruptedException e) {}

        _script = wrapper;

    }
}
