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
