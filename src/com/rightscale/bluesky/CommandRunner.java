/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.bluesky;

import  java.io.*;

/**
 *
 * @author tony
 */
public interface CommandRunner {
    public void init();
    public void runHeadless(String command);
    public void run(String command);
    public File getSafeDirectory() throws IOException;
    public void reportError(String message, Exception e);
}
