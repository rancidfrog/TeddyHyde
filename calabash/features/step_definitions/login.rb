
Given /I start the login process/ do
      touch "button"
end   

And /I acknowledge the 2 factor bug/ do
      tv = query( "textview", "text" )
      if tv =~ /2\-factor/ 
      touch "button"
      end   
end   


Then(/^I wait (\d+) seconds$/) do |arg1|
  sleep 10
end

And /I access the first repository/ do
      tv = query "textview"
      touch tv
end

And /I press "New Post"/ do
      np = query "TextView text:'New post'"
      touch np
end

And /I login using GitHub oAuth login/ do
      set_text "webView css:input[name=login]", "burning@burningon.com"
      set_text "webView css:input[name=password]", "L0udCaf3"
      performAction "scroll_to", "css", "a.oauth-deny"
      touch  "webView css:'button[name=authorize]'"
end    