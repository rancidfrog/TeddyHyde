require 'nokogiri'
$:<< File.join(File.dirname(__FILE__), 'rubylib')
require 'android_version'

VERSION_ATTRIBUTE = 'android:versionName'
VERSION_CODE = 'android:versionCode'
ANDROID_MANIFEST = "src/main/AndroidManifest.xml"

def bump_point( current )
  AndroidVersion.point( current )
end

def get_version_and_code( node )
  current = node[VERSION_ATTRIBUTE]
  code = node[VERSION_CODE]
  [ current, code ]
end

def extract_current_and_code_from_xml( xml, bump=false )

  # read and parse the old file
  current = nil
  code = nil
  new_version = nil
  new_code = nil

  # replace \n and any additional whitespace with a space
  xml.xpath("/manifest").each do |node|
    tuple = get_version_and_code(node)
    current = tuple.shift
    code = tuple.shift
    
    puts "Current version: #{current}/#{code}"
    if bump
      new_version = bump_point(current)
      new_code = code.to_i + 1
      node[VERSION_ATTRIBUTE] = new_version
      node[VERSION_CODE] = new_code
      puts "New version: #{new_version}/#{new_code}"
    end
  end
  [ current, code ]
end

def get_xml
  file = File.read( ANDROID_MANIFEST )
  xml = Nokogiri::XML(file)
end

def version( bump=false )
  xml = get_xml()
  extract_current_and_code_from_xml( xml, bump )
  
  if bump
    # save the output into a new file
    File.open( ANDROID_MANIFEST, "w") do |f|
      f.write xml.to_xml
    end
  end
end

namespace :version do
  desc "Tag current version and code"
  task :tag do
    xml = get_xml()
    tuple = extract_current_and_code_from_xml( xml )
    current = tuple.shift
    code = tuple.shift
    # Do git tag
    the_tag = "#{current}-#{code}"
    `git tag #{the_tag}"`
  end
  
  desc "Current version"
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
