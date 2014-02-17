TODO

= Finish proof of concept

Integrate Application with GraphicalUI

= De-brand

Remove references to "rightscale" in hardcoded paths. Choose more reasonable/suitable hardcoded names.

Change package and class names to remove RightScale affiliation.

Add license header to all source files.

= MindTerm replacement

Create a new launcher that embeds an OpenSSH ssh.exe for Windows systems that lack PuTTY or a preinstalled OpenSSH client.

= Redo persistence 

Distinguish between safe dir (for application-related scripts, etc) and SSH preferences dir (for private keys, host config, etc)
Launcher owns preferences dir; launchpad owns safe dir!

= Command-Line Interface

Redesign CLI to be task-oriented. Should avoid all flags and options and use a "natural" invocation pattern. Something like:

# open an SSH session, using the optional alias to identify the host for purposes of host key auth, etc
launssh open [--session=foo] user@hostname [--host-key=<host public key>]

# import a private key file to key directory; set permissions suitably
launssh import <base64 ppk file or OpenSSH private key block>

= Preferences Dialog

Give users a way to order their preference of launcher, choose the location of their safe directory, and so forth.

= Verbose Mode

Add backtrace info to error dialogs. Add --verbose or --debug flag that pops up a dialog for normal log(String, Throwable) calls.

= 