package net.xeger.ssh;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;

/**
 * JavaBean that contains information about an SSH session, usually as parsed from the command line or launch parameters.
 * 
 * @author Tony Spataro
 *
 */
public interface SessionInfo {
	// //
	// // Read-only properties of the SSH session
	// //
	public String getUsername();
	public String getServer();
	public String getServerUUID();
	public String getServerName();	
	public String getAuthMethod();
	public String getSpecialPrivateKey();
	public String getSpecialPuttyPrivateKey();
	public String getPassword();
	public String getUserKeyPath();	
	public URL getTroubleshootingLink();
	
	// //
	// // Derivatives of read-only properties
	// //
	public File getUserKeyFile();

	// //
	// // Read/write properties
	// //
	
	public Launcher getLauncher();
	public void setLauncher(Launcher launcher);	

	// //
	// // Read/write properties
	// //
	
	public void launch();

	// //
	// // PropertyChangeListener support
	// //
	void addPropertyChangeListener(PropertyChangeListener listener);
	void removePropertyChangeListener(PropertyChangeListener listener);	
}
