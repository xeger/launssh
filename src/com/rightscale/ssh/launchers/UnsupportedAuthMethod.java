package com.rightscale.ssh.launchers;

@SuppressWarnings("serial")
public class UnsupportedAuthMethod extends Exception
{
    public UnsupportedAuthMethod(String message)
    {
        super(message);
    }
}
