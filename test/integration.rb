#!/usr/bin/env ruby

# launssh integration test: spawns a Web server on port 3000 to serve up the applet.

require 'webrick'

include WEBrick

basedir = File.expand_path("../..", __FILE__)
testdir = File.expand_path("..", __FILE__)

cmd = "cp #{basedir}/build/jar/launssh.jar #{testdir}/integration/launssh.jar"
system(cmd) || (raise "Could not copy JAR to integration testbed")

puts "Starting up HTTP server on port 3000 to serve applet; ^C to quit..."

s = HTTPServer.new(
  :Port            => 3000,
  :DocumentRoot    => "#{testdir}/integration"
)

trap("INT"){ s.shutdown }
s.start
