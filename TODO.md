TODO

= Finish proof of concept

Get the Applescript launcher working (there's something wrong with the key material, user key file, etc)

= Command-Line Interface

Redesign CLI to be task-oriented. Should avoid all flags and options and use a "natural" invocation pattern. Something like:

# open an SSH session, using the optional alias to identify the host for purposes of host key auth, etc
launssh [--alias=foo] user@hostname [<base64 public key>]

# import a private key file to safe directory; set permissions suitably
launssh <base64 ppk file or OpenSSH private key block>

= De-brand

Distinguish between safe dir (for application-related scripts, etc) and SSH preferences dir (for private keys, host config, etc)
Launcher owns preferences dir; launchpad owns safe dir!

Remove references to "rightscale" in hardcoded paths. Choose more reasonable/suitable hardcoded names.

Change package and class names to remove RightScale affiliation.

= Preferences Dialog

Give users a way to order their preference of launcher, choose the location of their safe directory, and so forth.

= Verbose Mode

Add backtrace info to error dialogs. Add --verbose or --debug flag that pops up a dialog for normal log(String, Throwable) calls.

