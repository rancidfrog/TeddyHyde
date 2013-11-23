require 'nokogiri'
$:<< File.join(File.dirname(__FILE__), 'rubylib')
require 'android_version'

VERSION_ATTRIBUTE = 'android:versionName'
VERSION_CODE = 'android:versionCode'
ANDROID_MANIFEST = "src/main/AndroidManifest.xml"

def bump_point( current )
  AndroidVersion.point( current )
end

def version( bump=false )
  # read and parse the old file
  file = File.read( ANDROID_MANIFEST )
  xml = Nokogiri::XML(file)
  current = nil
  
  # replace \n and any additional whitespace with a space
  xml.xpath("/manifest").each do |node|
    current = node[VERSION_ATTRIBUTE]
    code = node[VERSION_CODE]
    
    puts "Current version: #{current}/#{code}"
    if bump
      new_version = bump_point(current)
      new_code = code.to_i + 1
      node[VERSION_ATTRIBUTE] = new_version
      node[VERSION_CODE] = new_code
      puts "New version: #{new_version}/#{new_code}"
    end
  end
  if bump
    # save the output into a new file
    File.open( ANDROID_MANIFEST, "w") do |f|
      f.write xml.to_xml
    end
  end
end

namespace :version do
  desc "Update version"
  task :current do
    version()
  end
  
  desc "Point version"
  task :point do
    version(true)
  end

  desc "Major version change"
  task :major do
    puts "NYI"
  end

  desc "Minor version change"
  task :minor do
    puts "NYI"
  end
end
