package net.xeger.ssh;

@SuppressWarnings("serial")
public class MissingKeyMaterial extends Exception {
    public MissingKeyMaterial(String message)
    {
        super(message);
    }
}
