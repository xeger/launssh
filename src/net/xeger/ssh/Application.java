package net.xeger.ssh;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import net.xeger.ssh.ui.*;

import java.util.*;

import javax.swing.SwingUtilities;

public class Application implements Runnable, Session
{
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

    private Map<String, String>   _parameters  = new HashMap<String, String>();
    
    private PropertyChangeSupport _thisBean    = new PropertyChangeSupport(this);	
    private DialogFrame           _frame       = new DialogFrame("SSH Launcher");
    private GraphicalUI           _ui          = new GraphicalUI(this, _frame);
    private Launchpad             _launchpad   = new Launchpad(_ui);


    public Application(String[] args)
    {
    	for(int i = 0; i < args.length; i++) {
    		String s = args[i];
    		int breakAt = s.indexOf('=');
    		if(breakAt >= 0) {
    			String name = s.substring(0, breakAt);
    			String value = s.substring(breakAt+1);
    			_parameters.put(name, value);
    		}
    		else {
    			String excerpt = s.substring(0, s.length() > 16 ? 16 : s.length());
                throw new IllegalArgumentException(String.format("Malformed command-line argument at position %d; expecting 'k=v', got '%s...'", i, excerpt));    			    		
    		}
        }

    	_frame.setContentPane(new net.xeger.ssh.ui.CenteringPanel(_ui));
    }

    /// Run the application.
    public void run()
    {    	
    	_frame.setVisible(true);    	

    	Map<KeyFormat, String> privateKeys = new HashMap<KeyFormat, String>();

        try {
	        if( getAuthMethod().equals(AuthMethod.PUBLIC_KEY) ) {
	            if( getUserKeyPath() != null && hasUserKeyFile() ) {
	                privateKeys.put( KeyFormat.OPEN_SSH, getUserPrivateKey() );
	                _ui.log("User OpenSSH private key loaded from local disk");
	            }
	            else if( getSpecialPrivateKey() != null ) {
	                privateKeys.put( KeyFormat.OPEN_SSH, getSpecialPrivateKey() );
	                _ui.log("Server-specific OpenSSH private key loaded from parameters");
	            }
	            else {
	                _ui.log("No OpenSSH private key loaded");
	            }
	
	            if( getUserKeyPath() != null && hasUserPuttyKeyFile() ) {
	                privateKeys.put( KeyFormat.PUTTY, getUserPuttyPrivateKey() );
	                _ui.log("User PuTTY private key loaded from local disk");
	            }
	            if( getSpecialPuttyPrivateKey() != null ) {
	                privateKeys.put( KeyFormat.PUTTY, getSpecialPuttyPrivateKey() );
	                _ui.log("Server-specific PuTTY private key loaded from local disk");
	            }
	            else {
	                _ui.log("No PuTTY private key loaded");
	            }
	
	            if(privateKeys.isEmpty() && getUserKeyPath() == null) {
	                throw new IllegalArgumentException("Unable to identify a private key; add openssh-key-material=, putty-key-material= or user-key-path=");
	            }
	        }
	        else if( getAuthMethod().equals(AuthMethod.PASSWORD)) {
	            if(getPassword() != null && getPassword().length() > 0) {
	                _launchpad.setPassword(getPassword());
	            }
	            else {
	                    throw new IllegalArgumentException("Unable to determine password; add password=");
	            }            
	        }
	
	        String uuid = getServerUUID();
	        if(uuid == null) {
	            uuid = getServer();
	        }
	
	        //Initialize the launchpad business logic
	        _launchpad.setUsername(getUsername());
	        _launchpad.setServer(getServer());
	        _launchpad.setServerUUID(uuid);
	        _launchpad.setPrivateKeys(privateKeys);

			// Arbitrary choose the first available launcher, going through our own setter so
			// we fire a property-change notification for the UI.
			// @todo consult preferences, or ask the user if multiple choices exist
			setLauncher(_launchpad.getLaunchers().get(0));
	        
	        if(_launchpad.isLauncherAvailable()) {
	            _launchpad.run();
	        }
	        else {
	            throw new IllegalArgumentException("No supported SSH client is available.");
	        }
        }
        catch(IllegalArgumentException e) {
            _ui.alert("Cannot launch SSH: " + e.getMessage());
        }
        catch(Exception e) {
            _ui.alert("Cannot launch SSH", e);
        }
    }

    ////
    //// An Applet workalike method for accessing command line parameters, plus getters for the various params we expect
    ////

