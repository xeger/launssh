#!/usr/bin/env ruby

require 'webrick'

include WEBrick

basedir = File.expand_path( File.dirname(__FILE__) )

unless ARGV[0] == "nosign"
  cmd = "jarsigner -tsa 'http://tsa.starfieldtech.com' #{basedir}/dist/launssh.jar rightscalejava_test"
  system(cmd) || (raise "Could not sign JAR")
end

cmd = "cp #{basedir}/dist/launssh.jar #{basedir}/integration/launssh.jar"
system(cmd) || (raise "Could not copy JAR to integration testbed")

puts "Starting up HTTP server on port 3000 to serve applet; ^C to quit..."

s = HTTPServer.new(
  :Port            => 3000,
  :DocumentRoot    => "#{basedir}/integration"
)

trap("INT"){ s.shutdown }
s.start
