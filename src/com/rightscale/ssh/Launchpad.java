package com.rightscale.ssh;

import com.rightscale.ssh.launchers.*;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;


/**
 *
 * @author Tony Spataro
 */
public class Launchpad
{
	static final private int KEY_DELETION_TIMEOUT = 300;

	static final private String[] LAUNCHERS = {
		"com.rightscale.ssh.launchers.osx.Applescript",
		"com.rightscale.ssh.launchers.unix.GnomeTerminal",
		"com.rightscale.ssh.launchers.unix.Konsole",
		"com.rightscale.ssh.launchers.unix.Xterm",
		"com.rightscale.ssh.launchers.windows.PuTTY",
		"com.rightscale.ssh.launchers.windows.OpenSSH",
		"com.rightscale.ssh.launchers.windows.GenericSSH"
	};

	private UI                     _ui                 = null;
	private String                 _username           = null;
	private String                 _server             = null;
	private String                 _serverUUID         = null;
	private Map<KeyFormat, String> _privateKeys        = null;
	private String                 _password           = null;
	private ArrayList<Launcher>    _launchers          = new ArrayList<Launcher>();
	private Launcher               _launcher           = null;
	private Set<KeyFormat>         _keyFormats         = new HashSet<KeyFormat>();

	////
	//// Launchpad implementation
	////

	public Launchpad(UI ui) {
		_ui = ui;
	}

	public File getSafeDirectory() {
		String dir = System.getProperty("user.home");
		dir = dir + "/.rightscale";
		return new File(dir);
	}

	////
	//// Read/write properties
	////

	public String getPrivateKey(KeyFormat format) {
		Object o = _privateKeys.get(format);
		if(o != null) {
			return (String)o;
		}
		else {
			return null;
		}
	}

	public void setPrivateKeys(Map<KeyFormat, String> privateKeys) {
		_privateKeys = privateKeys;
		initializeLaunchers();
	}

	public boolean hasKeyMaterial() {
		return (_privateKeys != null) && !_privateKeys.isEmpty();
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		validate(password);
		_password = password;
	}

	public boolean hasPassword() {
		return (_password != null);
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		validate(username);
		_username = username;
	}

	public String getServer() {
		return _server;
	}

	public void setServer(String server) {
		validate(server);
		_server = server;
	}

	public String getServerUUID() {
		return _serverUUID;
	}

	public void setServerUUID(String serverUUID) {
		validate(serverUUID);
		_serverUUID = serverUUID;
	}


	////
	//// Read-only and derived properties
	////

	public boolean hasKeyFormat(KeyFormat keyFormat) {
		return ( _privateKeys != null && _privateKeys.get(keyFormat) != null );
	}

	public boolean isLauncherAvailable() {
		return (_launchers.size() > 0);
	}

	@SuppressWarnings("unchecked")
	public List<Launcher> getLaunchers() {
		return (List<Launcher>) _launchers.clone();
	}
	
	public Launcher getLauncher() {
		return _launcher;
	}

	public void setLauncher(Launcher launcher) {
		_launcher = launcher;
	}
	
	public File getSpecialKeyFile(KeyFormat keyFormat) {
		if(getServerUUID() == null) {
			return null;
		}

		switch(keyFormat) {
		case OPEN_SSH:
			return new File(getSafeDirectory(), getServerUUID());
		case PUTTY:
			return new File(getSafeDirectory(), getServerUUID() + ".ppk");
		default:
			throw new Error("Unsupported key format");
		}
	}

	////
	//// Methods that do useful stuff
	////

