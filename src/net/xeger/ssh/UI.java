package net.xeger.ssh;

public interface UI {
	/// State indicating that we're busy launching an SSH client.
	public static final String LAUNCHING = "launching";

	/// Error state indicating that no compatible SSH client was found.
	public static final String NO_LAUNCHER = "choosing";
	
	/// Error state: no private key available (either parameters are missing, or key files are not found).
	public static final String MISSING_KEY = "missingKey";

	public void log(String message);

	public void log(String message, Throwable problem);

	public void alert(String message);

	public void alert(String message, Throwable problem);

	public void setDisplayState(String newState);
}