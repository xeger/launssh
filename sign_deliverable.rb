#!/usr/bin/env ruby

require 'webrick'

include WEBrick

basedir = File.expand_path( File.dirname(__FILE__) )

cmd = "jarsigner -tsa 'http://tsa.starfieldtech.com' #{basedir}/dist/launssh.jar rightscalejava_2009"
system(cmd) || (raise "Could not sign JAR")

cmd = "cp dist/launssh.jar ~/Projects/RightScale/right_site/public/ssh/launssh.jar"
system(cmd) || (raise "Could not copy JAR to right_site")

