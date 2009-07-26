#!/usr/bin/osascript

on run argv
       set AppleScript's text item delimiters to " "
       set cmd to (argv as text)

	set had_error to false
	
	-- First, attempt to open iTerm
	--Check first for its existence, which throws an error if it's not installed
	try
		tell application "Finder"
			if exists application file id "ITRM" then
				my open_iterm_tab("RightScale SSH", cmd)
			end if
		end tell
	on error
		set had_error to true
	end try
	-- As a fallthrough, open the SSH session with Mac Terminal
	--If this fails, don't bother catching the error; the user's system is probably borked anyway
	if had_error then
		my open_terminal_tab("RightScale SSH", cmd)
	end if
end run

on open_terminal_tab(title, cmd)
	tell application "Terminal" to activate
	tell application "System Events" to tell process "Terminal" to keystroke "t" using command down
	tell application "Terminal"
		tell window 0 to tell selected tab to tell current settings
			set custom title to title
			set clean commands to ["ssh", "login", "bash"]
		end tell
		do script with command cmd & ";clear;logout" in window 0
	end tell
end open_terminal_tab

on open_iterm_tab(title, cmd)
	tell application "iTerm"
		activate
		
		set term to (get current terminal)
		tell term
			set sess to (make new session)
			tell sess
				set name to title
				exec command cmd
			end tell
		end tell
	end tell
end open_iterm_tab
