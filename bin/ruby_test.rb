#!/usr/bin/env ruby

require 'octokit'

Octokit.netrc = '/Users/xrdawson/.netrc'
Octokit.user

# client = Octokit::Client.new(:login => "me", :password => "sekret")
# client.follow("sferik")

file = Octokit.contents 'xrd/xrd.github.com', :path => 'index.md'

puts file.inspect

