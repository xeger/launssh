package com.rightscale.ssh;

import com.rightscale.util.*;
import com.rightscale.ssh.launchers.*;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import javax.jnlp.*;
import javax.swing.*;
import java.util.*;
import java.security.*;

public class Application {
	public static void main(String args[]) {
	    Application app = new Application();
	}

    ////
    //// Implementation of com.rightscale.ssh.UI
    ////

    public void log(String message) {
        System.out.println(message);
    }

    public void log(String message, Throwable problem) {
        System.out.println(message);
    }	
}
