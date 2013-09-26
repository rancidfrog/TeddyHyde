#!/usr/bin/env ruby

from = ARGV.shift
to = ARGV.shift

unless from and to
  puts "Need both from package and to package"
else
  from.split( "." ).each do |d|
    Dir.chdir d
  end

  # Do stuff within this directory
  

  # Restore the directory
  from.split( "." ).each do |d|
    Dir.chdir ".."
  end
  
end

