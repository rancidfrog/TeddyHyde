And /I click the "Scratchpad" menu item/ do
  puts "Ok, in here"
  scratchpad_menu = "TextView text:'Scratchpad...'"
  if element_does_not_exist scratchpad_menu
    touch( "OverflowMenuButton" )
  end
  touch scratchpad_menu
end
