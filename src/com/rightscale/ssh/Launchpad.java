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
    public File getSafeDirectory() throws IOException;
    public void reportError(String message, Exception e);
}
