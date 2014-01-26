package com.rightscale.ssh;

public interface UI {
	public void log(String message);
	public void log(String message, Throwable problem);
}