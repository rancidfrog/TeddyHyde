
Then(/^I select "(.*?)"$/) do |text|
  touch( "listview textview text:'#{text}'" )
end

Then(/^I should see "(.*?)" in the hyde menu$/) do |item|
  out = query '*'
  puts out.inspect
  element_exists "* text:'#{item}'"
end

Then(/^I should not see "(.*?)" in the hyde menu$/) do |item|
  out = query '*'
  puts out.inspect
  element_exists "* text:'#{item}'"
end
