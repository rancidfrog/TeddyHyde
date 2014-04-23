
Then(/^I select "(.*?)"$/) do |text|
  touch( "listview textview text:'#{text}'" )
end

Then(/^I should see "(.*?)" in the menu$/) do |item|
  element_exists "* text:'#{item}'"
end
