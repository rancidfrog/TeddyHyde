require 'curb'
require 'date'
@cd = nil
def set_cd
  @cd = DateTime.now()
  @cd.to_s
end

Then /I enter the current date into field with id "([^"]*)"/ do |id|
  query "View id:'#{id}'", setText: set_cd()
end

And /I click the "Scratchpad" menu item/ do
  fourandabove = "TextView text:'Scratchpad...'"
  honeycomb = "Button text:'Scratchpad...'" # for Honeycomb...
  menu = nil
  
  if element_does_not_exist fourandabove
    puts "No regular scratch pad"
    if element_does_not_exist honeycomb
      puts "No Honeycomb scratchpad button"
      touch( "OverflowMenuButton" )
      menu = fourandabove
    else
      menu = honeycomb
    end
  else
    menu = fourandabove
  end
  puts "Menu is #{menu}"
  touch menu if menu
end

Then /I should see the gist menu item/ do
  gist_exists()
end

def gist_exists
  element_exists "TextView id:'action_save_as_gist'"
end

Then /I should not see the gist menu item/ do
  not gist_exists()
end

And /I expose the menu items/ do
  touch( "OverflowMenuButton" )
end

And /I paste in the URL/ do
  performAction 'long_press_on_view_by_id', 'markdownEditor'
  touch "TextView text:'Paste'"
end

Then /I should have a gist with the current date/ do
  et = query "EditText id:'markdownEditor'"
  text = et[0].getText()
  # extract the URL
  url = $1 if text =~ /(https:\/\/gist.[^\s]*)/
  if url
    Curl::Easy.download convert_to_raw_gist( url ) do |curb|
      
    end
  end
end
