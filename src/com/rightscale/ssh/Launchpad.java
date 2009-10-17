/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.ssh;

import  java.io.*;

/**
 *
 * @author tony
 */
public interface Launchpad {
    public void init();
    public File getSafeDirectory() throws IOException;
    public String getServerUUID();
    public void reportError(String message, Exception e);
}
