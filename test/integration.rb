#!/usr/bin/env ruby

# launssh integration test: spawns a Web server on port 3000 to serve up the applet.

require 'webrick'
require 'fileutils'

include WEBrick

basedir = File.expand_path("../..", __FILE__)
testdir = File.expand_path("..", __FILE__)

puts "Copying launssh.jar into integration dir"
FileUtils.cp File.join(basedir,'build', 'jar', 'launssh.jar'), File.join(testdir, 'integration', 'launssh.jar')

s = HTTPServer.new(
  :Port            => 3000,
  :DocumentRoot    => "#{testdir}/integration"
)

trap("INT"){ s.shutdown }

puts "Starting up HTTP server on port 3000 to serve applet; ^C to quit..."
s.start
