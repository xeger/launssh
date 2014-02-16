package com.rightscale.ssh;

@SuppressWarnings("serial")
public class UnsupportedAuthMethod extends Exception
{
    public UnsupportedAuthMethod(String message)
    {
        super(message);
    }
}
