require 'byebug'
require 'date'
FORMAT = "%Y-%m-%d"
filename = "My first file"
@auth_token = nil

def convert_name( text )
  rv = "#{DateTime.now().strftime( FORMAT )}-#{text.downcase.gsub(/\s+/, '-' )}.md"
  rv
end

Given /I start the login process/ do
  touch "button text:'Login'"
end   

And /I acknowledge the 2 factor bug/ do
  tv = query( "textview", "text" )
  if tv[0] =~ /2\-factor authentication/ 
    touch "button text:'Ok'"
  end   
end   

And /I scroll to the top/ do
  performAction('scroll_up')
end

And /I select "New Post"/ do
  touch "ActionMenuItemView id:'action_add_new_post'"
end

And /I select "Save File"/ do
  touch "ActionMenuItemView text:'Save File'"
end


Then /I click on the list item named "([^\"]*)"/ do |text|
  jekyll_name = convert_name( text )
  touch("TextView text:'#{jekyll_name}'")
end

And /I grab the oauth token for cleanup/ do
  preferences = get_preferences("com.EditorHyde.app")
  @auth_token = preferences['authToken']

end

def cleanup
  file = convert_name( "My new file" )
  # curl -u "your_login:your_token" \
  #     https://api.github.com/repos/user/repo
  cmd = "curl -i -X DELETE -u 'BurningOnUp:#{@auth_token}' https://api.github.com/repos/BurningOnUp/mynewrepo/contents/_posts/#{file}"
  puts "Cmd: #{cmd}"
  `#{cmd}`
  
  # /applications/:client_id/tokens
  cmd = "curl -i -X DELETE -u 'BurningOnUp:#{@auth_token}' https://api.github.com/applications/e4f185a088112cb1b0e9/tokens"
  #  puts "Delete the token for the app"
  `#{cmd}`
end

Then /^I scroll until I see the "([^\"]*)" as a jekyll post/ do |text|
  jekyll_name = convert_name( text )
  wait_for( timeout: 60  ) { query("TextView text:'#{jekyll_name}'") }
end

And /I don't see a logout button/ do
  query( "button text:'Logout from GitHub'" )
end

And /I wait for the repository layout/ do
  wait_for( timeout: 60 ) { query( "textview id:'repo_name'" ) }
end

And /I wait for the editor/ do
  wait_for( timeout: 60 ) { query( "editext id:'markdownEditor'" ) }
end

Then /^I should see the text containing "(.*?)"$/ do |arg1|
  et = query( "edittext" )
  puts et.inspect
  unless et[0]['text'] =~ /markdown/i
    fail( "Could not find markdown text" )
  end
end

Then /I wait for the "([^\"]*)" dialog to close/ do |text|
  # See if the dialog exists right now...
  unless query( "textview text:'#{text}'" ).length == 0
    # If it does, then wait for it to close...
    wait_for( timeout: 60 ) { 0 == query( "textview text:'#{text}'" ).length }
  end
end

And /I wait for the GitHub oAuth login page/ do
  wait_for( timeout: 30 ) { query( "webview" ) }
  wait_for( timeout: 30 ) { query( "webview css:'*'" ).length != 3 }
end

And /I enter "([^\"]*)" as the filename/ do |name|
  query "edittext", :setText => name
end

And /I login using GitHub oAuth login/ do
  set_text "webView css:input[name=login]", "burning@burningon.com"
  set_text "webView css:input[name=password]", "smallbaby1"
  #set_text "webView css:input[name=password]", "L0udCaf3"
  touch "webView input:'button[type=submit]'"
  sleep 10
  text = query( "webView css:'.bubble-content'", "textContent" )[0]
  if text =~ /Teddy Hyde Android wants/
    performAction "scroll_to", "css", "a.oauth-deny"
    touch "webView css:'button[name=authorize]'"
  end	    
end    

And /I access the "([^\"]*)" repository/ do |text|
  touch( "listview textview text:'#{text}'" )
end 
