package com.rightscale.ssh;

public class Application extends net.xeger.ssh.Application {
	public static void main(String args[]) {
        System.exit(new Application(args).run());
	}
	
	public Application(String[] args) {
		super(args);
	}
}
