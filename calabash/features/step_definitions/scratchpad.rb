And /I click the "Scratchpad" menu item/ do
  scratchpad_menu = "TextView text:'Scratchpad...'"
  if element_does_not_exist scratchpad_menu
    touch( "OverflowMenuButton" )
  end
  touch scratchpad_menu
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
