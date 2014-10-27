package com.rightscale.ssh;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

public class Application extends net.xeger.ssh.Application {
	public static void main(final String args[]) {
		try {
			SwingUtilities.invokeAndWait(new Application(args));
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch(InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public Application(String[] args) {
		super(args);
	}
}
