/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.ssh.launchers.windows;

import com.rightscale.ssh.*;
import com.rightscale.ssh.launchers.*;
import java.io.*;
import java.util.Vector;

/**
 *
 * @author tony
 */
public abstract class SimpleWindowsLauncher extends SimpleLauncher {
    public SimpleWindowsLauncher(Launchpad l) {
        super(l);
    }

    public static File[] getPathEnv() {
        String path = System.getenv("PATH");
        String[] comps = path.split(";|:");

        if(comps.length > 0) {
            Vector fcomps = new Vector();
            for(int i = 0; i < comps.length; i++) {
                String comp = comps[i];
                fcomps.add(new File(comp));
            }

            return vectorToFileArray(fcomps);
        }
        else {
            return null;
        }
    }

    public static File findProgramFile(String dir, String basename) {
        Vector paths = new Vector();

        if(System.getenv("ProgramFiles") != null) {
            paths.add( new File(System.getenv("ProgramFiles")) );

        }
        if(System.getenv("ProgramFiles(x86)") != null) {
            paths.add( new File(System.getenv("ProgramFiles(x86)")) );
        }

        File[] bases = vectorToFileArray(paths);

        for(int i = 0; i < bases.length; i++) {
            File base = bases[i];
            File pdir = new File(base, dir);
            File exe = new File(pdir, basename + ".exe");
            File bat = new File(pdir, basename + ".bat");
            File cmd = new File(pdir, basename + ".cmd");

            if(exe.exists())
                return exe;
            if(bat.exists())
                return bat;
            if(cmd.exists())
                return cmd;
        }

        return null;
    }

    public static File findExecutable(String basename) {
        File[] paths = getPathEnv();

        for(int i = 0; i < paths.length; i++) {
            File p = paths[i];
            File[] exes = {
                new File(p, basename + ".exe"),
                new File(p, basename + ".bat"),
                new File(p, basename + ".cmd"),
            };

            for(int j = 0; j < exes.length; j++) {
                File exe = exes[j];
                if( exe.exists() )
                    return exe;
            }
        }

        return null;
    }

    public static void debugExec(String[] command)
        throws IOException
    {
        Process p = getRuntime().exec(command);

        InputStream stdout =
            p.getInputStream();
        InputStream stderr =
            p.getErrorStream();

        InputStreamReader isr =
            new InputStreamReader(stdout);
        InputStreamReader isrErr =
            new InputStreamReader(stderr);

        BufferedReader br =
            new BufferedReader(isr);
        BufferedReader brErr =
            new BufferedReader(isrErr);

        System.err.println("---output trace---");
        String line = null;
        do {
            try {
                line = br.readLine();
                if(line != null) {
                    System.err.println("1> " + line);
                }
            }
            catch(IOException e) {
                System.err.println("---" + e + "---");
                line = null;
            }
        } while(line != null);
        do {
            try {
                line = brErr.readLine();
                if(line != null) {
                    System.err.println("2> " + line);
                }
            }
            catch(IOException e) {
                System.err.println("---" + e + "---");
                line = null;
            }
        } while(line != null);

        try {
            if (p.waitFor() != 0) {
                System.err.println("---exit value=" + p.exitValue() + "---");
            }
        }
        catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    private static File[] vectorToFileArray(Vector v) {
        File[] files = new File[v.size()];

        for(int i = 0; i < files.length; i++) {
            files[i] = (File)v.get(i);
        }

        return files;
    }
}