    protected String getParameter(String name) {
        return _parameters.get(name);
    }

    public String getUsername() {
        String v = getParameter("username");

        if(v != null)
            return v;
        else
            return System.getProperty("user.name");
    }

    public String getServer() {
        String v = getParameter("server");

        if(v != null)
            return v;
        else
            return "localhost";
    }

    public String getServerUUID() {
        return getParameter("server-uuid");
    }

    public String getServerName() {
        String v = getParameter("server-name");

        if(v != null)
            return v;
        else
            return getServer();
    }

    public AuthMethod getAuthMethod() {
        if("password".equals(getParameter("auth-method")))
            return AuthMethod.PASSWORD;
        else
            return AuthMethod.PUBLIC_KEY;
    }

    public String getSpecialPrivateKey() {
        String newline = System.getProperty("line.separator");
        String km = getParameter("openssh-key-material");
        if(km == null) return null;
        return km.replaceAll("\\*", newline);
    }

    public String getSpecialPuttyPrivateKey() {
        String newline = System.getProperty("line.separator");
        String km = getParameter("putty-key-material");
        if(km == null) return null;
        return km.replaceAll("\\*", newline);
    }

    public String getPassword() {
        return getParameter("password");
    }

    public String getUserKeyPath() {
        return getParameter("user-key-path");
    }

    public URL getTroubleshootingLink() {
        try {
            return new URL( getParameter("troubleshooting-url") );
        }
        catch(MalformedURLException e) {
            return null;
        }
    }

    protected File getServerKeyFile() {
        return new File(_launchpad.getSafeDirectory(), getServerUUID());
    }

    protected File getServerPuttyKeyFile() {
        return new File(_launchpad.getSafeDirectory(), getServerUUID() + ".ppk");
    }

    public File getUserKeyFile() {
        String path = getUserKeyPath();
        if(path == null)
            return null;


        //Split the path into elements, accepting either \ or / as a separator
        String[] elements = path.split("/|\\\\");

        String home = System.getProperty("user.home");

        StringBuffer canonPath = new StringBuffer();
        canonPath.append(home);

        for(String elem : elements) {
            canonPath.append(File.separator);
            canonPath.append(elem);
        }

        return new File(canonPath.toString());
    }

	public Launcher getLauncher() {
		return _launchpad.getLauncher();
	}

	public void setLauncher(Launcher launcher) {
		_thisBean.firePropertyChange("launcher", _launchpad.getLauncher(), launcher);
		_launchpad.setLauncher(launcher);
	}

	public void launch() {
		try {
			_launchpad.run();
		} catch (Exception e) {
			_ui.alert("Cannot launch your computer's SSH client", e);
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		_thisBean.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		_thisBean.removePropertyChangeListener(listener);
	}
	
    protected File getUserPuttyKeyFile() {
        File f = getUserKeyFile();
        String s = f.getPath();

        if(s.endsWith(".ppk")) {
            return f;
        }
        else {
            return new File(s + ".ppk");
        }
    }

    protected boolean hasUserKeyFile() {
        File f = getUserKeyFile();
        return f.exists();
    }

    protected boolean hasUserPuttyKeyFile() {
        File f = getUserPuttyKeyFile();
        return f.exists();
    }

    protected String getUserPrivateKey()
    {
    	BufferedReader br = null;
    	StringBuffer   sb = null;
    	
        try {
            if(hasUserKeyFile()) {
                File f = getUserKeyFile();
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                sb = new StringBuffer();
                while(br.ready()) {
                    sb.append(br.readLine());
                    sb.append("\n");
                }

                return sb.toString();
            }
            else {
                throw new Error("Key file does not exist");
            }
        }
        catch(Exception e) {
            return null;
        }
        finally {
        	if(br != null) {        		
        		try { br.close(); } catch(Exception e) {}
        	}
        }
    }

    protected String getUserPuttyPrivateKey()
    {
    	BufferedReader br = null;
    	StringBuffer   sb = null;
    	
        try {
            if(hasUserPuttyKeyFile()) {
                File f = getUserPuttyKeyFile();
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                sb = new StringBuffer();
                while(br.ready()) {
                    sb.append(br.readLine());
                    sb.append("\n");
                }

                return sb.toString();
            }
            else {
                throw new Error("Key file does not exist");
            }
        }
        catch(Exception e) {
            return null;
        }
        finally {
        	if(br != null) {        		
        		try { br.close(); } catch(Exception e) {}
        	}
        }
    }
}
