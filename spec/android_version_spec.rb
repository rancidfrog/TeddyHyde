

require 'android_version'

describe AndroidVersion do
  it "should do point upgrades" do
    AndroidVersion.point( "0.3.1" ).should == "0.3.2"
    AndroidVersion.point( "0.3.10" ).should == "0.3.11"
  end
end
