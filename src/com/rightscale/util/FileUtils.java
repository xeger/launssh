/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.util;

import java.io.*;

/**
 *
 * @author tony
 */
public class FileUtils {
    public static String readText(File location)
            throws IOException
    {
        FileInputStream fis = new FileInputStream(location);
        BufferedReader  br = new BufferedReader(new InputStreamReader(fis));

        StringBuffer contents = new StringBuffer();
        String       line     = null;

        do {
            line = br.readLine();
            if(line != null) {
                contents.append(line);
            }
        } while(line != null);

        return contents.toString();
    }

    public static void writeText(String contents, File location)
            throws IOException
    {
        FileOutputStream fos = new FileOutputStream(location);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write(contents);
        bw.close();
    }

    public static void scheduleDelete(File f, int timeout) {
        new FileDeleter(f, timeout);
    }
}

class FileDeleter implements Runnable {
    File _file  = null;
    int  _delay = 0;

    public FileDeleter(File file, int delay) {
        _file = file;
        _delay = delay;
        new Thread(this).start();
    }

    public void run() {
        try {
            Thread.currentThread().sleep(_delay * 1000);
        }
        catch (InterruptedException e) {}

        _file.delete();
        System.err.println("Deleted " + _file.getName());
    }
}