	public void writePrivateKeys()
			throws IOException
			{
		if(_keyFormats.contains(KeyFormat.OPEN_SSH)) {
			String matl = getPrivateKey(KeyFormat.OPEN_SSH);
			File   key  = getSpecialKeyFile(KeyFormat.OPEN_SSH);

			if(matl != null && key != null) {
				SimpleLauncher.writePrivateKey(matl, key, KEY_DELETION_TIMEOUT);
			}
			else {
				_ui.log("Not writing special OpenSSH private key (key material or name is null)");
			}

		}
		if(_keyFormats.contains(KeyFormat.PUTTY)) {
			String matl = getPrivateKey(KeyFormat.PUTTY);
			File   key  = getSpecialKeyFile(KeyFormat.PUTTY);

			if(matl != null && key != null) {
				SimpleLauncher.writePrivateKey(matl, key, KEY_DELETION_TIMEOUT);
			}
			else {
				_ui.log("Not writing special PuTTY private key key material or name is null)");
			}
		}
			}

	public boolean run()
			throws IOException
	{
		initializeLaunchers();

		try {
			writePrivateKeys();
		}
		catch(IOException e) {
			_ui.log("Could not write your private key file.", e);
			return false;
		}

		//Try all the launchers in sequence
		Iterator<Launcher> it = _launchers.iterator();
		while( it.hasNext() ) {
			Launcher l = (Launcher)it.next();

			_ui.log("  Running " + l.getClass().getName());

			try {
				// Attempt public-key auth first
				for(KeyFormat f : KeyFormat.values()) {
					if(hasKeyFormat(f) && l.supportsKeyFormat(f)) {
						l.run( getUsername(), getServer(), getSpecialKeyFile(f) );
						return true;					
					}					
				}
				
				// Try password auth if we were given one
				if(hasPassword()) {
					l.run( getUsername(), getServer(), getPassword() );
					return true;
				}
				
				// Give up and throw an error
				if(_keyFormats.size() == 0) {
					throw new MissingKeyMaterial("No key material or password is available; can't authenticate");						
				}
				else {
					StringBuffer mine = new StringBuffer(), his = new StringBuffer();
					
					for(KeyFormat f : KeyFormat.values()) {
						if(hasKeyFormat(f)) {
							if(mine.length() > 0) mine.append(",");
							mine.append(f);
						}
						if(l.supportsKeyFormat(f)) {
							if(his.length() > 0) his.append(",");
							his.append(f);
						}
					}
					
					throw new MissingKeyMaterial(String.format("Incompatible key material: I have [%s]; launcher supports [%s]", mine, his));						
				}
			}
			catch(Exception e) {
				_ui.alert("Failed to launch using " + l.getFriendlyName(), e);
			}
		}

		return false;
	}

	private void initializeLaunchers() {
		Class<?>[]  paramTypes = {Launchpad.class};
		Object[] params     = {this};

		_launchers.clear();
		_keyFormats.clear();

		for(int i = 0; i < LAUNCHERS.length; i++) {
			String cn = LAUNCHERS[i];

			try {
				Constructor<?> ctor = Class.forName(cn).getConstructor(paramTypes);
				Launcher l = (Launcher) ctor.newInstance(params);

				_ui.log(cn + " is COMPATIBLE.");

				if(l.canPublicKeyAuth()) {
					for(KeyFormat f : KeyFormat.values()) {
						if(l.supportsKeyFormat(f)) {
							_keyFormats.add(f);
						}
					}
				}
				
				_launchers.add(l);

			}			
			catch(InvocationTargetException e) {
				_ui.log(cn + " is INCOMPATIBLE (threw exception during initialization)", e.getCause());                
			}
			catch(Exception e) {
				_ui.log(cn + " is INCOMPATIBLE (threw exception during initialization)", e);
			}
		}
	}

	// Validate a parameter against a whitelist of known-good characters and raise an exception if it's suspicious.
	private void validate(String str) {
		if(str != null) {
			Pattern p = Pattern.compile("[^A-Za-z0-9\\_\\-\\.]");
			Matcher m = p.matcher(str);
			if(m.matches()) {
				throw new SecurityException("Input contains unsafe characters: " + str);
			}
		}
	}
}
