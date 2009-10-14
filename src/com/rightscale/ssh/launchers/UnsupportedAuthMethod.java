package com.rightscale.ssh.launchers;

public class UnsupportedAuthMethod extends Exception
{
    public UnsupportedAuthMethod(String message)
    {
        super(message);
    }
}
