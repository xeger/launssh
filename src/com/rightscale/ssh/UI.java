package com.rightscale.ssh;

public interface UI {
	public void log(String message);
	public void log(String message, Throwable problem);
	public void alert(String message);
	public void alert(String message, Throwable problem);
}