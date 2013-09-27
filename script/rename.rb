#!/usr/bin/env ruby

from = ARGV.shift
to = ARGV.shift

unless from and to
  puts "Need both from package and to package"
else
  from_dir = from.gsub ".", "/"
  to_dir = to.gsub ".", "/"

  puts "From: #{from_dir}, To: #{to_dir}"
  
  files = Dir.entries( File.join( "src", from_dir ) ).reject { |d| d =~ /^\.\.?$/ }

  files.each do |f|
    puts "Updating #{f}"
    contents = File.read( File.join( "src", from_dir, f ) )
    puts "Rewriting package name #{from} to #{to}"
    updated = contents.gsub( from, to )
    File.open( File.join( "src", to_dir, f ), "w+" ) do |o|
      puts "Writing file #{File.join( 'src', to_dir, f )}"
      o.write updated
    end
  end
  
end

