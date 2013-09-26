#!/usr/bin/env ruby

from = ARGV.shift
to = ARGV.shift

unless from and to
  puts "Need both from package and to package"
else
  to = "src/" + to
  from = "src/" + from
  from_dir = from.gsub ".", "/"
  to_dir = to.gsub ".", "/"

  puts "From: #{from_dir}, To: #{to_dir}"
  
  files = Dir.entries( from_dir ).reject { |d| d =~ /^\.\.?$/ }

  files.each do |f|
    contents = File.read( File.join( from_dir, f ), "r" )
    updated = contents.gsub( from, to )
    File.open( File.join( to_dir, f ), "w+" ) do |o|
      o.write contents
    end
  end
  
end

